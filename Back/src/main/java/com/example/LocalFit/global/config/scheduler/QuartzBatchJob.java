package com.example.LocalFit.global.config.scheduler;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class QuartzBatchJob extends QuartzJobBean {// 분산락으로 동시 실행 방지

    private final JobLauncher jobLauncher;
    private final Job exportEventsJob;
    private final RedissonClient redisson;

    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String INDEXING_LOCK = "FULL_INDEXING_LOCK";

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {
        RLock lock = redisson.getLock(INDEXING_LOCK);
        try { // 락 점유 시작
            if (lock.tryLock(0, FULL_INDEX_TTL, TimeUnit.MILLISECONDS)) {
                String today = LocalDateTime.now().toString(); // 배포시엔 localDate로
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("jobName", "exportEventsJob")
                        .addString("executionDate", today)
                        .toJobParameters();

                jobLauncher.run(exportEventsJob, jobParameters);

                log.info("Batch Job 'exportEventsJob' successfully executed.");
            } else {
                log.warn("Job is already running. Skipping execution.");
            }
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            log.error("Failed to execute Batch Job 'exportEventsJob': {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition was interrupted: {}", e.getMessage(), e);
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released.");
            }
        }
    }
}
