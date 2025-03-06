package com.example.LocalFit.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResDto {
    private final String accessToken;
    private final String refreshToken;
}
