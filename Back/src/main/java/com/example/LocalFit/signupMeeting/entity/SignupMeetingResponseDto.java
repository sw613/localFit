package com.example.LocalFit.signupMeeting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupMeetingResponseDto {

    private Long id;
    private Long userId;
    private String nickname;
    private Long meetingId;
    private String greeting;
    private boolean isagree;
}
