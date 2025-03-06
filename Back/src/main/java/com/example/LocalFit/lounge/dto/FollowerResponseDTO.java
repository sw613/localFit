package com.example.LocalFit.lounge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowerResponseDTO {
    private Long id;
    private String nickname;
    private String profileImageUrl;
}