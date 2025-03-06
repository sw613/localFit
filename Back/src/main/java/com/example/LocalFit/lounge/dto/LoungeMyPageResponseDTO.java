package com.example.LocalFit.lounge.dto;

import java.util.List;

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
public class LoungeMyPageResponseDTO {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String intro;

    private Long followerCount;   // 팔로워 수
    private Long followingCount;  // 팔로잉 수

    private List<FeedResponseDTO> feedList;
    
    private boolean isFollowing;
}