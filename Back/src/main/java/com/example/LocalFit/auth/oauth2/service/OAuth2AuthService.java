package com.example.LocalFit.auth.oauth2.service;

import com.example.LocalFit.auth.dto.CustomUserDetails;
import com.example.LocalFit.auth.jwt.TokenProvider;
import com.example.LocalFit.auth.service.RedisTokenService;
import com.example.LocalFit.global.CookieUtil;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import com.example.LocalFit.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthService {
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final RedisTokenService redisTokenService;

    public void processOAuthSuccess(CustomUserDetails userDetails,
                                    User user,
                                    HttpServletResponse response) {
        try {
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // 기존 RefreshToken 삭제
            redisTokenService.deleteRefreshToken(user.getEmail());

            String accessToken = tokenProvider.generateAccessToken(newAuth);
            String refreshToken = tokenProvider.generateRefreshToken(newAuth);

            redisTokenService.saveRefreshToken(user.getEmail(), refreshToken);

            cookieUtil.addAccessTokenCookie(response, accessToken);
            cookieUtil.addRefreshTokenCookie(response, refreshToken);

            boolean needsAdditionalInfo = (user.getNickname() == null ||
                    user.getBirth() == null ||
                    user.getGender() == null);

            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth/callback")
                    .queryParam("token", accessToken)
                    .queryParam("needsAdditionalInfo", needsAdditionalInfo)
                    .build().toUriString();

            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("OAuth 인증 처리 중 오류 발생", e);
            throw new CustomException(CustomErrorCode.OAUTH_PROCESSING_ERROR);
        }
    }
}