package com.example.LocalFit.global.config.scheduler;

import org.quartz.*;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzSchedulerConfig {

    private static final String EVENT_JOB_NAME = "eventToCSVJob";
    private static final String FACILITY_JOB_NAME = "facilityToCSVJob";

    @Bean
    public JobDetail eventJobDetail() {
        return JobBuilder.newJob(QuartzBatchJob.class)
                .withIdentity(EVENT_JOB_NAME)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger eventTrigger(JobDetail eventJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(eventJobDetail)
                .withIdentity(EVENT_JOB_NAME + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?"))
                .build();
    }

    @Bean
    public JobDetail facilityJobDetail() {
        return JobBuilder.newJob(FacilityQuartzBatchJob.class)
                .withIdentity(FACILITY_JOB_NAME)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger facilityTrigger(JobDetail facilityJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(facilityJobDetail)
                .withIdentity(FACILITY_JOB_NAME + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                .build();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory quartzJobFactory,
                                                     JobDetail eventJobDetail,
                                                     Trigger eventTrigger,
                                                     JobDetail facilityJobDetail,
                                                     Trigger facilityTrigger) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(quartzJobFactory);
        schedulerFactoryBean.setJobDetails(eventJobDetail, facilityJobDetail);
        schedulerFactoryBean.setTriggers(eventTrigger, facilityTrigger);
        return schedulerFactoryBean;
    }


    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }
}
