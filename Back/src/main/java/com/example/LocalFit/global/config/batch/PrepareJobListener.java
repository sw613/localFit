package com.example.LocalFit.global.config.batch;

import com.example.LocalFit.search.service.ElasticS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PrepareJobListener implements JobExecutionListener { // 배치 작업 전 s3파일을 정, redis에 작업 상태 저장

    private final ElasticS3Service elasticS3Service;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L;
    private static final String FULL_INDEXING_RESERVED = "FULL_INDEXING_RESERVED";

    @Override
    public void beforeJob(JobExecution jobExecution) {

        // Job이 처음 시작할 때만 S3 파일 삭제
        redisTemplate.opsForValue().set(FULL_INDEXING_RESERVED, "true", FULL_INDEX_TTL, TimeUnit.MILLISECONDS);

        String jobName = jobExecution.getJobInstance().getJobName();

        if ("exportEventsJob".equals(jobName)) {
            elasticS3Service.deleteAllElasticData();
        } else if ("exportFacilityJob".equals(jobName)) {
            elasticS3Service.deleteAllFacilityElasticData();
        }
    }


    @Override
    public void afterJob(JobExecution jobExecution) {
        // 이후 작업 할거 있으면 추가
    }
}
