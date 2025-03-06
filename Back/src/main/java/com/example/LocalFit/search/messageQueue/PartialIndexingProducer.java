package com.example.LocalFit.search.messageQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartialIndexingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "partial-indexing";

    public void sendIndexingMessage(Object payload) {
        try {
            String message = objectMapper.writeValueAsString(
                    new PartialIndexingMessage(payload)
            );

            kafkaTemplate.send(TOPIC_NAME, message);
            log.info("Kafka로 메시지를 전송했습니다: {}", message);
        } catch (JsonProcessingException e) {
            log.error("색인 메시지 직렬화 실패. 데이터: {}, 오류: {}",
                    payload, e.getMessage());
            throw new RuntimeException("Kafka로 메시지 전송 실패", e);
        }
    }

    @Getter
    private static class PartialIndexingMessage {
        private final Object payload;
        public PartialIndexingMessage(Object payload) {
            this.payload = payload;
        }
    }
}
