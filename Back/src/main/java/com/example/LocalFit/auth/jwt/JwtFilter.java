package com.example.LocalFit.auth.jwt;

import com.example.LocalFit.auth.service.RedisTokenService;
import com.example.LocalFit.global.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = cookieUtil.getCookie(request, "accessToken")
                    .map(Cookie::getValue)
                    .orElse(null);

            if (jwt != null) {
                // 토큰이 유효한지 확인
                if (tokenProvider.isValidToken(jwt)) {
                    // 블랙리스트에 있는지 확인
                    if (redisTokenService.isTokenBlacklisted(jwt)) {
                        // 블랙리스트에 있으면 인증 실패 처리
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"message\":\"블랙리스트에 등록된 토큰입니다.\"}");
                        response.setContentType("application/json");
                        return;
                    }

                    // 정상 토큰이면 인증 처리
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("JWT 토큰 처리 중 오류 발생", e);
            // 예외 발생 시 인증 컨텍스트를 비웁니다
            SecurityContextHolder.clearContext();
            // 예외를 전파하지 않고 다음 필터로 진행합니다
        }

        filterChain.doFilter(request, response);
    }
}