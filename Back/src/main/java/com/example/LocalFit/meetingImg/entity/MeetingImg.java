package com.example.LocalFit.meetingImg.entity;

import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.meeting.entity.Meeting;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MeetingImg extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_img_id")
    Long id;

    String url;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

}
