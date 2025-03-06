package com.example.LocalFit.meeting.entity;

import com.example.LocalFit.connectHashtag.entity.ConnectHashtag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingResponseDto {

    private Long meetingId;

    private String meetingTitle;

    private String content;

    private Long numberPeopleMin;

    private Long numberPeopleMax;

    private Long numberPeopleCur;

    private LocalTime meetingTime;

    private Long numberAgeMin;

    private Long numberAgeMax;

    private String applicationMethod;

    private String meetingWeek;

    private String thumbnail;

    private Long userId;

    private Long facilityId;

    private List<ConnectHashtag> connectHashtags;
}
