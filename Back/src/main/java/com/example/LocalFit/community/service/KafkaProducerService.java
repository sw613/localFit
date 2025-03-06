package com.example.LocalFit.community.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.LocalFit.community.dto.RequestChatMessageDto;
import com.example.LocalFit.community.entity.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
	
	private static final String TOPIC_NAME = "group-chat";  
	private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
	
	public void send(RequestChatMessageDto requestChatMessageDto) {
		ChatMessage chatMessage = requestChatMessageDto.toChatMessage();
		
		try {
		    kafkaTemplate.send(TOPIC_NAME, chatMessage)
		                 .whenComplete((result, ex) -> {
		                	 if(ex == null) {
		                		 log.info("Message sent successfully");
		                	 } else {
		                		 log.error("Message sending failed : {}", ex.getMessage());
		                	 }
		                 });		    

		    
		} catch (Exception e) {
		    log.error("Kafka send exception", e);
		}

	}
}
