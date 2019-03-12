package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.ci.fetcher.PullFetcher;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.support.Settings;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FetcherService {

    @Autowired
    public ProjectService projectService;
    @Autowired // No constructor-injection to avoid cyclic-dependencies because SettingService depends on FetcherService
    @Lazy
    private SettingService settingService;
    @Autowired
    private ApplicationContext applicationContext;
    /**
     * Lazy-loaded: to be used through {@link #getFetchers()}.
     */
    private List<Fetcher> fetchers;

    /**
     * Get the fetcher configured for the project (never null: filesystem fetcher is the default one).<br>
     * The fetcher will be used to index executions to the project's Ara history.
     *
     * @param projectId the ID of the project in which to work
     * @return the fetcher service instance configured for the given project
     * @throws NotGonnaHappenException if the given project has an unrecognized fetcherCode (like a custom one).
     */
    public Fetcher get(long projectId) {
        final String fetcherCode = settingService.get(projectId, Settings.EXECUTION_INDEXER);
        for (Fetcher fetcher : getFetchers()) {
            if (fetcherCode.equals(fetcher.getCode())) {
                return fetcher;
            }
        }
        throw new NotGonnaHappenException(
                "Project " + projectId + " has execution indexer " + fetcherCode + " configured, " +
                        "but cannot find the service. " +
                        "Did you deploy the standard ARA version instead of a custom one like before? " +
                        "Or is your custom fetcher class loaded successfully?");
    }

    /**
     * @return the list of all declared fetchers ordered by name
     */
    public List<Fetcher> getFetchers() {
        if (fetchers == null) { // Lazy-loading (thread safe)
            fetchers = applicationContext.getBeansOfType(Fetcher.class)
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(Fetcher::getName))
                    .collect(Collectors.toList());
        }
        return fetchers;
    }

    /**
     * Check if the given project's fetcher implements the {@link PullFetcher} interface.
     *
     * @param projectId the id of the project to check
     * @return true if the given project implements the PullFetcher, false otherwise
     */
    public boolean usePullFetcher(long projectId) {
        return PullFetcher.class.isAssignableFrom(this.get(projectId).getClass());
    }

}
