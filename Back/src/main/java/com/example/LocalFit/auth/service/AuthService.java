package com.example.LocalFit.auth.service;

import com.example.LocalFit.auth.dto.LoginReqDto;
import com.example.LocalFit.auth.jwt.TokenBlacklistService;
import com.example.LocalFit.auth.jwt.TokenProvider;
import com.example.LocalFit.global.CookieUtil;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;
    private final RedisTokenService redisTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public void login(LoginReqDto loginReqDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginReqDto.getEmail(),
                            loginReqDto.getPassword()
                    )
            );
            // 기존 RefreshToken이 삭제
            redisTokenService.deleteRefreshToken(authentication.getName());

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // Redis에 저장
            redisTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            cookieUtil.addAccessTokenCookie(response, accessToken);
            cookieUtil.addRefreshTokenCookie(response, refreshToken);

        } catch (Exception e) {
            log.error("Login failed: ", e);
            throw e;
        }
    }

    @Transactional
    public void refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN));

        // 토큰 유효성 검사
        if (!tokenProvider.isValidToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        // Redis에 저장된 RefreshToken과 비교
        if (!redisTokenService.validateRefreshToken(authentication.getName(), refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        // 이전 토큰 삭제 및 새 토큰 저장
        redisTokenService.deleteRefreshToken(authentication.getName());
        redisTokenService.saveRefreshToken(authentication.getName(), newRefreshToken);

        cookieUtil.addAccessTokenCookie(response, newAccessToken);
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && tokenProvider.isValidToken(refreshToken)) {
            try {
                Authentication authentication = tokenProvider.getAuthentication(refreshToken);
                // Redis에서 RefreshToken 삭제
                redisTokenService.deleteRefreshToken(authentication.getName());
            } catch (Exception e) {
                log.warn("RefreshToken 삭제 실패: {}", e.getMessage());
            }
        }

        cookieUtil.deleteCookie(response, "accessToken");
        cookieUtil.deleteCookie(response, "refreshToken");

        SecurityContextHolder.clearContext();
    }

    // 회원 탈퇴나 비밀번호 변경 시 해당 사용자의 모든 토큰 무효화
    public void revokeAllUserTokens(String username) {
        // 현재 리프레시 토큰 가져오기
        Optional<String> currentRefreshToken = redisTokenService.getRefreshToken(username);

        // 리프레시 토큰이 있으면 블랙리스트에 추가
        currentRefreshToken.ifPresent(token -> {
            try {
                long expiry = tokenProvider.getTokenValidTime(token);
                redisTokenService.addToBlacklist(token, expiry);
            } catch (Exception e) {
                log.warn("RefreshToken 블랙리스트 추가 실패: {}", e.getMessage());
            }
        });

        // Redis에서 RefreshToken 삭제
        redisTokenService.deleteRefreshToken(username);
    }
}
