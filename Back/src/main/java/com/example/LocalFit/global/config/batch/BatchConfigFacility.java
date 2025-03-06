package com.example.LocalFit.global.config.batch;

import com.example.LocalFit.facility.repository.FacilityRepository;
import com.example.LocalFit.hashtag.entity.FailedItem;
import com.example.LocalFit.hashtag.entity.JobStatus;
import com.example.LocalFit.hashtag.repository.FailedItemRepository;
import com.example.LocalFit.search.service.ElasticS3Service;
import com.example.LocalFit.search.utils.CSVGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfigFacility {

    private static final String JOB_NAME = "exportFacilityJob";
    private static final String MANAGER_STEP_NAME = "facilityManagerStep";
    private static final String WORKER_STEP_NAME = "facilityWorkerStep";
    private static final String RETRY_STEP_NAME = "retryFailedItemsStep";
    private static final String KAFKA_STEP_NAME = "sendKafkaMessageStep";
    private static final int CHUNK_SIZE = 2000;
    private static final int PAGE_SIZE = 1000;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_NAME_PREFIX = "Batch-Thread-";

    private final ElasticS3Service elasticS3Service;
    private final CSVGenerator csvGenerator;
    private final FacilityRepository facilityRepository;
    private final FacilityFullIndexingProducer fullIndexingProducer;
    private final PrepareJobListener prepareJobListener;
    private final FailedItemRepository failedItemRepository;

    @Bean(name = "exportFacilityJob")
    public Job exportFacilityJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(prepareJobListener) // 리스너 설정해서 인덱싱한 csv파일 S3에서 삭제
                .start(facilityManagerStep(jobRepository, taskFacilityExecutor(), transactionManager)) // CSV 변환 후 S3 저장
                .next(facilityRetryFailedItemsStep(jobRepository, transactionManager))
                .next(sendFacilityKafkaMessageStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step facilityManagerStep(JobRepository jobRepository, @Qualifier("taskExecutor") TaskExecutor taskExecutor, PlatformTransactionManager transactionManager) {
        return new StepBuilder(MANAGER_STEP_NAME, jobRepository)
                .partitioner(WORKER_STEP_NAME, facilityPartitioner())
                .step(facilityWorkerStep(jobRepository, transactionManager))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step facilityRetryFailedItemsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(RETRY_STEP_NAME, jobRepository)
                .<FailedItem, Long>chunk(100, transactionManager)
                .reader(failedFacilityItemReader())
                .processor(failedFacilityItemProcessor())
                .writer(itemFacilityWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .listener(skipFacilityListener()) // 동일 Listener 재사용
                .build();
    }

    @Bean
    public Partitioner facilityPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            long minId = facilityRepository.findMinId();
            long maxId = facilityRepository.findMaxId();
            long numberOfPartitions = (maxId - minId + CHUNK_SIZE) / CHUNK_SIZE;

            for (int i = 0; i < numberOfPartitions; i++) {
                long start = minId + ((long) i * CHUNK_SIZE);
                long end = Math.min(start + CHUNK_SIZE - 1, maxId);

                ExecutionContext context = new ExecutionContext();
                context.putLong("minId", start);
                context.putLong("maxId", end);
                partitions.put("partition" + i, context);
            }
            return partitions;
        };
    }

    @Bean
    public Step facilityWorkerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(WORKER_STEP_NAME, jobRepository)
                .<Long, Long>chunk(CHUNK_SIZE, transactionManager)
                .reader(eventFacilityReader(null, null))
                .processor(batchFacilityProcessor())
                .writer(csvBatchFacilityWriter())
                .faultTolerant()
                .skip(Exception.class).skipLimit(100)
                .retry(Exception.class).retryLimit(3)
                .listener(skipFacilityListener())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step sendFacilityKafkaMessageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(KAFKA_STEP_NAME, jobRepository)
                .tasklet(sendFacilityKafkaMessageTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet sendFacilityKafkaMessageTasklet() {
        return (contribution, chunkContext) -> {
            List<String> filePaths = elasticS3Service.getFacilityFiles();
            int batchSize = 100;

            for (int i = 0; i < filePaths.size(); i += batchSize) {
                List<String> batch = filePaths.subList(i, Math.min(i + batchSize, filePaths.size()));
                String message = String.join(",", batch);

                boolean isLastMessage = (i + batchSize >= filePaths.size());

                fullIndexingProducer.sendIndexingMessage(message, isLastMessage);
            }

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public ListItemReader<Long> eventFacilityReader(@Value("#{stepExecutionContext['minId']}") Long minId,
                                            @Value("#{stepExecutionContext['maxId']}") Long maxId) {
        List<Long> facilities = facilityRepository.findFacilityIdsByRange(minId, maxId);
        return new ListItemReader<>(facilities);
    }

    @Bean
    @StepScope
    public ItemProcessor<Long, Long> batchFacilityProcessor() {
        return item -> {
            log.info("Processing item: {}", item);
            if (!(item instanceof Long)) {
                throw new IllegalArgumentException("Expected type Long but got " + item.getClass());
            }
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> csvBatchFacilityWriter() {
        return items -> {
            String filePath = String.format("%s.csv", UUID.randomUUID());
            File csvFile = csvGenerator.facilityGenerateCsv((List<Long>) items.getItems(), filePath);
            elasticS3Service.uploadFacilityListInfoFile(csvFile);
            if (!csvFile.delete()) {
                throw new IllegalStateException("Failed to delete CSV file: " + csvFile.getAbsolutePath());
            }
        };
    }

    @Bean
    public SkipListener<Long, Long> skipFacilityListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInWrite(Long item, Throwable t) {
                // 실패 항목을 DB에 기록
                failedItemRepository.save(FailedItem.builder()
                        .itemId(item)
                        .reason(t.getMessage())
                        .status(JobStatus.PENDING)
                        .build());
                log.error("Item failed during write: {}, reason: {}", item, t.getMessage());
            }

            @Override
            public void onSkipInRead(Throwable t) {
                log.error("Item failed during read: {}", t.getMessage());
            }

            @Override
            public void onSkipInProcess(Long item, Throwable t) {
                log.error("Item failed during process: {}, reason: {}", item, t.getMessage());
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<FailedItem> failedFacilityItemReader() {
        // 실패한 항목 데이터를 읽어오는 ItemReader
        return () -> {
            // PENDING 상태의 FailedItem을 조회
            List<FailedItem> failedItems = failedItemRepository.findByStatus(JobStatus.PENDING);
            // 항목이 없으면 null 반환
            return failedItems.isEmpty() ? null : failedItems.remove(0);
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<FailedItem, Long> failedFacilityItemProcessor() {
        return failedItem -> {
            // FailedItem에 itemId가 없으면 예외 발생
            if (failedItem.getItemId() == null) {
                throw new EntityNotFoundException("해당 엔티티 없음");
            }
            log.info("실패한 항목 처리 중: {}", failedItem);
            // 처리 결과로 itemId 반환
            return failedItem.getItemId();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> itemFacilityWriter() {
        // 처리 완료된 항목을 데이터베이스에 업데이트하는 ItemWriter
        return items -> {
            // 처리된 항목 ID 리스트 가져오기
            List<Long> itemIds = (List<Long>) items.getItems();
            // ID 리스트에 해당하는 FailedItem을 한 번에 조회
            List<FailedItem> failedItems = failedItemRepository.findAllByItemIdIn(itemIds);

            // 조회된 항목이 없으면 경고 로그를 남기고 종료
            if (failedItems.isEmpty()) {
                log.warn("주어진 항목 ID에 해당하는 FailedItem이 없습니다: {}", itemIds);
                return;
            }

            // 상태를 SUCCESS로 업데이트
            failedItems.forEach(failedItem -> failedItem.setStatus(JobStatus.SUCCESS));
            // 업데이트된 항목을 한 번에 저장
            failedItemRepository.saveAll(failedItems);
            log.info("총 {}개의 FailedItem이 SUCCESS 상태로 업데이트되었습니다.", failedItems.size());
        };
    }

    @Bean
    public TaskExecutor taskFacilityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int coreCount = Runtime.getRuntime().availableProcessors(); // CPU 코어 수
        executor.setCorePoolSize(coreCount); // 기본 스레드 수
        executor.setMaxPoolSize(coreCount * 4); // 최대 스레드 수
        executor.setQueueCapacity(QUEUE_CAPACITY); // 큐 용량 증가
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setAwaitTerminationSeconds(60); // 종료 대기 시간 설정
        executor.initialize();
        return executor;
    }
}
