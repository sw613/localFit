package com.example.LocalFit.global.exception;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomErrorResponseDto {
    private final int statusCode;
    private final String code;
    private final String message;
}
