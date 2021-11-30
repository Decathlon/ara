package com.decathlon.ara.purge.scheduler;

import com.decathlon.ara.purge.service.PurgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurgeTaskSchedulerTest {

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private PurgeService purgeService;

    @InjectMocks
    private PurgeTaskScheduler purgeTaskScheduler;

    @Test
    void schedulePurge_cronPurge_whenCronFound() {
        // Given
        String purgeCronValue = "0 0 0 * * *";
        ReflectionTestUtils.setField(purgeTaskScheduler, "purgeCronScheduleValue", purgeCronValue);

        // When

        // Then
        purgeTaskScheduler.schedulePurge();
        var cronTriggerArgumentCaptor = ArgumentCaptor.forClass(CronTrigger.class);
        verify(taskScheduler).schedule(any(), cronTriggerArgumentCaptor.capture());
        var cronTrigger = cronTriggerArgumentCaptor.getValue();
        assertThat(cronTrigger.getExpression()).isEqualTo(purgeCronValue);
    }

    @Test
    void schedulePurge_purge5MinutesAfterStartupEveryday_whenCronNotFound() {
        // Given
        ReflectionTestUtils.setField(purgeTaskScheduler, "purgeCronScheduleValue", null);

        // When

        // Then
        purgeTaskScheduler.schedulePurge();
        var purgeStartDateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        var purgeOccurrenceArgumentCaptor = ArgumentCaptor.forClass(long.class);
        verify(taskScheduler).scheduleWithFixedDelay(any(), purgeStartDateArgumentCaptor.capture(), purgeOccurrenceArgumentCaptor.capture());
        var purgeStartDate = purgeStartDateArgumentCaptor.getValue();
        var purgeOccurrence = purgeOccurrenceArgumentCaptor.getValue();
        var expectedPurgeDate = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(purgeStartDate).isCloseTo(expectedPurgeDate, 1000);
        assertThat(purgeOccurrence).isEqualTo(Duration.ofDays(1).toMillis());

        verify(taskScheduler, never()).schedule(any(), any(CronTrigger.class));
    }

    @Test
    void schedulePurge_purge5MinutesAfterStartupEveryday_whenCronNotValid() {
        // Given
        String anIncorrectPurgeCronValue = "32 42 102 69 59 62";
        ReflectionTestUtils.setField(purgeTaskScheduler, "purgeCronScheduleValue", anIncorrectPurgeCronValue);

        // When

        // Then
        purgeTaskScheduler.schedulePurge();
        var purgeStartDateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        var purgeOccurrenceArgumentCaptor = ArgumentCaptor.forClass(long.class);
        verify(taskScheduler).scheduleWithFixedDelay(any(), purgeStartDateArgumentCaptor.capture(), purgeOccurrenceArgumentCaptor.capture());
        var purgeStartDate = purgeStartDateArgumentCaptor.getValue();
        var purgeOccurrence = purgeOccurrenceArgumentCaptor.getValue();
        var expectedPurgeDate = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(purgeStartDate).isCloseTo(expectedPurgeDate, 1000);
        assertThat(purgeOccurrence).isEqualTo(Duration.ofDays(1).toMillis());

        verify(taskScheduler, never()).schedule(any(), any(CronTrigger.class));
    }
}
