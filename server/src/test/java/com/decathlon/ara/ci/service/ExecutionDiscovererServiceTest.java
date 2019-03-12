package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.BuildToIndex;
import com.decathlon.ara.ci.fetcher.OldFileSystemFetcher;
import com.decathlon.ara.ci.fetcher.PullFetcher;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.service.SettingService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionDiscovererServiceTest {

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private DateService dateService;

    @Mock
    private CycleDefinitionRepository cycleDefinitionRepository;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private PullFetcher fetcher;

    @Mock
    private FetcherService fetcherService;

    @Mock
    private ExecutionCrawlerService executionCrawlerService;

    @Mock
    private OldFileSystemFetcher oldFileSystemFetcher;
    @Mock
    private SettingService settingService;

    @InjectMocks
    private ExecutionDiscovererService cut;

    private static long timestamp(int day, int hours) {
        final Calendar timestamp = Calendar.getInstance();
        timestamp.set(2018, Calendar.DECEMBER, day, hours, 59, 59);
        timestamp.set(Calendar.MILLISECOND, 999);
        return timestamp.getTimeInMillis();
    }

    @Test
    public void retrieveBuildsToIndex_should_return_initialized_buildToIndex_if_pull_fetcher() throws FetchException {
        // GIVEN
        long projectId = 18;
        CycleDefinition cycleDefinition = new CycleDefinition(1L, projectId, "develop", "day", 0);
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        when(fetcherService.usePullFetcher(projectId)).thenReturn(true);
        when(fetcher.getJobHistory(projectId, "develop", "day")).thenReturn(Arrays.asList(
                new Build().withUrl("1"),
                new Build().withUrl("2")));
        when(araConfiguration.getMaxExecutionDaysToKeep()).thenReturn(-1);
        when(araConfiguration.getMinExecutionsToKeepPerCycle()).thenReturn(-1);
        when(dateService.now()).thenReturn(new Date(timestamp(31, 12)));

        // WHEN
        List<BuildToIndex> buildsToIndex = cut.retrieveBuildsToIndex(cycleDefinition);

        // THEN
        assertThat(buildsToIndex).hasSize(2);
        assertThat(buildsToIndex).withFailMessage("All names are set")
                .allMatch(b -> "day".equals(b.getCycleDefinition().getName()));
        assertThat(buildsToIndex).withFailMessage("All branches are set")
                .allMatch(b -> "develop".equals(b.getCycleDefinition().getBranch()));
        assertThat(buildsToIndex.stream().map(b -> b.getBuild().getUrl())).containsExactly("1", "2");
    }

    @Test
    public void retrieveBuildsToIndex_should_return_empty_list_if_not_pull_fetcher() throws FetchException {
        // GIVEN
        long projectId = 42;
        CycleDefinition cycleDefinition = new CycleDefinition(1L, projectId, "any", "any", 0);
        when(fetcherService.usePullFetcher(projectId)).thenReturn(false);
        // WHEN
        List<BuildToIndex> buildsToIndex = cut.retrieveBuildsToIndex(cycleDefinition);
        // THEN
        assertThat(buildsToIndex).isEmpty();
    }

    @Test
    public void truncateBuilds_should_truncate_days_when_more_favorable() {
        assertTruncateBuilds(1, 1, "1", "2");
    }

    @Test
    public void truncateBuilds_should_truncate_days_when_min_cycle_is_disabled() {
        assertTruncateBuilds(1, -1, "1", "2");
    }

    @Test
    public void truncateBuilds_should_truncate_executions_when_more_favorable() {
        assertTruncateBuilds(1, 3, "1", "2", "3");
    }

    @Test
    public void truncateBuilds_should_truncate_executions_when_days_is_disabled() {
        assertTruncateBuilds(-1, 3, "1", "2", "3");
    }

    @Test
    public void truncateBuilds_should_truncate_nothing_when_lot_of_margin() {
        assertTruncateBuilds(10, 10, "1", "2", "3", "4");
    }

    @Test
    public void truncateBuilds_should_truncate_nothing_when_the_limit() {
        assertTruncateBuilds(3, 4, "1", "2", "3", "4");
    }

    @Test
    public void truncateBuilds_should_truncate_nothing_when_all_is_disabled() {
        assertTruncateBuilds(-1, -1, "1", "2", "3", "4");
    }

    private void assertTruncateBuilds(int maxExecutionDaysToKeep, int minExecutionsToKeepPerCycle, String... expectedUrls) {
        // GIVEN
        when(araConfiguration.getMaxExecutionDaysToKeep()).thenReturn(maxExecutionDaysToKeep);
        when(araConfiguration.getMinExecutionsToKeepPerCycle()).thenReturn(minExecutionsToKeepPerCycle);
        when(dateService.now()).thenReturn(new Date(timestamp(31, 12)));
        List<Build> allBuilds = Arrays.asList(
                new Build().withUrl("1").withTimestamp(timestamp(31, 11)), // Today, one hour ago
                new Build().withUrl("2").withTimestamp(timestamp(30, 13)), // Yesterday with less than 24 hours
                new Build().withUrl("3").withTimestamp(timestamp(30, 11)), // Yesterday with more than 24 hours
                new Build().withUrl("4").withTimestamp(timestamp(29, 11)) // The day before yesterday with more than 24 hours
        );

        // WHEN
        List<Build> truncatedBuilds = cut.truncateBuilds(allBuilds);

        // THEN
        assertThat(truncatedBuilds.stream().map(Build::getUrl)).containsExactly(expectedUrls);
    }

    @Test
    public void filterOutDoneExecutions_should_alter_the_list() {
        // GIVEN
        when(executionRepository.findJobUrls(JobStatus.DONE, Arrays.asList("1", "2")))
                .thenReturn(Collections.singletonList("1"));
        List<BuildToIndex> buildsToIndex = new ArrayList<>();
        buildsToIndex.add(new BuildToIndex().withBuild(new Build().withUrl("1")));
        buildsToIndex.add(new BuildToIndex().withBuild(new Build().withUrl("2")));

        // WHEN
        cut.filterOutDoneExecutions(buildsToIndex);

        // THEN
        assertThat(buildsToIndex.stream().map(b -> b.getBuild().getUrl())).containsExactly("2");
    }

    @Test
    public void index_should_index_each_build_one_by_one_for_parallelization_purpose() {
        // GIVEN
        BuildToIndex build1 = new BuildToIndex();
        BuildToIndex build2 = new BuildToIndex();
        List<BuildToIndex> builds = Arrays.asList(build1, build2);
        ArgumentCaptor<BuildToIndex> argument = ArgumentCaptor.forClass(BuildToIndex.class);
        doNothing().when(executionCrawlerService).crawlInNewTransaction(argument.capture());

        // WHEN
        cut.index(builds);

        // THEN
        assertThat(argument.getAllValues()).containsExactly(build1, build2);
    }

}
