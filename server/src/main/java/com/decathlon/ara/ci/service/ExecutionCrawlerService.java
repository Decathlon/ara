package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.BuildToIndex;
import com.decathlon.ara.ci.bean.CountryDeploymentExecution;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.ExecutionTree;
import com.decathlon.ara.ci.bean.NrtExecution;
import com.decathlon.ara.ci.bean.PlatformRule;
import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.ExecutionCompletionRequest;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.postman.model.NewmanParsingResult;
import com.decathlon.ara.postman.service.PostmanService;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.report.service.ExecutedScenarioExtractorService;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.service.ProblemDenormalizationService;
import com.decathlon.ara.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ExecutionCrawlerService {

    private static final String ARTIFACT_RESULT_TXT = "result.txt";

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final ExecutionCompletionRequestRepository executionCompletionRequestRepository;

    @NonNull
    private final CountryRepository countryRepository;

    @NonNull
    private final TypeRepository typeRepository;

    @NonNull
    private final FetcherService fetcherService;

    @NonNull
    private final PostmanService postmanService;

    @NonNull
    private final ExecutedScenarioExtractorService executedScenarioExtractorService;

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final ErrorRepository errorRepository;

    @NonNull
    private final QualityService qualityService;

    @NonNull
    private final QualityEmailService qualityEmailService;

    @NonNull
    private final ProblemDenormalizationService problemDenormalizationService;

    @NonNull
    private final TransactionService transactionService;

    /**
     * Index the execution of a test cycle.<br>
     * Can come from a continuous integration build (with possibly sub-builds).<br>
     * Compared to {@link #crawl(BuildToIndex)}, this version create a new database transaction to isolate this indexing
     * from the rest of the current transaction (if any): a failing execution indexing does not impact the previous or
     * next tasks.
     *
     * @param buildToIndex the description of the build to be indexed as an execution
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crawlInNewTransaction(BuildToIndex buildToIndex) {
        crawl(buildToIndex);
    }

    /**
     * Index the execution of a test cycle.<br>
     * Can come from a continuous integration build (with possibly sub-builds).
     *
     * @param buildToIndex the description of the build to be indexed as an execution
     * @see #crawlInNewTransaction(BuildToIndex) crawlInNewTransaction to render the indexing autonomous
     */
    @Transactional
    public void crawl(BuildToIndex buildToIndex) {
        log.info("Began crawling execution {}/{} for job URL {} and link {}",
                buildToIndex.getCycleDefinition().getBranch(),
                buildToIndex.getCycleDefinition().getName(),
                buildToIndex.getBuild().getUrl(),
                buildToIndex.getBuild().getLink());

        // Exception-handling strategy regarding all network exchanges in this class:
        // * 404 on any file are silent: this is because jobs are not completely executed yet, or are in error and we will display errors in ARA
        // * Other errors (network, parsing, other HTTP response status codes) are thrown as exceptions: they are clearly not expected
        try {
            final long projectId = buildToIndex.getCycleDefinition().getProjectId();
            List<Long> existingErrorIds = getErrorIds(executionRepository.findByProjectIdAndJobUrlOrJobLink(
                    projectId,
                    buildToIndex.getBuild().getUrl(),
                    buildToIndex.getBuild().getLink()));

            final Execution execution = crawlToExecution(buildToIndex);
            // When the returned execution is null, crawlToExecution() already logged a message explaining why
            if (execution != null) {
                final Execution savedExecution = executionRepository.save(execution);

                List<Long> newErrorIds = getErrorIds(savedExecution);
                newErrorIds.removeAll(existingErrorIds);
                if (!newErrorIds.isEmpty()) {
                    final Set<Problem> updatedProblems = errorRepository.autoAssignProblemsToNewErrors(projectId, newErrorIds);
                    problemDenormalizationService.updateFirstAndLastSeenDateTimes(updatedProblems);
                }

                if (savedExecution.getStatus() == JobStatus.DONE) {
                    transactionService.doAfterCommit(() -> safelySendQualityEmail(savedExecution));
                }
            }
        } catch (Exception e) {
            // Catch the generic Exception class to log the full exception
            // Otherwise the @Async's SimpleAsyncUncaughtExceptionHandler would only log the message without stack-trace
            log.error("Error while crawling execution {}/{} job URL {} and link {}",
                    buildToIndex.getCycleDefinition().getBranch(),
                    buildToIndex.getCycleDefinition().getName(),
                    buildToIndex.getBuild().getUrl(),
                    buildToIndex.getBuild().getLink(),
                    e);
        }
        log.info("Ended crawling execution {}/{} job URL {} and link {}",
                buildToIndex.getCycleDefinition().getBranch(),
                buildToIndex.getCycleDefinition().getName(),
                buildToIndex.getBuild().getUrl(),
                buildToIndex.getBuild().getLink());
    }

    /**
     * @param execution send the quality email for this execution without throwing any exception (errors are logged):
     *                  an email failure is not a problem for the remaining of business logic
     */
    private void safelySendQualityEmail(Execution execution) {
        try {
            qualityEmailService.sendQualityEmail(execution.getCycleDefinition().getProjectId(), execution.getId());
        } catch (Exception e) {
            log.error("Uncaught exception while sending quality email (continuing normally)", e);
        }
    }

    List<Long> getErrorIds(Execution execution) {
        if (execution == null) {
            return Collections.emptyList();
        }
        return execution.getRuns().stream()
                .flatMap(run -> run.getExecutedScenarios().stream())
                .flatMap(executedScenario -> executedScenario.getErrors().stream())
                .map(Error::getId)
                .collect(Collectors.toList());
    }

    /**
     * @param buildToIndex for this given execution build to index, create or update the Execution entity fetching
     *                     information from the continuous integration job(s) as needed
     * @return the Execution to be saved (inserted/updated) in database, or null if nothing has to be saved
     * @throws FetchException on any network issue, wrong HTTP response status code (404 if expected to be
     *                        found, 500...) or parsing issue
     */
    Execution crawlToExecution(BuildToIndex buildToIndex) throws FetchException {
        final long projectId = buildToIndex.getCycleDefinition().getProjectId();
        final Fetcher fetcher = fetcherService.get(projectId);

        // Get the execution object to update in database, or create a new entity that will be persisted
        final Execution execution = getOrCreateExecution(projectId, fetcher, buildToIndex);

        // The job is about to be complete and it requested a completion of its indexation?
        // If yes, then delete the flag, so the job can know (as soon as we commit)
        // that we committed the very latest crawled data and the quality status computation is now accurate/definitive
        Optional<ExecutionCompletionRequest> completionRequest =
                (StringUtils.isEmpty(buildToIndex.getBuild().getUrl())
                        ? Optional.empty()
                        : executionCompletionRequestRepository.findById(buildToIndex.getBuild().getUrl()));
        completionRequest.ifPresent(executionCompletionRequestRepository::delete);

        // No work to do if the job is asked to be indexed several times and was already terminated last time it was indexed
        if (execution.getStatus() == JobStatus.DONE) {
            log.info("Cycle-run is already DONE (requested to be indexed several times?): no further indexing");
            return null;
        }

        // Update status and result
        execution.setStatus(toJobStatus(buildToIndex.getBuild()));
        execution.setResult(buildToIndex.getBuild().getResult());
        execution.setDuration(buildToIndex.getBuild().getDuration());
        execution.setEstimatedDuration(buildToIndex.getBuild().getEstimatedDuration());

        // First time we crawl this execution? Create all run & country-deployment children based on what this cycle was supposed to deploy and test
        if (execution.getRuns().isEmpty()) {
            // If file not found, forget about this execution:
            // it is either too soon (will be crawled again later)
            // or it is completely wrong (the cycleDefinition.json is supposed to be archived first in the build: don't bother to index such deeply broken build)
            Optional<CycleDef> formerCycleDefinition = fetcher.getCycleDefinition(projectId, buildToIndex.getBuild());
            if (!formerCycleDefinition.isPresent()) {
                if (execution.getStatus() == JobStatus.DONE || completionRequest.isPresent()) {
                    log.info("Cycle-run's cycle-definition JSON not found in done job (cycle deeply broken): indexing it as failed");
                    // The job started, did not produce cycleDefinition.json (first thing it does, flawlessly, in theory) and then crashed
                    // Store the failed execution job, to not repeatedly crawl it every minutes for nothing
                    execution.setBlockingValidation(Boolean.FALSE); // We really do not know
                    registerAfterCommitExecutionCleanUp(projectId, fetcher, execution);
                    return execution;
                }
                log.info("Cycle-run's cycle-definition JSON not found (too soon?): not indexing it yet");
                return null;
            }
            // Will fail if a country or type is unknown (caller will log it, so someone will be able to fix the application configuration)
            initializeExecutionHierarchy(execution, formerCycleDefinition.get());
        }

        // Download advancement of the job hierarchy of this execution and update each run and country-deployment with up to date information
        final ExecutionTree executionTree = fetcher.getTree(projectId, buildToIndex.getBuild());
        updateExecutionHierarchyJobUrls(execution, executionTree);
        crawlNewAvailableRuns(projectId, fetcher, execution.getRuns());
        updateExecutionHierarchyStatuses(execution, executionTree);

        // Mark still-active children as UNAVAILABLE or DONE if this execution is DONE
        finalizeExecutionHierarchy(execution);

        // Update the quality level and scenario-counts (INCOMPLETE at first, it will come to FAILED, WARNING or PASSED once all runs mandatory for the quality computation are done)
        qualityService.computeQuality(execution);

        registerAfterCommitExecutionCleanUp(projectId, fetcher, execution);

        return execution;
    }

    private void registerAfterCommitExecutionCleanUp(long projectId, Fetcher fetcher, Execution execution) {
        transactionService.doAfterCommit(() -> {
            try {
                fetcher.onDoneExecutionIndexingFinished(projectId, execution);
            } catch (FetchException e) {
                log.error("Error while cleaning up execution URL {} and link {}",
                        execution.getJobUrl(), execution.getJobLink(), e);
            }
        });
    }

    /**
     * For runs that have no executed-scenarios yet, now have an URL and are not DONE yet, check if they now have a
     * report.json Cucumber results file, and index it if present.
     *
     * @param projectId the ID of the project in which to work
     * @param fetcher   the fetcher to use to download execution progress for the target project of the runs
     * @param runs      the runs for which to index their report.json (executed-scenarios will be added to them, if any), if
     *                  they became or are still eligible
     */
    void crawlNewAvailableRuns(long projectId, Fetcher fetcher, Collection<Run> runs) {
        for (Run run : runs) {
            if (run.getExecutedScenarios().isEmpty() &&
                    run.getStatus() != JobStatus.DONE &&
                    StringUtils.isNotEmpty(run.getJobUrl()) &&
                    run.getType().getSource() != null) {
                switch (run.getType().getSource().getTechnology()) {
                    case CUCUMBER:
                        crawlCucumberRun(projectId, fetcher, run);
                        break;
                    case POSTMAN:
                        crawlPostmanRun(projectId, fetcher, run);
                        break;
                    default:
                        throw new NotGonnaHappenException(
                                "Trying to crawl a run for which the type's source technology has no indexing mechanism");
                }
            }
        }
    }

    private void crawlPostmanRun(long projectId, Fetcher fetcher, Run run) {
        List<String> newmanReportPaths;
        try {
            newmanReportPaths = fetcher.getNewmanReportPaths(projectId, run);
        } catch (FetchException e) {
            log.info("Cannot download artifact list of {}", run.getJobUrl(), e);
            return;
        }

        // When all collections ran, result.txt is created to indicate it
        // If Newman crashes by executing some collections but not all, mark the run as "not entirely executed" to mark the cycle as "INCOMPLETE" and do not bases quality percentages on partial results
        boolean allCollectionsDidRun = newmanReportPaths.stream().anyMatch(p -> p.endsWith(ARTIFACT_RESULT_TXT));
        if (allCollectionsDidRun || run.getStatus() == JobStatus.DONE) {
            // Do not pollute logs every minute while the currently running NRT job does not produce result.txt yet

            // Each collection is run twice (one for all countries, and one for the given country)
            // Newman reports have no line number for the request, so we generate a unique number for the run
            // (two requests can have the same name and position within sub-folder (all or country) of a collection, so we need a unique number for the whole run)
            AtomicInteger requestPosition = new AtomicInteger(0);

            for (String newmanReportPath : newmanReportPaths) {
                if (newmanReportPath.endsWith(ARTIFACT_RESULT_TXT)) {
                    continue;
                }

                try {
                    indexPostmanReport(projectId, fetcher, run, newmanReportPath, requestPosition);
                } catch (FetchException e) {
                    log.error("There has been an error while indexing {} for run ", newmanReportPath, run.getJobUrl(), e);
                    // TODO Cannot index all collection runs: make the run quality status as INCOMPLETE
                }
            }
        }
    }

    private void indexPostmanReport(long projectId, Fetcher fetcher, Run run, String newmanReportPath, AtomicInteger requestPosition)
            throws FetchException {
        final NewmanParsingResult newmanParsingResult = new NewmanParsingResult();
        try {
            JsonParserConsumer consumer = jsonParser -> postmanService.parse(jsonParser, newmanParsingResult);
            fetcher.streamNewmanResult(projectId, run, newmanReportPath, consumer);
            run.addExecutedScenarios(
                    postmanService.postProcess(run, newmanParsingResult, newmanReportPath, requestPosition));
        } finally {
            // Make sure to delete all temporary files, even if stream thrown an exception in the middle of parsing
            postmanService.deleteTempFiles(newmanParsingResult);
        }
    }

    /**
     * Crawl a Cucumber run job to try to extract ExecutedScenarios for the run.<br>
     * Try to download report.json for the run, and add their executed-scenarios to the run.<br>
     * Do not fail if something goes wrong: it could be better next time (no more network error) or there is nothing to
     * index (the job crashed deeply).
     *
     * @param projectId the ID of the project in which to work
     * @param fetcher   the fetcher to use to download execution result for the target project of the run
     * @param run       the run for which to index its report.json (executed-scenarios will be added to it, if any)
     */
    void crawlCucumberRun(long projectId, Fetcher fetcher, Run run) {
        Optional<List<Feature>> features;
        try {
            features = fetcher.getCucumberReport(projectId, run);
            if (!features.isPresent()) {
                // Do not pollute logs every minute while the currently running NRT job does not produce report.json yet
                return;
            }
        } catch (FetchException e) {
            log.info("Cannot download report.json in {}", run.getJobUrl(), e);
            return;
        }

        Optional<List<String>> stepDefinitions;
        try {
            stepDefinitions = fetcher.getCucumberStepDefinitions(projectId, run);
            if (!stepDefinitions.isPresent()) {
                log.info("Found no stepDefinitions.json in {} (no problem: faking them instead)", run.getJobUrl());
            }
        } catch (FetchException e) {
            log.info("Cannot download stepDefinitions.json in {} (no problem: faking them instead)", run.getJobUrl(), e);
            stepDefinitions = Optional.empty();
        }

        run.addExecutedScenarios(executedScenarioExtractorService.extractExecutedScenarios(
                features.get(),
                stepDefinitions.orElse(new ArrayList<>()),
                run.getJobUrl()));
    }

    /**
     * If the execution is DONE, it won't be crawled anymore: children jobs must be either UNAVAILABLE or DONE (and not
     * PENDING nor RUNNING anymore).
     *
     * @param execution the execution to "terminate" children if it is terminated
     */
    void finalizeExecutionHierarchy(Execution execution) {
        if (execution.getStatus() == JobStatus.DONE) {
            for (CountryDeployment countryDeployment : execution.getCountryDeployments()) {
                if (countryDeployment.getStatus() == JobStatus.PENDING) {
                    countryDeployment.setStatus(JobStatus.UNAVAILABLE);
                } else if (countryDeployment.getStatus() == JobStatus.RUNNING) {
                    countryDeployment.setStatus(JobStatus.DONE);
                }
            }
            for (Run run : execution.getRuns()) {
                if (run.getStatus() == JobStatus.PENDING) {
                    run.setStatus(JobStatus.UNAVAILABLE);
                } else if (run.getStatus() == JobStatus.RUNNING) {
                    run.setStatus(JobStatus.DONE);
                }
            }
        }
    }

    /**
     * Set jobUrl fields on run and country-deployment entities, if they have a matching build in the job hierarchy
     * (builds will be regularly added to the hierarchy as the parent build advances).
     *
     * @param execution     the parent build in which to find runs and country-deployments
     * @param executionTree the tree of actual builds for the given execution
     */
    void updateExecutionHierarchyJobUrls(Execution execution, ExecutionTree executionTree) {
        for (CountryDeployment countryDeployment : execution.getCountryDeployments()) {
            findDeploymentBuild(executionTree, countryDeployment).ifPresent(
                    deploymentBuild -> {
                        countryDeployment.setJobUrl(deploymentBuild.getUrl());
                        countryDeployment.setJobLink(deploymentBuild.getLink());
                        countryDeployment.setStartDateTime(new Date(deploymentBuild.getTimestamp()));
                        countryDeployment.setEstimatedDuration(deploymentBuild.getEstimatedDuration());
                        countryDeployment.setDuration(deploymentBuild.getDuration());
                    });
        }
        for (Run run : execution.getRuns()) {
            findRunJobBuild(executionTree, run).ifPresent(
                    runBuild -> {
                        run.setJobUrl(runBuild.getUrl());
                        run.setJobLink(runBuild.getLink());
                        run.setStartDateTime(new Date(runBuild.getTimestamp()));
                        run.setEstimatedDuration(runBuild.getEstimatedDuration());
                        run.setDuration(runBuild.getDuration());

                        if (StringUtils.isNotEmpty(runBuild.getComment())) {
                            run.setComment(runBuild.getComment());
                        }
                    });
        }
    }

    /**
     * Set status & result fields on run and country-deployment entities, if they have a matching build in the job
     * hierarchy
     * (builds will be regularly added to the hierarchy as the parent build advances).
     *
     * @param execution     the parent build in which to find runs and country-deployments
     * @param executionTree the tree of actual builds for the given execution
     */
    void updateExecutionHierarchyStatuses(Execution execution, ExecutionTree executionTree) {
        for (CountryDeployment countryDeployment : execution.getCountryDeployments()) {
            findDeploymentBuild(executionTree, countryDeployment).ifPresent(deploymentBuild -> {
                countryDeployment.setStatus(toJobStatus(deploymentBuild));
                countryDeployment.setResult(deploymentBuild.getResult());
            });
        }
        for (Run run : execution.getRuns()) {
            findRunJobBuild(executionTree, run).ifPresent(
                    runBuild -> run.setStatus(toJobStatus(runBuild)));
        }
    }

    /**
     * Given the cycle definition used at the time this cycle ran, create run and country-deployment entities that will be
     * populated later.
     *
     * @param execution        the execution in which to initialize children entities
     * @param formerDefinition the cycle definition used at the time the given cycle ran
     */
    void initializeExecutionHierarchy(Execution execution, CycleDef formerDefinition) {
        execution.setBlockingValidation(formerDefinition.isBlockingValidation());
        try {
            execution.setQualityThresholds(objectMapper.writeValueAsString(formerDefinition.getQualityThresholds()));
        } catch (JsonProcessingException e) {
            throw new NotGonnaHappenException("JSON serializing should not have failed when serializing to a String", e);
        }

        final long projectId = execution.getCycleDefinition().getProjectId();
        List<Country> countries = countryRepository.findAllByProjectIdOrderByCode(projectId);
        List<Type> types = typeRepository.findAllByProjectIdOrderByCode(projectId);
        // Create missing Runs and CountryDeployments, if any
        formerDefinition.getPlatformsRules().forEach((platform, countryRules) -> {
            for (PlatformRule countryRule : countryRules) {
                if (countryRule.isEnabled()) {
                    final String countryCode = countryRule.getCountry().toLowerCase();
                    Country country = countries.stream()
                            .filter(c -> countryCode.equals(c.getCode()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryCode));
                    execution.addCountryDeployment(createCountryDeployment(country, platform));
                    execution.addRuns(createRuns(country, platform, countryRule, types));
                }
            }
        });
    }

    /**
     * @param country  the country to be deployed by the build
     * @param platform the platform on which the country was deployed
     * @return an initialized country-deployment entity, in PENDING state, ready to be indexed
     */
    CountryDeployment createCountryDeployment(Country country, String platform) {
        CountryDeployment newCountryDeployment = new CountryDeployment();
        newCountryDeployment.setCountry(country);
        newCountryDeployment.setPlatform(platform);
        newCountryDeployment.setStatus(JobStatus.PENDING);
        return newCountryDeployment;
    }

    /**
     * @param country     the country to be tested by the build
     * @param platform    the platform on which to test this country by running non-regression-tests
     * @param countryRule the definition of what tests (and how) to run for this country
     * @param types       all known run types in database
     * @return a list of runs for each test-type to be run on this country (may be empty)
     */
    List<Run> createRuns(Country country, String platform, PlatformRule countryRule, List<Type> types) {
        List<Run> runs = new ArrayList<>();
        if (StringUtils.isNotEmpty(countryRule.getTestTypes())) {
            for (String typeCode : countryRule.getTestTypes().split(PlatformRule.TEST_TYPES_SEPARATOR)) {
                Type type = types.stream()
                        .filter(t -> typeCode.equals(t.getCode()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Type not found: " + typeCode + ". " +
                                "If the run is not in a format ARA can understand, " +
                                "a type must be configured without source; " +
                                "so ARA understands it's not a mis-configuration, and will not index the run."));
                if (type.getSource() != null) {
                    Run run = new Run();
                    run.setCountry(country);
                    run.setType(type);
                    run.setStatus(JobStatus.PENDING);
                    run.setPlatform(platform);
                    run.setCountryTags(countryRule.getCountryTags());
                    run.setSeverityTags(countryRule.getSeverityTags());
                    run.setIncludeInThresholds(countryRule.isBlockingValidation());
                    runs.add(run);
                }
            }
        }
        return runs;
    }

    /**
     * @param build containing an URL and a result
     * @return PENDING on null build or URL, RUNNING on isBuilding or null result, UNAVAILABLE on NOT_BUILT, DONE otherwise
     */
    JobStatus toJobStatus(Build build) {
        if (build == null || StringUtils.isEmpty(build.getUrl())) {
            return JobStatus.PENDING;
        }
        if (build.isBuilding() || build.getResult() == null) {
            return JobStatus.RUNNING;
        }
        switch (build.getResult()) {
            case ABORTED:
            case FAILURE:
            case SUCCESS:
            case UNSTABLE:
                return JobStatus.DONE;
            case NOT_BUILT:
                return JobStatus.UNAVAILABLE;
            default:
                throw new NotGonnaHappenException("New Result enum value not supported yet in code: " + build.getResult());
        }
    }

    /**
     * @param executionTree all ran builds (up to now, if still running) for the execution of the given run
     * @param run           the run to find in {@code executionTree}
     * @return a matching build for {@code run} in {@code executionTree}
     */
    Optional<Build> findRunJobBuild(ExecutionTree executionTree, Run run) {
        String countryCode = run.getCountry().getCode();
        String typeCode = run.getType().getCode();
        return executionTree.getNonRegressionTests().stream()
                .filter(n -> n.getCountry().equals(countryCode) && n.getType().equals(typeCode))
                .map(NrtExecution::getBuild)
                .findFirst();
    }

    /**
     * @param executionTree     all ran builds (up to now, if still running) for the execution of the given country-deployment
     * @param countryDeployment the country-deployment to find in {@code executionTree}
     * @return a matching build for {@code countryDeployment} in {@code executionTree}
     */
    private Optional<Build> findDeploymentBuild(ExecutionTree executionTree, CountryDeployment countryDeployment) {
        final String countryCode = countryDeployment.getCountry().getCode();
        return executionTree.getDeployedCountries().stream()
                .filter(c -> c.getCountry().equals(countryCode))
                .map(CountryDeploymentExecution::getBuild)
                .findFirst();
    }

    /**
     * @param projectId    the ID of the project in which to work
     * @param fetcher      the fetcher to use to download execution progress for the target project of the execution
     * @param buildToIndex the build to index (branch name, cycle name & job URL)
     * @return the Execution for this job URL, either from existing entity in database (to update it), or created (to insert it)
     */
    Execution getOrCreateExecution(long projectId, Fetcher fetcher, BuildToIndex buildToIndex) throws FetchException {
        fetcher.completeBuildInformation(projectId, buildToIndex.getBuild());
        Execution execution = executionRepository.findByProjectIdAndJobUrlOrJobLink(
                buildToIndex.getCycleDefinition().getProjectId(),
                buildToIndex.getBuild().getUrl(),
                buildToIndex.getBuild().getLink());
        if (execution == null) {
            final Long versionTimestamp = buildToIndex.getBuild().getVersionTimestamp();
            execution = new Execution();
            execution.setName(buildToIndex.getCycleDefinition().getName());
            execution.setBranch(buildToIndex.getCycleDefinition().getBranch());
            execution.setRelease(buildToIndex.getBuild().getRelease());
            execution.setVersion(buildToIndex.getBuild().getVersion());
            execution.setBuildDateTime(versionTimestamp == null ? null : new Date(versionTimestamp));
            execution.setTestDateTime(new Date(buildToIndex.getBuild().getTimestamp()));
            execution.setJobUrl(buildToIndex.getBuild().getUrl());
            execution.setJobLink(buildToIndex.getBuild().getLink());
            execution.setStatus(JobStatus.PENDING);
            execution.setAcceptance(ExecutionAcceptance.NEW);
            execution.setCycleDefinition(buildToIndex.getCycleDefinition());
            execution.setQualityStatus(QualityStatus.INCOMPLETE);
        }
        return execution;
    }

}
