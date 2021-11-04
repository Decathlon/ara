package com.decathlon.ara.purge.scheduler;

import com.decathlon.ara.purge.service.PurgeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class PurgeTaskScheduler {

    @NonNull
    private final ThreadPoolTaskScheduler taskScheduler;

    @NonNull
    private final PurgeService purgeService;

    /**
     * Schedule a purge
     */
    @PostConstruct
    public void scheduleRunnablePurgeAtFixedRate() {
        var in5minutesFromNow = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        var everyDay = Duration.ofDays(1).toMillis();
        var runPurge = getPurgeRunnable();
        taskScheduler.scheduleAtFixedRate(runPurge, in5minutesFromNow, everyDay);
    }

    public Runnable getPurgeRunnable() {
        return purgeService::purgeAllProjects;
    }
}
