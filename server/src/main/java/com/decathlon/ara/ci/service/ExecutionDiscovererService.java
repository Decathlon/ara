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
import com.decathlon.ara.service.support.Settings;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ExecutionDiscovererService {

    @NonNull
    private final AraConfiguration araConfiguration;

    @NonNull
    private final DateService dateService;

    @NonNull
    private final CycleDefinitionRepository cycleDefinitionRepository;

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final FetcherService fetcherService;

    @NonNull
    private final ExecutionCrawlerService executionCrawlerService;

    // TODO SNI Those 2 are to remove after validation of HTTPS Indexer.
    @NonNull
    private final OldFileSystemFetcher oldFileSystemFetcher;
    @NonNull
    private final SettingService settingService;

    @Value("${ara.executionSchedulingEnabled}")
    private boolean executionSchedulingEnabled;

    /**
     * Scheduled very regularly to list all cycle executions to index (running or not yet indexed).
     */
    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${ara.executionSchedulingDelayInMilliseconds}",
            initialDelayString = "${ara.executionSchedulingInitialDelayInMilliseconds}")
    public void run() {
        if (executionSchedulingEnabled) {
            List<BuildToIndex> buildsToIndex = new ArrayList<>();
            // Find all cycles, for all projects: they will be indexed depending of their project settings
            for (CycleDefinition cycleDefinition : cycleDefinitionRepository.findAll()) {
                try {
                    List<BuildToIndex> buildToIndex = retrieveBuildsToIndex(cycleDefinition);
                    // TODO SNI Remove this block after validation of HTTPs indexing.
                    if (("filesystem".equals(settingService.get(cycleDefinition.getProjectId(), Settings.EXECUTION_INDEXER)) ||
                            "old-filesystem".equals(settingService.get(cycleDefinition.getProjectId(), Settings.EXECUTION_INDEXER))) &&
                            buildToIndex.isEmpty()) {
                        final List<Build> builds = oldFileSystemFetcher.getJobHistory(
                                cycleDefinition.getProjectId(),
                                cycleDefinition.getBranch(),
                                cycleDefinition.getName());
                        if (!builds.isEmpty()) {
                            log.warn("Using Old file system Fetcher for {} ( {} builds found ).", cycleDefinition, builds.size());
                        }
                        buildToIndex.addAll(truncateBuilds(builds).stream()
                                .map(build -> new BuildToIndex(cycleDefinition, build))
                                .collect(Collectors.toList()));
                    }
                    buildsToIndex.addAll(buildToIndex);
                } catch (FetchException e) {
                    log.error("Cannot find new jobs to index for cycle {}", cycleDefinition, e);
                }
            }
            filterOutDoneExecutions(buildsToIndex);
            index(buildsToIndex);
        }
    }

    /**
     * Retrieve all the builds to index in Ara for the given cycle definition.
     * <p>
     * If the project linked to the cycle definition use a {@link PullFetcher}, then return an empty list because it
     * means that the indexation is made after each pull.
     *
     * @param cycleDefinition the cycle to query for recent job executions
     * @return the list of job executions for a given cycle, truncated according to configured maximum days and minimum
     * jobs to keep
     * @throws FetchException on any network issue, wrong HTTP response status code (404, 500...) or parsing
     *                        issue
     */
    List<BuildToIndex> retrieveBuildsToIndex(CycleDefinition cycleDefinition) throws FetchException {
        if (!fetcherService.usePullFetcher(cycleDefinition.getProjectId())) {
            return new ArrayList<>();
        }
        final PullFetcher fetcher = (PullFetcher) fetcherService.get(cycleDefinition.getProjectId());
        final List<Build> builds = fetcher.getJobHistory(
                cycleDefinition.getProjectId(),
                cycleDefinition.getBranch(),
                cycleDefinition.getName());
        return truncateBuilds(builds).stream()
                .map(build -> new BuildToIndex(cycleDefinition, build))
                .collect(Collectors.toList());
    }

    /**
     * @param builds the raw list of builds
     * @return the truncated list of builds, according to configured maximum days and minimum jobs to keep (the minimum
     * jobs being more important than the maximum days)
     */
    List<Build> truncateBuilds(final List<Build> builds) {
        final Calendar minDate = Calendar.getInstance();
        minDate.setTime(dateService.now());
        minDate.add(Calendar.DATE, -araConfiguration.getMaxExecutionDaysToKeep());
        long minTimestamp = minDate.getTimeInMillis();

        final int indexOfFirstTooOldJob = IntStream.range(0, builds.size())
                .filter(i -> builds.get(i).getTimestamp() < minTimestamp)
                .findFirst()
                .orElse(-1);

        final int biggestIndexToRemove = Math
                .max(indexOfFirstTooOldJob, araConfiguration.getMinExecutionsToKeepPerCycle());

        if (biggestIndexToRemove <= 0 || biggestIndexToRemove > builds.size()) {
            return builds;
        } else {
            return builds.subList(0, biggestIndexToRemove);
        }
    }

    /**
     * @param buildsToIndex a list of builds to index: will be altered by removing all builds already fully indexed (not
     *                      running anymore)
     */
    void filterOutDoneExecutions(List<BuildToIndex> buildsToIndex) {
        // For jobUrl-based fetchers (eg. continuous integration job URLs)
        final List<String> jobUrlsToQuery = buildsToIndex.stream()
                .map(b -> b.getBuild().getUrl())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (!jobUrlsToQuery.isEmpty()) {
            final List<String> foundJobUrls = executionRepository.findJobUrls(JobStatus.DONE, jobUrlsToQuery);
            buildsToIndex.removeIf(b -> foundJobUrls.contains(b.getBuild().getUrl()));
        }

        // For jobLink-based fetchers (eg. file-system folders; all pointing to continuous integration job URLs)
        final List<String> jobLinksToQuery = buildsToIndex.stream()
                .map(b -> b.getBuild().getLink())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (!jobLinksToQuery.isEmpty()) {
            final List<String> foundJobLinks = executionRepository.findJobLinks(JobStatus.DONE, jobLinksToQuery);
            buildsToIndex.removeIf(b -> foundJobLinks.contains(b.getBuild().getLink()));
        }
    }

    /**
     * @param builds submits these builds to the thread that will index them in the background, and in independent
     *               transactions
     */
    void index(List<BuildToIndex> builds) {
        for (BuildToIndex build : builds) {
            // Isolate each execution in its own transaction,
            // so a failing execution indexing does not impact the indexing of previous and next executions
            executionCrawlerService.crawlInNewTransaction(build);
        }
    }

}
