package com.example.workmanager.config;

import com.example.workmanager.service.CookieService;
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
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

//        final String authHeader = request.getHeader("Authorization");
//        logger.info("DEBUG: Request URI: {}", request.getRequestURI());
//        logger.info("DEBUG: Authorization header: {}", authHeader);

        String jwt = cookieService.getCookieValue(request, "access_token");
        String username = null;

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

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                logger.warn("Access token expired. Trying refresh...");

                // Nếu access token hết hạn → thử lấy refresh token
                String refreshToken = cookieService.getCookieValue(request, "refresh_token");
                if (refreshToken != null) {
                    try {
                        String refreshUsername = jwtUtil.extractUsername(refreshToken);

                        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshUsername);

                        if (jwtUtil.isTokenValid(refreshToken, userDetails)) {
                            // Sinh access token mới
                            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
                            cookieService.setAuthCookie(response, newAccessToken);

                            username = refreshUsername;
                            jwt = newAccessToken;

                            logger.info("Issued new access token for user: {}", refreshUsername);
                        } else {
                            logger.warn("Invalid refresh token for user: {}", refreshUsername);
                        }
                    } catch (Exception ex) {
                        logger.error("Error while refreshing token", ex);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing access token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("DEBUG: Authentication set for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}