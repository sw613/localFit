package com.example.LocalFit.community.config;

import java.util.HashMap;
import java.util.Map;

import com.example.LocalFit.hashtag.entity.HashtagIndexingInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.example.LocalFit.community.entity.ChatMessage;


@Configuration
@EnableKafka
public class KafkaConsumerConfig {
	
	// kafka 클러스트 호스트 및 호스트 번호
	//@Value("${spring.kafka.bootstrap-servers}")
	private static final String BOOTSTRAP_SERVER = "localhost:9092";
	private static final String GROUP_ID ="chat-group";
	private static final String HASHTAG_GROUP_ID = "hashtag-group";
	
	@Bean
	public ConsumerFactory<String , ChatMessage> consumerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		configProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);  // key 역직렬화
		configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);  // value 역직렬화
        
		// ChatMessage가 속한 패키지만 신뢰하는 패키지로 등록
        JsonDeserializer<ChatMessage> deserializer = new JsonDeserializer<>(ChatMessage.class, false);
        deserializer.addTrustedPackages("com.example.LocalFit.community.entity");
		
		return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), deserializer);
	}
	
	@Bean
	// 메세지를 비동기적으로 처리할 수 있게 하는 컨테이너
	public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		
		return factory;
	}

	// elastic kafka

	@Bean
	public ConsumerFactory<String, String> fullIndexingConsumerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "full-indexing-group");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		// 글로벌 설정(application.yml)에서 추가된 속성들을 제거합니다.
		config.remove("spring.deserializer.value.delegate.class");
		config.remove("spring.json.trusted.packages");
		config.remove("spring.json.value.default.type");

		// 생성자에 직접 deserializer 인스턴스를 전달하여 글로벌 설정을 무시합니다.
		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new StringDeserializer());
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> fullIndexingKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(fullIndexingConsumerFactory());
		return factory;
	}



}
