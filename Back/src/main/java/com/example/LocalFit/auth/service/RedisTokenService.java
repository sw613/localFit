package com.example.LocalFit.auth.service;

import com.example.LocalFit.auth.jwt.JwtProperties;
import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    // 충돌 방지용
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";

    @PostConstruct
    public void init() {
        try {
            stringRedisTemplate.opsForValue().set("test", "test", 1, TimeUnit.SECONDS);

            // test 키가 실제로 저장되었는지 확인
            String testValue = stringRedisTemplate.opsForValue().get("test");
            if (testValue == null) {
                throw new CustomException(CustomErrorCode.REDIS_CONNECTION_ERROR);
            }
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.REDIS_CONNECTION_ERROR);
        }
    }

    public void saveRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            stringRedisTemplate.opsForValue().set(
                    key,
                    refreshToken,
                    604800000,  // 7일
                    //jwtProperties.getRefreshTokenValidTime(),
                    TimeUnit.MILLISECONDS
            );

            // 즉시 검증
            String savedToken = stringRedisTemplate.opsForValue().get(key);
            if (savedToken == null) {
                throw new CustomException(CustomErrorCode.REFRESH_TOKEN_SAVE_FAILED);
            }
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_SAVE_FAILED);
        }
    }

    //리프레시 토큰 조회
    public Optional<String> getRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            String value = stringRedisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_READ_ERROR);
        }
    }

    //리프레시 토큰 삭제
    public void deleteRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_DELETE_ERROR);
        }
    }

    //리프레시 토큰 검증
    public boolean validateRefreshToken(String username, String refreshToken) {
        try {
            //블랙리스트 체크
            if (isTokenBlacklisted(refreshToken)) {
                throw new CustomException(CustomErrorCode.TOKEN_BLACKLISTED);
            }

            return getRefreshToken(username)
                    .map(savedToken -> {
                        if (!savedToken.equals(refreshToken)) {
                            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
                        }
                        return true;
                    })
                    .orElseThrow(() -> new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_VALIDATION_ERROR);
        }
    }

    /**
     * 토큰을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 토큰
     * @param expiryTime 토큰 만료 시간(밀리초)
     */
    public void addToBlacklist(String token, long expiryTime) {
        try {
            String key = BLACKLIST_PREFIX + token;
            // 토큰 자체를 키로 사용하고, 값은 간단히 "blacklisted"로 설정
            // 만료 시간은 토큰의 원래 만료 시간으로 설정 (불필요한 저장 공간 사용 방지)
            long ttl = calculateTTL(expiryTime);
            stringRedisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.BLACKLIST_OPERATION_FAILED);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.BLACKLIST_OPERATION_FAILED);
        }
    }

    /**
     * 토큰의 TTL 계산 (현재 시간부터 만료 시간까지)
     * @param expiryTimeMillis 토큰 만료 시간(밀리초)
     * @return 남은 TTL(밀리초)
     */
    private long calculateTTL(long expiryTimeMillis) {
        long now = System.currentTimeMillis();
        long ttl = expiryTimeMillis - now;
        // 이미 만료된 토큰이면 최소 TTL 설정 (예: 1일)
        return ttl > 0 ? ttl : TimeUnit.DAYS.toMillis(1);
    }
}
