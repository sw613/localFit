package com.example.LocalFit.search.messageQueue;

import com.example.LocalFit.search.dto.FullIndexingMessage;
import com.example.LocalFit.search.dto.PartialIndexingMessage;
import com.example.LocalFit.search.service.FacilityIndexingService;
import com.example.LocalFit.search.service.LockingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityIndexingConsumer {
    private final LockingService lockingService;
    private final FacilityIndexingService facilityIndexingService;

    private static final String INDEXING_LOCK = "FACILITY_INDEXING_LOCK";
    private static final BlockingQueue<PartialIndexingMessage> queue = new LinkedBlockingQueue<>();

    @KafkaListener(
            topics = "facility-full-indexing",
            groupId = "facility-full-indexing-group",
            containerFactory = "fullIndexingKafkaListenerContainerFactory"
    )
    public void handleFullIndexing(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FullIndexingMessage fullIndexingMessage = mapper.readValue(message, FullIndexingMessage.class);
            processFullIndexing(fullIndexingMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void processFullIndexing(FullIndexingMessage message) {
        log.info("체육 시설 전체 색인 작업을 시작합니다. Message: {}", message);
        facilityIndexingService.handleFullIndexing(message.getS3UrlList());

        if (message.isLastMessage()) {
            log.info("마지막 메시지입니다. Message: {}", message);
            lockingService.releaseLock(INDEXING_LOCK);
        }

        log.info("체육 시설 전체 색인 작업이 완료되었습니다. Message: {}", message);
    }

    @KafkaListener(
            topics = "facility-full-indexing-dlq",
            groupId = "facility-full-indexing-dlq-group",
            concurrency = "1"
    )
    public void handleFailFullIndexing(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FullIndexingMessage fullIndexingMessage = mapper.readValue(message, FullIndexingMessage.class);
            log.warn("체육 시설 DLQ 메시지: {}", fullIndexingMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
