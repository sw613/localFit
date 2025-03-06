package com.example.LocalFit.community.config;

import java.util.HashMap;
import java.util.Map;

import com.example.LocalFit.hashtag.entity.HashtagIndexingInfo;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.example.LocalFit.community.entity.ChatMessage;

import org.apache.kafka.common.serialization.StringSerializer;

@Configuration
public class KafkaProducerConfig {
	
	// kafka 클러스트 호스트 및 호스트 번호
	//@Value("${spring.kafka.bootstrap-servers}")
	private static final String BOOTSTRAP_SERVER = "localhost:9092";
	
	@Bean
	public ProducerFactory<String, ChatMessage> producerFactory() {   // kafka의 producer인스턴스 생성
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);    // kafka 클러스트 호스트 및 포트 지정
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);   // key : 토픽 이름
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);  // value : 메세지 내용 (직렬화)
		
		return new DefaultKafkaProducerFactory<>(configProps);
	}
	
	@Bean
	public KafkaTemplate<String, ChatMessage> kafkaTemplate() {  // kafka에 메세지를 보낼 때 사용하는 객체
		return new KafkaTemplate<>(producerFactory()); 
	}

	//아래는 인덱싱 용

	@Bean("indexingKafkaTemplate")
	public KafkaTemplate<String, String> indexingKafkaTemplate() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
	}

}
