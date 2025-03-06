package com.example.LocalFit.hashtag.entity;

import com.example.LocalFit.global.BaseEntity;
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
public class Hashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashTag;


    public HashtagResponseDto hashtagToHashtagResponseDto() {
        return new HashtagResponseDto(this.id, this.hashTag);
    }
}

