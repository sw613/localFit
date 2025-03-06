package com.example.LocalFit.auth.jwt;

import com.example.LocalFit.auth.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    private final RedisTokenService redisTokenService;
    private final TokenProvider tokenProvider;

    /**
     * 사용자의 모든 토큰을 무효화
     * @param username 사용자 이메일
     */
    public void revokeAllUserTokens(String username) {
        // 리프레시 토큰 가져오기
        Optional<String> refreshToken = redisTokenService.getRefreshToken(username);

        // 리프레시 토큰이 있으면 블랙리스트에 추가
        refreshToken.ifPresent(token -> {
            try {
                long expiry = tokenProvider.getTokenValidTime(token);
                redisTokenService.addToBlacklist(token, expiry);
                log.info("사용자 [{}]의 리프레시 토큰을 블랙리스트에 추가했습니다.", username);
            } catch (Exception e) {
                log.warn("사용자 [{}]의 리프레시 토큰 블랙리스트 추가 실패: {}", username, e.getMessage());
            }
        });

        // Redis에서 RefreshToken 삭제
        redisTokenService.deleteRefreshToken(username);
        log.info("사용자 [{}]의 리프레시 토큰을 삭제했습니다.", username);
    }

    /**
     * 특정 토큰을 블랙리스트에 추가
     * 비밀번호 변경 시 토큰 무효화를 위해 사용
     * @param token 블랙리스트에 추가할 토큰
     */
    public void addTokenToBlacklist(String token) {
        try {
            if (token == null || !tokenProvider.isValidToken(token)) {
                log.warn("블랙리스트에 추가 불가: 토큰이 null이거나 유효하지 않습니다.");
                return;
            }

            long expiry = tokenProvider.getTokenValidTime(token);
            redisTokenService.addToBlacklist(token, expiry);
            log.info("토큰이 블랙리스트에 추가되었습니다.");
        } catch (Exception e) {
            log.warn("토큰 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        return redisTokenService.isTokenBlacklisted(token);
    }
}
