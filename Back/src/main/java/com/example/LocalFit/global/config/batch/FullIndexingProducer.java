package com.example.LocalFit.global.config.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FullIndexingProducer {  //Kafka 메시지를 생성하고 전송, 실패한 메시지를 DLQ로 처리하는 역할

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "full-indexing";
    private static final String DLQ_TOPIC_NAME = "full-indexing-dlq";
    private static final int PARTITION_NUMBER = 0;

    // 카프카 인덱싱 템플릿
    public FullIndexingProducer(@Qualifier("indexingKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // 메시지 전송
    public void sendIndexingMessage(String s3UrlList, boolean isLast) {
        try {
            String message = objectMapper.writeValueAsString(
                    new FullIndexingMessage(s3UrlList, isLast)
            );

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, PARTITION_NUMBER, null, message);

            kafkaTemplate.send(record)
                    .thenApply(result -> {
                        log.info("Kafka 메시지 전송 성공: topic={}, partition={}, offset={}, message={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                message);
                        return result;
                    })
                    .exceptionally(ex -> {
                        log.error("Kafka 메시지 전송 실패: {}, 오류: {}", message, ex.getMessage());
                        sendToDLQ(message);
                        return null;
                    });

        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 직렬화 실패. 데이터: {}, 오류: {}", s3UrlList, e.getMessage());
            throw new RuntimeException("Kafka 메시지 직렬화 실패", e);
        }
    }

    private void sendToDLQ(String failedMessage) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(DLQ_TOPIC_NAME, PARTITION_NUMBER, null, failedMessage);
            kafkaTemplate.send(record)
                    .thenAccept(result -> log.info("DLQ 메시지 전송 성공: topic={}, partition={}, offset={}, message={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            failedMessage))
                    .exceptionally(ex -> {
                        log.error("DLQ 메시지 전송 실패: {}, 오류: {}", failedMessage, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("DLQ 메시지 전송 중 예외 발생. 메시지: {}, 오류: {}", failedMessage, e.getMessage());
        }
    }



    @Getter
    private static class FullIndexingMessage {
        private final String s3UrlList;
        private final boolean isLastMessage;

        public FullIndexingMessage(String message, boolean isLastMessage) {
            this.s3UrlList = message;
            this.isLastMessage = isLastMessage;
        }
    }
}
