package com.example.LocalFit.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoCompleteFacilityRes {

    @JsonProperty("id")
    private String facilityId;
    @JsonProperty("name")
    private String name;
}
