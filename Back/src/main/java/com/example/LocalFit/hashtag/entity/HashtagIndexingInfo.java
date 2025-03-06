package com.example.LocalFit.hashtag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashtagIndexingInfo {
    private Long id;
    private String hashtag;
}
