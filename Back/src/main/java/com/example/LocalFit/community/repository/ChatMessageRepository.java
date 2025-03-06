package com.example.LocalFit.community.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.community.entity.ChatMessage;

import reactor.core.publisher.Flux;


@Repository
public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, Object> {
	Flux<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);
}
