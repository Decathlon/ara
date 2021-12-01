package com.decathlon.ara.purge.scheduler;

import com.decathlon.ara.purge.service.PurgeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PurgeTaskScheduler {

    @Value("${ara.purge.schedule:}")
    private String purgeCronScheduleValue;

    @NonNull
    private final ThreadPoolTaskScheduler taskScheduler;

    @NonNull
    private final PurgeService purgeService;

    /**
     * Schedule a purge
     */
    @PostConstruct
    public void schedulePurge() {
        log.info("Preparing purge schedule...");
        var runPurge = getPurgeRunnable();
        try {
            var scheduledByCron = new CronTrigger(purgeCronScheduleValue);
            taskScheduler.schedule(runPurge, scheduledByCron);
            log.debug("Purge cron: {}", purgeCronScheduleValue);
        } catch (IllegalArgumentException iae) {
            var in5minutesFromNow = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
            var everyDay = Duration.ofDays(1).toMillis();
            var dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            log.warn("Cron missing or invalid: purge scheduled 5 minutes from now ({}), every day", dateFormat.format(in5minutesFromNow), iae);
            taskScheduler.scheduleWithFixedDelay(runPurge, in5minutesFromNow, everyDay);
        }
    }

    public Runnable getPurgeRunnable() {
        return purgeService::purgeAllProjects;
    }
}
