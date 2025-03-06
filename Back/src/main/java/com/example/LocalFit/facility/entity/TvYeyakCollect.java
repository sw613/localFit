package com.example.LocalFit.facility.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TvYeyakCollect {
    @JsonProperty("list_total_count")
    private int totalCount;  //총 데이터 건수 (정상조회 시 출력됨)

    @JsonProperty("RESULT")
    private Result result;  //요청결과

    @JsonProperty("row")
    private List<Facility> facilities; //데이터
}