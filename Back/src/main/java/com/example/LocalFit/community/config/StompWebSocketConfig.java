package com.example.LocalFit.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns("*").withSockJS();  // stomp 접속 주소 url = ws://localhost:8080/ws-stomp
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		//config.enableSimpleBroker("/sub");     // 외부 브로커 사용함
		config.setApplicationDestinationPrefixes("/pub");
	}
	
}
