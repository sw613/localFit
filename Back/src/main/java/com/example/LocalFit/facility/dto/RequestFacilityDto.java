package com.example.LocalFit.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestFacilityDto {
    private String groundCategory;
    private String areaName;
    private String search;
}
