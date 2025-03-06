package com.example.LocalFit.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullIndexingMessage {
    private String s3UrlList;
    private boolean isLastMessage;
}
