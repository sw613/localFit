package com.example.LocalFit.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FacilityElasticsearchDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("maxClassName")
    private String maxClassName;

    @JsonProperty("groundCategory")
    private String groundCategory;

    @JsonProperty("areaName")
    private String areaName;

    @JsonProperty("placeName")
    private String placeName;
}
