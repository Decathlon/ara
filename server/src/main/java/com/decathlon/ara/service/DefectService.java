package com.decathlon.ara.service;

import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.ci.service.DateService;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.service.support.Settings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles synchronization of problem statuses with their external defects.<br>
 * Contains only the ARA business-logic.<br>
 * The technical details of how to contact the defect tracking system are handled by
 * {@link DefectAdapter}.
 */
@Slf4j
@Service
public class DefectService {

    final Map<Long, Date> lastFullIndexDates = new ConcurrentHashMap<>();
    final Map<Long, Date> lastIncrementalIndexDates = new ConcurrentHashMap<>();

    @Autowired // No constructor-injection to avoid cyclic-dependencies because SettingService depends on DefectService
    @Lazy
    private SettingService settingService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private DateService dateService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Lazy-loaded: to be used through {@link #getAdapters()}.
     */
    private List<DefectAdapter> adapters;

    /**
     * Get the defect adapter configured for the project, if one such adapter is configured for the project.<br>
     * The defect adapter will be used to contact the bug tracking system and get defect statuses.
     *
     * @param projectId the ID of the project in which to work
     * @return the defect adapter instance configured for the given project
     */
    public Optional<DefectAdapter> getAdapter(long projectId) {
        final String adapterCode = settingService.get(projectId, Settings.DEFECT_INDEXER);
        if (StringUtils.isEmpty(adapterCode)) {
            return Optional.empty();
        }
        for (DefectAdapter adapter : getAdapters()) {
            if (adapterCode.equals(adapter.getCode())) {
                return Optional.of(adapter);
            }
        }
        throw new NotGonnaHappenException(
                "Project " + projectId + " has defect adapter " + adapterCode + " configured, " +
                        "but cannot find the adapter. " +
                        "Did you deploy the standard ARA version instead of a custom one like before? " +
                        "Or is your custom adapter class loaded successfully?");
    }

    /**
     * @return the list of all declared defect adapters ordered by name
     */
    public List<DefectAdapter> getAdapters() {
        if (adapters == null) { // Lazy-loading (thread safe)
            adapters = applicationContext.getBeansOfType(DefectAdapter.class)
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(DefectAdapter::getName))
                    .collect(Collectors.toList());
        }
        return adapters;
    }

    @Scheduled(fixedDelayString = "${ara.defectSyncSchedulingDelayInMilliseconds}",
            initialDelayString = "${ara.defectSyncSchedulingInitialDelayInMilliseconds}")
    @Transactional
    public void updateStatuses() {
        for (Project project : projectRepository.findAllByOrderByName()) {
            getAdapter(project.getId().longValue())
                    .ifPresent(adapter -> updateStatuses(project, adapter));
        }
    }

    void updateStatuses(Project project, DefectAdapter defectAdapter) {
        final Long projectId = project.getId();
        final Date startDate = dateService.now();

        try {
            if (needFullIndexing(projectId, startDate)) {
                fullIndex(project, defectAdapter);
                lastFullIndexDates.put(projectId, startDate);
            } else {
                incrementalIndex(project, defectAdapter, lastIncrementalIndexDates.get(projectId));
            }

            // Only fullIndex() and incrementalIndex() are @Transactional
            // This is to ensure everything went well (no exception before or on commit)
            // before changing the last indexing date
            // (to be sure failed-to-index defects will have a chance to be re-indexed next time)
            lastIncrementalIndexDates.put(projectId, startDate);
        } catch (FetchException e) {
            // Also catch RuntimeException to not impact calling code in case of a faulty DefectAdapter in a custom ARA
            log.error("Failed to index defects of project " + project.getName() + ": " +
                    "will perhaps have a better chance later...", e);
        }
    }

    boolean needFullIndexing(Long projectId, Date now) {
        // Refresh all defect statuses at startup,
        // to avoid any delta, handle deleted defects and to set the SINCE for next incremental indexing
        // + full index every hour to handle deleted defects
        return lastFullIndexDates.get(projectId) == null ||
                lastIncrementalIndexDates.get(projectId) == null ||
                diffHours(now, lastFullIndexDates.get(projectId)) >= 1;
    }

    @Transactional
    public void fullIndex(Project project, DefectAdapter defectAdapter) throws FetchException {
        log.trace("Begin defect full indexing for project {}", project.getName());

        final long projectId = project.getId();
        final List<Problem> problems = problemRepository.findAllByProjectIdAndDefectIdIsNotEmpty(projectId);

        final List<String> defectIds = problems.stream().map(Problem::getDefectId).collect(Collectors.toList());
        final List<Defect> statuses = defectAdapter.getStatuses(projectId, defectIds);
        updateDefectAssignations(problems, statuses);
    }

    @Transactional
    public void incrementalIndex(Project project, DefectAdapter defectAdapter, Date since) throws FetchException {
        log.trace("Begin defect incremental indexing for updates since {} for project {}", since, project.getName());

        final long projectId = project.getId();
        final List<Problem> problems = problemRepository.findAllByProjectIdAndDefectIdIsNotEmpty(projectId);
        incrementalIndex(projectId, defectAdapter, since, problems);
    }

    /**
     * Set the defect-existence of problems with defects to UNKNOWN,
     * and trigger a full indexing for the next defect indexing scheduled updater.
     *
     * @param projectId the ID of the project in which to work
     */
    @Transactional
    public void refreshDefectExistences(long projectId) {
        problemRepository.findByProjectIdAndDefectExistenceIsNotAndDefectIdIsNotNull(projectId, DefectExistence.UNKNOWN)
                .forEach(problem -> problem.setDefectExistence(DefectExistence.UNKNOWN));

        // Flag the project to get new full indexing (indexing is done in another thread)
        transactionService.doAfterCommit(() ->
                lastFullIndexDates.remove(projectId));
    }

    private void incrementalIndex(long projectId, DefectAdapter defectAdapter, Date since, List<Problem> problems) throws FetchException {
        // Get updated/created defects since last successful indexing, and update associated problems, if any
        final List<Problem> problemsToUpdate = new ArrayList<>();
        for (Defect defect : defectAdapter.getChangedDefects(projectId, since)) {
            final Optional<Problem> maybeProblem = problems.stream()
                    .filter(p -> defect.getId().equals(p.getDefectId()))
                    .findFirst();
            if (maybeProblem.isPresent()) {
                Problem problem = maybeProblem.get();
                if (problem.getDefectExistence() != DefectExistence.EXISTS ||
                        problem.getStatus() != defect.getStatus() ||
                        !areEqualDownToSeconds(problem.getClosingDateTime(), defect.getCloseDateTime())) {
                    problem.setDefectExistence(DefectExistence.EXISTS);
                    problem.setStatus(defect.getStatus());
                    problem.setClosingDateTime(defect.getCloseDateTime());
                    problemsToUpdate.add(problem);
                }
            }
        }
        problemRepository.saveAll(problemsToUpdate);

        // Force indexing UNKNOWN for defects assigned while the defect tracking system was down
        final List<Problem> unknownProblems = problems.stream()
                .filter(p -> p.getDefectExistence() == DefectExistence.UNKNOWN)
                .collect(Collectors.toList());
        final List<String> unknownDefectIds = unknownProblems.stream()
                .map(Problem::getDefectId)
                .collect(Collectors.toList());
        if (!unknownDefectIds.isEmpty()) {
            updateDefectAssignations(unknownProblems, defectAdapter.getStatuses(projectId, unknownDefectIds));
        }
    }

    void updateDefectAssignations(List<Problem> problems, List<Defect> statuses) {
        final List<Problem> problemsToUpdate = new ArrayList<>();
        for (Problem problem : problems) {
            final Optional<Defect> defect = statuses.stream()
                    .filter(d -> problem.getDefectId().equals(d.getId()))
                    .findFirst();
            if (defect.isPresent()) {
                if (problem.getDefectExistence() != DefectExistence.EXISTS ||
                        problem.getStatus() != defect.get().getStatus() ||
                        !areEqualDownToSeconds(problem.getClosingDateTime(), defect.get().getCloseDateTime())) {
                    problem.setDefectExistence(DefectExistence.EXISTS);
                    problem.setStatus(defect.get().getStatus());
                    problem.setClosingDateTime(defect.get().getCloseDateTime());
                    problemsToUpdate.add(problem);
                }
            } else {
                if (problem.getDefectExistence() != DefectExistence.NONEXISTENT ||
                        problem.getStatus() != ProblemStatus.OPEN ||
                        problem.getClosingDateTime() != null) {
                    problem.setDefectExistence(DefectExistence.NONEXISTENT);
                    problem.setStatus(ProblemStatus.OPEN); // In case defect was closed and removed: it should be acted on
                    problem.setClosingDateTime(null); // Not CLOSED anymore (if it was)
                    problemsToUpdate.add(problem);
                }
            }
        }

        problemRepository.saveAll(problemsToUpdate);
    }

    /**
     * @param date1 a date (CANNOT be null)
     * @param date2 another date (CANNOT be null)
     * @return the number of complete hours between the two dates
     */
    long diffHours(Date date1, Date date2) {
        return Math.abs(date1.getTime() - date2.getTime()) / (1000 * 60 * 60);
    }

    /**
     * When two dates originate from different systems (can be a Date and Timestamp, with or without milliseconds),
     * compare them down to the millisecond.
     *
     * @param date1 the first date to compare (can be null)
     * @param date2 the second date to compare (can be null)
     * @return true if both dates are null, or equal in all fields except the milliseconds
     */
    boolean areEqualDownToSeconds(Date date1, Date date2) {
        final boolean bothNull = (date1 == null && date2 == null);
        final boolean bothNotNull = (date1 != null && date2 != null);
        return bothNull || (bothNotNull && (date1.getTime() / 1000) == (date2.getTime() / 1000));
    }

}
