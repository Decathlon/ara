package com.decathlon.ara.purge.scheduler;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.decathlon.ara.purge.service.PurgeService;

@Component
public class PurgeTaskScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeTaskScheduler.class);

    @Value("${ara.purge.schedule:}")
    private String purgeCronScheduleValue;

    private final ThreadPoolTaskScheduler taskScheduler;

    private final PurgeService purgeService;

    public PurgeTaskScheduler(ThreadPoolTaskScheduler taskScheduler,
            PurgeService purgeService) {
        this.taskScheduler = taskScheduler;
        this.purgeService = purgeService;
    }

    /**
     * Schedule a purge
     */
    @PostConstruct
    public void schedulePurge() {
        LOG.info("Preparing purge schedule...");
        var runPurge = getPurgeRunnable();
        try {
            var scheduledByCron = new CronTrigger(purgeCronScheduleValue);
            taskScheduler.schedule(runPurge, scheduledByCron);
            LOG.debug("Purge cron: {}", purgeCronScheduleValue);
        } catch (IllegalArgumentException iae) {
            var in5minutesFromNow = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
            var everyDay = Duration.ofDays(1).toMillis();
            var dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            LOG.warn("Cron missing or invalid: purge scheduled 5 minutes from now ({}), every day", dateFormat.format(in5minutesFromNow), iae);
            taskScheduler.scheduleWithFixedDelay(runPurge, in5minutesFromNow, everyDay);
        }
    }

    public Runnable getPurgeRunnable() {
        return purgeService::purgeAllProjects;
    }
}
