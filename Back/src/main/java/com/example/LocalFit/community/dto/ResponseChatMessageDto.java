package com.example.LocalFit.community.dto;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseChatMessageDto {
	private ObjectId id;
	private Long roomId;
	private String sender;
	private String content;
	private boolean systemMessage;
	private String timestamp;
}
