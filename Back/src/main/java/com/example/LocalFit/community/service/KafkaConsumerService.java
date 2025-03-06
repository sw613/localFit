package com.example.LocalFit.community.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.LocalFit.community.dto.ResponseChatMessageDto;
import com.example.LocalFit.community.entity.ChatMessage;
import com.example.LocalFit.community.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


@Slf4j
@Service
public class KafkaConsumerService {
	
	private static final String TOPIC_NAME = "group-chat";
	private static final String GROUP_ID ="chat-group";

	private final SimpMessagingTemplate template;
	//private final ChatMessageRepository chatMessageRepository;
	//private final ReactiveMongoTemplate reactiveMongoTemplate;
	
	private static final String CHAT_ROOM_MESAAGES = "chatRoom:%s:messages";
	private final RedisTemplate<String, ChatMessage> redisTemplate;
	
	
	public KafkaConsumerService(SimpMessagingTemplate template, 
								//ChatMessageRepository chatMessageRepository,
								//ReactiveMongoTemplate reactiveMongoTemplate,
								RedisTemplate<String, ChatMessage> redisTemplate) {
		this.template = template;
		//this.chatMessageRepository = chatMessageRepository;
		//this.reactiveMongoTemplate = reactiveMongoTemplate;
		this.redisTemplate = redisTemplate;
	}
	
	@KafkaListener(topics = TOPIC_NAME, groupId = GROUP_ID) //  해당 topic에 메시지가 들어오면 메서드 실행 됨
	public void consume(ChatMessage chatMessage) { 
		try {
			String key = String.format(CHAT_ROOM_MESAAGES, chatMessage.getRoomId());
			redisTemplate.opsForList().rightPush(key, chatMessage);  // 들어온 순서대로 저장
			
//			chatMessageRepository.save(chatMessage)
//		    .doOnSuccess(savedMessage -> {
//		        log.info("Message saved: {}", savedMessage);
//		        template.convertAndSend("/sub/chatRoom/" + savedMessage.getRoomId(), savedMessage.toChatMessageDto());
//		    })
//		    .subscribe();
			
			ResponseChatMessageDto dto = chatMessage.toChatMessageDto();
			template.convertAndSend("/sub/chatRoom/" + dto.getRoomId() , dto);	// 해당 채팅방(roomId)으로 메시지 전달
			
		} catch (Exception e) {
			log.error("message processing fail: {}", e.getMessage()); 
		}
	}
	
	
	// 특정 채팅방 메세지 조회
	public List<ResponseChatMessageDto> getChatMessages(Long roomId) {
	    String key = String.format(CHAT_ROOM_MESAAGES, roomId.toString());
	    List<ChatMessage> chatMessages = redisTemplate.opsForList().range(key, 0, -1);

	    return chatMessages.stream()
	            .map(ChatMessage::toChatMessageDto)
	            .collect(Collectors.toList());
//		 return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
//	                .map(ChatMessage::toChatMessageDto) // 🔹 Entity → DTO 변환
//	                .doOnNext(dto -> log.info("Retrieved message: {}", dto));
	}
}

