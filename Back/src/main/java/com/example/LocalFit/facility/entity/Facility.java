package com.example.LocalFit.facility.entity;


import com.example.LocalFit.global.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
public class Facility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_id")
    Long id;

    @JsonProperty("SVCNM")
    @Column(nullable = false)
    String name;

    @JsonProperty("X")
    @Column(nullable = false)
    String xPos;

    @JsonProperty("Y")
    @Column(nullable = false)
    String yPos;

    @JsonProperty("IMGURL")
    @Column(nullable = false)
    String imageUrl;

    @JsonProperty("MAXCLASSNM")
    String maxClassName;    // 대분류명

    @JsonProperty("MINCLASSNM")
    String groundCategory;  // 소분류명

    @JsonProperty("SVCOPNBGNDT")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
    LocalDateTime serviceBeginDate;  // 서비스개시시작일시

    @JsonProperty("SVCOPNENDDT")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
    LocalDateTime serviceEndDate;  // 서비스개시종료일시

    @JsonProperty("V_MAX")
    String serviceEndTime;  // 서비스이용종료시간

    @JsonProperty("V_MIN")
    String serviceBeginTime;  // 서비스이용시작시간

    @JsonProperty("AREANM")
    String areaName;

    @JsonProperty("PLACENM")
    String placeName;

    public Facility() {}

}
