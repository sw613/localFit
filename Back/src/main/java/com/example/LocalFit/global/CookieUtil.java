package com.example.LocalFit.global;

import com.example.LocalFit.auth.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CookieUtil {
    private final JwtProperties jwtProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, "accessToken", token,
                (int) (jwtProperties.getAccessTokenValidTime() / 1000));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, "refreshToken", token,
                (int) (jwtProperties.getRefreshTokenValidTime() / 1000));
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        //cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        //cookie.setSecure(false);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        //response.addHeader("Set-Cookie", name + "=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
        response.addCookie(cookie);

    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (name.equals(cookie.getName())) {
                        return Optional.of(cookie);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("쿠키 조회 중 오류 발생: ", e.getMessage());
            return Optional.empty();
        }
    }
}
