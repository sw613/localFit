package com.example.LocalFit.signupMeeting.entity;

import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SignupMeeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signup_meeting_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    private String greeting;

    @Column(nullable = false)
    boolean isAgree;     // 가입 승인 여부

    public void agreeSignupMeeting() {
        this.isAgree = true;
    }

    public SignupMeetingResponseDto signupMeetingToSignupMeetingResponseDto() {
        return new SignupMeetingResponseDto(this.id, this.user.getId(), this.user.getNickname(), this.meeting.getId(), this.greeting, this.isAgree);
    }
}
