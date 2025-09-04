package com.example.workmanager.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${jwt.cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${jwt.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${jwt.cookie.access-token-age:300}") // 5 phút
    private int accessTokenAge;

    @Value("${jwt.cookie.refresh-token-age:604800}") // 7 ngày
    private int refreshTokenAge;

    // Ghi access_token vào cookie
    public void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(accessTokenAge);

        if (cookieDomain != null && !cookieDomain.isEmpty() && !cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }

    // Xóa access_token
    public void clearAuthCookie(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("access_token", "");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(cookieSecure);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            deleteCookie.setDomain(cookieDomain);
        }

        response.addCookie(deleteCookie);
    }

    // Ghi refresh_token vào cookie
    public void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenAge);

        if (cookieDomain != null && !cookieDomain.isEmpty() && !cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }

    // Xóa refresh_token
    public void clearRefreshCookie(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("refresh_token", "");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(cookieSecure);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            deleteCookie.setDomain(cookieDomain);
        }

        response.addCookie(deleteCookie);
    }

    // Lấy giá trị cookie theo tên
    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
