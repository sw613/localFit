package com.example.LocalFit.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ElasticsearchDTO {
    @JsonProperty("tag")
    private String tag;

    @JsonProperty("search_count")
    private int searchCount;
}
