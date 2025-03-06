package com.example.LocalFit.facility.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityIndexingInfo {
    @JsonProperty("FacilityId")
    private Long id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("MaxClassName")
    private String maxClassName;

    @JsonProperty("GroundCategory")
    private String groundCategory;

    @JsonProperty("AreaName")
    private String areaName;

    @JsonProperty("PlaceName")
    private String placeName;
}
