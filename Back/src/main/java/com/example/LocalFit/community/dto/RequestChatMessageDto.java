package com.example.LocalFit.community.dto;

import com.example.LocalFit.community.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestChatMessageDto {
	private Long roomId;
	private String sender;
	private String content;
	private boolean systemMessage;   // 공지 메세지
	private String timestamp;
	
	public ChatMessage toChatMessage() {
		if(systemMessage) {
			setSender("sysyem");
		}
		
		return ChatMessage.builder()
				.roomId(roomId)
				.sender(sender)
				.content(content)
				.systemMessage(systemMessage)
				.timestamp(timestamp)
				.build();
	}
}