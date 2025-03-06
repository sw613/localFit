package com.example.LocalFit.meeting.entity;

import com.example.LocalFit.connectHashtag.entity.ConnectHashtag;
import com.example.LocalFit.facility.entity.Facility;
import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.meetingImg.entity.MeetingImg;
import com.example.LocalFit.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @Column(nullable = false)
    private String meetingTitle;     // 제목

    @Column(nullable = false, length = 1000)
    private String content;          // 내용 - String

    @Column(nullable = false)
    private Long numberPeopleMin;    // 최소인원

    @Column(nullable = false)
    private Long numberPeopleMax;    // 최대인원

    @Column(nullable = false)
    private Long numberPeopleCur;    // 현재인원

    @Column(nullable = false)
    private LocalTime meetingTime;   // 모임 시간

    @Column(nullable = false)
    private Long numberAgeMin;       // 최소 연령

    @Column(nullable = false)
    private Long numberAgeMax;       // 최대 연령

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationMethod applicationMethod;  // 가입 방법

    @Column(nullable = false)
    private String meetingWeek;      // 모임 요일 (ex. "월요일,화요일,수요일,목요일,금요일" )

    private String thumbnail; // 모임 대표이미지(썸네일)

    @OneToMany(mappedBy = "meeting")
    private List<MeetingImg> meetingImages; // 모임 내용 - 이미지 url

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;  // 모임장

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;  // 모임 장소

    @OneToMany(mappedBy = "meeting")
    @JsonManagedReference
    private List<ConnectHashtag> connectHashtag;


    public void updateMeeting(MeetingRequestDto meetingRequestDto) {
        if (meetingRequestDto.getMeetingTitle() != null) this.meetingTitle = meetingRequestDto.getMeetingTitle();
        if (meetingRequestDto.getContent() != null) this.content = meetingRequestDto.getContent();
        if (meetingRequestDto.getNumberPeopleMin() != null)
            this.numberPeopleMin = meetingRequestDto.getNumberPeopleMin();
        if (meetingRequestDto.getNumberPeopleMax() != null /*현재 인원보다 max가 작아지면 안됨*/)
            this.numberPeopleMax = meetingRequestDto.getNumberPeopleMax();
        if (meetingRequestDto.getMeetingTime() != null) this.meetingTime = meetingRequestDto.getMeetingTime();
        if (meetingRequestDto.getNumberAgeMin() != null) this.numberAgeMin = meetingRequestDto.getNumberAgeMin();
        if (meetingRequestDto.getNumberAgeMax() != null) this.numberAgeMax = meetingRequestDto.getNumberAgeMax();
        if (meetingRequestDto.getApplicationMethod() != null)
            this.applicationMethod = ApplicationMethod.from(meetingRequestDto.getApplicationMethod());
        if (meetingRequestDto.getMeetingWeek() != null) this.meetingWeek = meetingRequestDto.getMeetingWeek();
    }

    public void updateConnectHashtag(List<ConnectHashtag> connectHashtagList) {
        this.connectHashtag = connectHashtagList;
    }

    public MeetingResponseDto meetingToMeetingResponseDto() {
        return new MeetingResponseDto(this.id, this.meetingTitle, this.content, this.numberPeopleMin, this.numberPeopleMax, this.numberPeopleCur, this.meetingTime, this.numberAgeMin, this.numberAgeMax, this.applicationMethod.toString(), this.meetingWeek, this.thumbnail, this.user.getId(), this.facility.getId(), this.connectHashtag);
    }

    public void decreaseCurrentPeople() {
        this.numberPeopleCur = getNumberPeopleCur() - 1;
    }

    public void increaseCurrentPeople() {
        this.numberPeopleCur = getNumberPeopleCur() + 1;
    }
}
