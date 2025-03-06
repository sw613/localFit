package com.example.LocalFit.facility.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FacilityResponse {

    @JsonProperty("tvYeyakCOllect")
    private TvYeyakCollect tvYeyakCollect;

}