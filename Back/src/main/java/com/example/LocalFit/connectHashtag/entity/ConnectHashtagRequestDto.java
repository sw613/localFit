package com.example.LocalFit.connectHashtag.entity;


public class ConnectHashtagRequestDto {
    private Long meetingId;
    private Long hashtagId;

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Long getHashtagId() {
        return hashtagId;
    }

    public void setHashtagId(Long hashtagId) {
        this.hashtagId = hashtagId;
    }

    public ConnectHashtagRequestDto() {
    }

    public ConnectHashtagRequestDto(Long meetingId, Long hashtagId) {
        this.meetingId = meetingId;
        this.hashtagId = hashtagId;
    }
}
