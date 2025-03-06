package com.example.LocalFit.connectHashtag.entity;

import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.meeting.entity.Meeting;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectHashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connect_hashtag_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    @JsonBackReference
    private Meeting meeting;


    @ManyToOne
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    public void updateHashtag(Hashtag hashtag) {
        this.hashtag = hashtag;
    }

    public ConnectHashtagResponseDto connectHashtagToConnectHashtagResponseDto() {
        return new ConnectHashtagResponseDto(this.meeting.getId(), this.hashtag.getId(), this.hashtag.getHashTag());
    }
}
