package com.example.LocalFit.search.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialIndexingMessage {
    private Object payload;
    private String operationType;
}
