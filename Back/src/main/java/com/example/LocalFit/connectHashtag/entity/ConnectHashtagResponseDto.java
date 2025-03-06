package com.example.LocalFit.connectHashtag.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConnectHashtagResponseDto {
    private Long meetingId;
    private Long hashtagId;
    private String hashtag;
}
