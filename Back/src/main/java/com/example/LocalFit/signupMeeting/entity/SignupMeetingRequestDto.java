package com.example.LocalFit.signupMeeting.entity;


public class SignupMeetingRequestDto {

    private String greeting;
    private Long meetingId;

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }
}
