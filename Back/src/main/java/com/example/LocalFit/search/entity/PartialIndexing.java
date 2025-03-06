package com.example.LocalFit.search.entity;

import com.example.LocalFit.global.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "partial-indexing")
public class PartialIndexing {
    @Id
    private String id;

    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("HashTagId")
    private String hashTagId;

    @JsonProperty("tag")
    private String tag;

    private boolean indexed;
}