package com.example.LocalFit.meeting.entity;

import java.util.Arrays;

public enum ApplicationMethod {
    APPLY       // 신청제
    , FIRSTCOME;  // 선착순


    public static ApplicationMethod from(String value) {
        return Arrays.stream(ApplicationMethod.values())
                .filter(m -> m.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid ApplicationMethod: " + value));
    }
}
