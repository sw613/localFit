package com.example.LocalFit.global.config.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FacilityFullIndexingProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "facility-full-indexing";  // 체육 시설 색인용 Kafka Topic
    private static final String DLQ_TOPIC_NAME = "facility-full-indexing-dlq";  // DLQ (Dead Letter Queue)
    private static final int PARTITION_NUMBER = 0;

    public FacilityFullIndexingProducer(@Qualifier("indexingKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                        ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendIndexingMessage(String s3UrlList, boolean isLast) {
        try {
            String message = objectMapper.writeValueAsString(
                    new FacilityFullIndexingMessage(s3UrlList, isLast)
            );

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, PARTITION_NUMBER, null, message);

            kafkaTemplate.send(record)
                    .thenApply(result -> {
                        log.info("체육 시설 Kafka 메시지 전송 성공: topic={}, partition={}, offset={}, message={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                message);
                        return result;
                    })
                    .exceptionally(ex -> {
                        log.error("체육 시설 Kafka 메시지 전송 실패: {}, 오류: {}", message, ex.getMessage());
                        sendToDLQ(message);
                        return null;
                    });

        } catch (JsonProcessingException e) {
            log.error("체육 시설 Kafka 메시지 직렬화 실패. 데이터: {}, 오류: {}", s3UrlList, e.getMessage());
            throw new RuntimeException("체육 시설 Kafka 메시지 직렬화 실패", e);
        }
    }

    private void sendToDLQ(String failedMessage) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(DLQ_TOPIC_NAME, PARTITION_NUMBER, null, failedMessage);
            kafkaTemplate.send(record)
                    .thenAccept(result -> log.info("체육 시설 DLQ 메시지 전송 성공: topic={}, partition={}, offset={}, message={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            failedMessage))
                    .exceptionally(ex -> {
                        log.error("체육 시설 DLQ 메시지 전송 실패: {}, 오류: {}", failedMessage, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("체육 시설 DLQ 메시지 전송 중 예외 발생. 메시지: {}, 오류: {}", failedMessage, e.getMessage());
        }
    }

    @Getter
    private static class FacilityFullIndexingMessage {
        private final String s3UrlList;
        private final boolean isLastMessage;

        public FacilityFullIndexingMessage(String message, boolean isLastMessage) {
            this.s3UrlList = message;
            this.isLastMessage = isLastMessage;
        }
    }
}
