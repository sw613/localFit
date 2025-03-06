package com.example.LocalFit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.LocalFit.community.entity.ChatMessage;


@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

//    @Value("${spring.data.redis.password}")
//    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
//        redisStandaloneConfiguration.setPassword(password);

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    	RedisTemplate<String, Object> template = new RedisTemplate<>();
    	template.setConnectionFactory(redisConnectionFactory);
    	template.setKeySerializer(new StringRedisSerializer());
    	template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
    	
    	return template;
    }
    
    @Bean
    public RedisTemplate<String, ChatMessage> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ChatMessage> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // opsForList()) 사용 시 setValueSerializer()만 설정하면 저장 & 조회 모두 자동 변환됨
        Jackson2JsonRedisSerializer<ChatMessage> serializer = new Jackson2JsonRedisSerializer<>(ChatMessage.class);
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

}
