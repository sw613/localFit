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
public class AutoCompleteRes {


    @JsonProperty("HashtagId")
    private String hashtagId;

    @JsonProperty("Hashtag")
    private String hashTag;

}
