package com.example.LocalFit.community.entity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.LocalFit.community.dto.ResponseChatMessageDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "chat_messages")  // MongoDB의 collection 이름 지정
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {  // mongoDB에 메세지 저장
	@Id
	private ObjectId id;
	private Long roomId;
	private String sender;
	private String content;
	private boolean systemMessage;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private String timestamp;
	
	public ResponseChatMessageDto toChatMessageDto() {
		return ResponseChatMessageDto.builder()
				.id(id)
				.roomId(roomId)
				.sender(sender)
				.content(content)
				.systemMessage(systemMessage)
				.timestamp(timestamp)
				.build();
	}
}
