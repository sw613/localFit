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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FacilityQuartzBatchJob extends QuartzJobBean { // 분산 락으로 동시 실행 방지

    private final JobLauncher jobLauncher;

    private final Job exportFacilitiesJob;  // 체육 시설 배치 작업
    private final RedissonClient redisson;


    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String INDEXING_LOCK = "FACILITY_FULL_INDEXING_LOCK";  // 체육 시설 락

    public FacilityQuartzBatchJob(JobLauncher jobLauncher,
                                  @Qualifier("exportFacilityJob") Job exportFacilitiesJob,
                                  RedissonClient redisson) {
        this.jobLauncher = jobLauncher;
        this.exportFacilitiesJob = exportFacilitiesJob;
        this.redisson = redisson;
    }

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {
        if (jobLauncher == null || exportFacilitiesJob == null || redisson == null) {
            log.error("Dependencies not injected properly.");
            return;
        }
        RLock lock = redisson.getLock(INDEXING_LOCK);
        try {
            if (lock.tryLock(0, FULL_INDEX_TTL, TimeUnit.MILLISECONDS)) {
                String today = LocalDateTime.now().toString();
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("jobName", "exportFacilitiesJob")
                        .addString("executionDate", today)
                        .toJobParameters();

                jobLauncher.run(exportFacilitiesJob, jobParameters);

                log.info("Batch Job 'exportFacilitiesJob' successfully executed.");
            } else {
                log.warn("Facility job is already running. Skipping execution.");
            }
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            log.error("Failed to execute Batch Job 'exportFacilitiesJob': {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition was interrupted: {}", e.getMessage(), e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Facility indexing lock released.");
            }
        }
    }
}
