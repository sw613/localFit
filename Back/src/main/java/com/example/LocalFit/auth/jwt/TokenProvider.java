package com.example.LocalFit.auth.jwt;

import com.example.LocalFit.global.exception.CustomErrorCode;
import com.example.LocalFit.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(Authentication authentication, String subject, long validityTime) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        // 사용자 역할 정보 추가
        claims.put("role", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, "access-token",
                jwtProperties.getAccessTokenValidTime());
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, "refresh-token",
                jwtProperties.getRefreshTokenValidTime());
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getClaimsFromToken(token).getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public boolean isValidToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims.getExpiration().before(new Date())) {
                throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
            }
            return true;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            throw new CustomException(CustomErrorCode.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.TOKEN_VALIDATION_ERROR);
        }
    }

    public long getTokenValidTime(String token) {
        return getClaimsFromToken(token).getExpiration().getTime();
    }
}
