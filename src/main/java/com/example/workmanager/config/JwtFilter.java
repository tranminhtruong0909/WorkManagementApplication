package com.example.workmanager.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

//        final String authHeader = request.getHeader("Authorization");
//        logger.info("DEBUG: Request URI: {}", request.getRequestURI());
//        logger.info("DEBUG: Authorization header: {}", authHeader);

        String username = null;
        String jwt = null;

//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            jwt = authHeader.substring(7);
//            logger.info("DEBUG: JWT token extracted: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
//
//            // ✅ Kiểm tra token có đúng định dạng JWT hay không (tương thích mọi Java version)
//            if (jwt.split("\\.").length - 1 != 2) {
//                logger.warn("Invalid JWT format: {}", jwt);
//                filterChain.doFilter(request, response);
//                return;
//            }
//            try {
//                username = jwtUtil.extractUsername(jwt);
//                logger.info("DEBUG: Username extracted from JWT: {}", username);
//            } catch (ExpiredJwtException e) {
//                logger.warn("JWT expired: {}", jwt);
//            } catch (Exception e) {
//                logger.error("Error parsing JWT: {}", jwt, e);
//            }
//        } else {
//            logger.warn("DEBUG: No Authorization header or invalid format");
//        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    logger.info("DEBUG: JWT Token extracted from cookies: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "....");
                    break;
                }
            }
        }

        if  (jwt == null) {
            final String Authorization = request.getHeader("Authorization");
            if (Authorization != null && Authorization.startsWith("Bearer ")) {
                jwt = Authorization.substring(7);
                logger.info("DEBUG: JWT Token extracted from Authorization: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "....");
            }
        }

        if (jwt != null) {
            if( jwt.split("\\.").length -1  != 2 ) {
                logger.info("DEBUG: JWT Token extracted from JWT: {}", jwt);
                filterChain.doFilter(request, response);
                return;
            }
            try{
                username = jwtUtil.extractUsername(jwt);
                logger.info("DEBUG: Username extracted from JWT: {}", username);
            }
            catch(ExpiredJwtException e){
                logger.warn("JWT expired {}", jwt);
            }
            catch(Exception e){
                logger.error("Error parsing JWT: {}", jwt, e);
            }
        }else {
            logger.warn("DEBUG: No JWT token found in cookie or Authorization header!");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("DEBUG: Authentication set for user: {}", username);
            } else {
                logger.warn("DEBUG: JWT token is not valid for user: {}", username);
            }
        } else {
            logger.info("DEBUG: Username is null or authentication already exists");
        }

        filterChain.doFilter(request, response);
    }
}
