package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.BuildToIndex;
import com.decathlon.ara.ci.bean.CountryDeploymentExecution;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.ExecutionTree;
import com.decathlon.ara.ci.bean.NrtExecution;
import com.decathlon.ara.ci.bean.PlatformRule;
import com.decathlon.ara.ci.bean.Result;
import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.ExecutionCompletionRequest;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.domain.enumeration.Technology;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;

import static com.decathlon.ara.util.TestUtil.get;
import static com.decathlon.ara.util.TestUtil.longs;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionCrawlerServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ExecutionCompletionRequestRepository executionCompletionRequestRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private FetcherService fetcherService;

    @Mock
    private Fetcher fetcher;

    @Mock
    private PostmanService postmanService;

    @Mock
    private ExecutedScenarioExtractorService executedScenarioExtractorService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ErrorRepository errorRepository;

    @Mock
    private QualityService qualityService;

    @Mock
    private QualityEmailService qualityEmailService;

    @Mock
    private ProblemDenormalizationService problemDenormalizationService;

    @Mock
    private TransactionService transactionService;

    @Spy
    @InjectMocks
    private ExecutionCrawlerService cut;

    @Test
    public void getErrorIds_should_return_empty_list_if_null() {
        assertThat(cut.getErrorIds(null)).isEmpty();
    }

    @Test
    public void getErrorIds_should_return_error_ids() {
        // GIVEN
        Execution execution = new Execution().withRuns(new HashSet<>(Arrays.asList(
                new Run().withExecutedScenarios(new HashSet<>(Arrays.asList(
                        new ExecutedScenario().withErrors(new HashSet<>(Arrays.asList(
                                new Error().withId(Long.valueOf(1)).withStepLine(1),
                                new Error().withId(Long.valueOf(2)).withStepLine(2)
                        ))).withLine(1),
                        new ExecutedScenario().withErrors(new HashSet<>(Collections.singletonList(
                                new Error().withId(Long.valueOf(3)).withStepLine(1)
                        ))).withLine(2)
                ))).withType(new Type().withCode("api")),
                new Run().withExecutedScenarios(new HashSet<>(Collections.singletonList(
                        new ExecutedScenario().withErrors(new HashSet<>(Arrays.asList(
                                new Error().withId(Long.valueOf(4)).withStepLine(1),
                                new Error().withId(Long.valueOf(5)).withStepLine(2)
                        ))).withLine(1)
                ))).withType(new Type().withCode("firefox"))
        )));

        // WHEN
        List<Long> errorIds = cut.getErrorIds(execution);

        // THEN
        assertThat(errorIds).containsOnly(longs(1, 2, 3, 4, 5));
    }

    @Test
    public void crawlToExecution_on_DONE_execution_should_return_null() throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(new Build().withUrl("url"));
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", null))
                .thenReturn(new Execution().withStatus(JobStatus.DONE));

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(execution).isNull();
    }

    @Test
    public void crawlToExecution_on_new_DONE_execution_with_no_cycleDef_should_return_DONE_INCOMPLETE_execution() throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        final Build build = new Build()
                .withUrl("url")
                .withResult(Result.SUCCESS);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", null))
                .thenReturn(null);
        when(fetcher.getCycleDefinition(projectId, build))
                .thenReturn(Optional.empty());

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE); // Will not be crawled anymore
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(execution.getBlockingValidation()).isFalse(); // We do not know
    }

    @Test(expected = FetchException.class)
    public void crawlToExecution_on_new_execution_with_failing_cycleDef_should_throw_exception()
            throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        final Build build = new Build().withUrl("url").withResult(Result.SUCCESS);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", null))
                .thenReturn(null);
        when(fetcher.getCycleDefinition(projectId, build))
                .thenThrow(new FetchException(new RestClientException(""), ""));

        // WHEN
        cut.crawlToExecution(buildToIndex);
    }

    @Test
    public void crawlToExecution_on_new_execution_should_return_a_new_execution_with_given_properties()
            throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        final Build build = new Build()
                .withUrl("url")
                .withResult(Result.SUCCESS)
                .withDuration(1)
                .withEstimatedDuration(2);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", null))
                .thenReturn(null);
        when(fetcher.getCycleDefinition(projectId, build))
                .thenReturn(Optional.of(new CycleDef()
                        .withPlatformsRules(new HashMap<>())));

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(execution.getDuration()).isEqualTo(1);
        assertThat(execution.getEstimatedDuration()).isEqualTo(2);
    }

    @Test
    public void crawlToExecution_on_new_execution_should_create_runs_and_countryDeployments()
            throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        Map<String, List<PlatformRule>> countryRules = new HashMap<>();
        countryRules.put("any-platform",
                Arrays.asList(
                        new PlatformRule()
                                .withEnabled(true)
                                .withCountry("BE")
                                .withTestTypes("firefox"),
                        new PlatformRule()
                                .withEnabled(false)));
        final Build build = new Build().withUrl("url").withResult(Result.SUCCESS);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", null))
                .thenReturn(null);
        when(fetcher.getCycleDefinition(projectId, build))
                .thenReturn(Optional.of(new CycleDef()
                        .withPlatformsRules(countryRules)));
        when(fetcher.getTree(projectId, build)).thenReturn(
                new ExecutionTree()
                        .withDeployedCountries(Collections.singletonList(
                                new CountryDeploymentExecution()
                                        .withCountry("be")
                                        .withBuild(new Build())))
                        .withNonRegressionTests(Collections.singletonList(
                                new NrtExecution()
                                        .withCountry("be")
                                        .withType("firefox")
                                        .withBuild(new Build()))));
        when(countryRepository.findAllByProjectIdOrderByCode(projectId))
                .thenReturn(Collections.singletonList(new Country().withCode("be")));
        when(typeRepository.findAllByProjectIdOrderByCode(projectId))
                .thenReturn(Collections.singletonList(new Type()
                        .withCode("firefox")
                        .withSource(new Source()
                                .withTechnology(Technology.CUCUMBER))));

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(execution.getCountryDeployments()).hasSize(1);
        assertThat(execution.getRuns()).hasSize(1);
    }

    @Test
    public void crawlToExecution_on_existing_execution_should_update_runs_and_countryDeployments()
            throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        final Build build = new Build()
                .withUrl("url")
                .withLink("link")
                .withResult(Result.SUCCESS);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        final Run run = new Run()
                .withCountry(new Country().withCode("be"))
                .withType(new Type()
                        .withCode("firefox")
                        .withSource(new Source()
                                .withTechnology(Technology.CUCUMBER)));
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "url", "link"))
                .thenReturn(new Execution()
                        .withCountryDeployments(Collections.singleton(
                                new CountryDeployment().withCountry(new Country().withCode("be"))))
                        .withRuns(Collections.singleton(
                                run)));
        when(fetcher.getTree(projectId, build)).thenReturn(
                new ExecutionTree()
                        .withDeployedCountries(Collections.singletonList(
                                new CountryDeploymentExecution()
                                        .withCountry("be")
                                        .withBuild(new Build().withUrl("deploy-url"))))
                        .withNonRegressionTests(Collections.singletonList(
                                new NrtExecution()
                                        .withCountry("be")
                                        .withType("firefox")
                                        .withBuild(new Build().withUrl("run-url")))));
        when(fetcher.getCucumberReport(projectId, run)).thenReturn(Optional.empty());

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getJobUrl()).isEqualTo("deploy-url");
        assertThat(get(execution.getRuns(), 0).getJobUrl()).isEqualTo("run-url");
    }

    @Test
    public void crawlToExecution_should_delete_completion_request_if_present() throws FetchException {
        // GIVEN
        long projectId = 42;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(new Build().withUrl("url"));
        Execution execution = new Execution().withStatus(JobStatus.DONE);
        doReturn(execution).when(cut).getOrCreateExecution(projectId, fetcher, buildToIndex);

        ExecutionCompletionRequest completionRequest = new ExecutionCompletionRequest();
        when(executionCompletionRequestRepository.findById(eq("url"))).thenReturn(Optional.of(completionRequest));

        // WHEN
        cut.crawlToExecution(buildToIndex);

        // THEN
        verify(executionCompletionRequestRepository, times(1)).delete(same(completionRequest));
    }

    @Test
    public void crawlToExecution_should_not_delete_any_completion_request_if_absent() throws FetchException {
        // GIVEN
        long projectId = 42;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(new Build().withUrl("url"));
        Execution execution = new Execution().withStatus(JobStatus.DONE);
        doReturn(execution).when(cut).getOrCreateExecution(projectId, fetcher, buildToIndex);

        when(executionCompletionRequestRepository.findById("url")).thenReturn(Optional.empty());

        // WHEN
        cut.crawlToExecution(buildToIndex);

        // THEN
        verify(executionCompletionRequestRepository, never()).delete(any(ExecutionCompletionRequest.class));
    }

    @Test
    public void crawlToExecution_should_return_incomplete_running_execution_if_completion_request_is_present() throws FetchException {
        // GIVEN
        long projectId = 10;
        when(fetcherService.get(projectId)).thenReturn(fetcher);
        final Build build = new Build().withUrl("url");
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(build);
        when(fetcher.getCycleDefinition(projectId, build))
                .thenReturn(Optional.empty());
        ExecutionCompletionRequest completionRequest = new ExecutionCompletionRequest();
        when(executionCompletionRequestRepository.findById(eq("url")))
                .thenReturn(Optional.of(completionRequest));

        // WHEN
        final Execution execution = cut.crawlToExecution(buildToIndex);

        // THEN
        assertThat(execution.getStatus()).isEqualTo(JobStatus.RUNNING);
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

    @Test
    public void crawlNewAvailableRuns_should_do_nothing_if_executedScenarios_already_present()
            throws FetchException {
        // GIVEN
        long projectId = 1;
        List<Run> runs = Collections.singletonList(
                new Run().withExecutedScenarios(Collections.singleton(new ExecutedScenario())));

        // WHEN
        cut.crawlNewAvailableRuns(projectId, null, runs);

        // THEN
        verify(fetcher, never()).getCucumberReport(eq(projectId), any());
    }

    @Test
    public void crawlNewAvailableRuns_should_do_nothing_if_status_is_done() throws FetchException {
        // GIVEN
        long projectId = 1;
        List<Run> runs = Collections.singletonList(
                new Run().withStatus(JobStatus.DONE));

        // WHEN
        cut.crawlNewAvailableRuns(projectId, null, runs);

        // THEN
        verify(fetcher, never()).getCucumberReport(eq(projectId), any());
    }

    @Test
    public void crawlNewAvailableRuns_should_do_nothing_if_url_is_null() throws FetchException {
        // GIVEN
        long projectId = 1;
        List<Run> runs = Collections.singletonList(
                new Run());

        // WHEN
        cut.crawlNewAvailableRuns(projectId, null, runs);

        // THEN
        verify(fetcher, never()).getCucumberReport(eq(projectId), any());
    }

    @Test
    public void crawlNewAvailableRuns_should_do_nothing_if_url_is_empty() throws FetchException {
        // GIVEN
        long projectId = 1;
        List<Run> runs = Collections.singletonList(
                new Run().withJobUrl(""));

        // WHEN
        cut.crawlNewAvailableRuns(projectId, null, runs);

        // THEN
        verify(fetcher, never()).getCucumberReport(eq(projectId), any());
    }

    @Test
    public void crawlNewAvailableRuns_crawl_run_if_eligible() throws FetchException {
        // GIVEN
        long projectId = 1;
        final Run run = new Run()
                .withJobUrl("url")
                .withType(new Type().withSource(new Source().withTechnology(Technology.CUCUMBER)));
        List<Run> runs = Collections.singletonList(run);
        when(fetcher.getCucumberReport(projectId, run)).thenReturn(Optional.empty());

        // WHEN
        cut.crawlNewAvailableRuns(projectId, fetcher, runs);

        // THEN
        verify(fetcher, only()).getCucumberReport(projectId, run);
    }

    @Test
    public void crawlCucumberRun_should_do_nothing_if_no_report_json_present() throws FetchException {
        // GIVEN
        long projectId = 1;
        Run run = new Run().withJobUrl("url");
        when(fetcher.getCucumberReport(projectId, run)).thenReturn(Optional.empty());

        // WHEN
        cut.crawlCucumberRun(projectId, fetcher, run);

        // THEN
        verify(executedScenarioExtractorService, never()).extractExecutedScenarios(any(), any(), any());
    }

    @Test
    public void crawlCucumberRun_should_do_nothing_if_report_json_failed_to_download() throws FetchException {
        // GIVEN
        long projectId = 1;
        Run run = new Run().withJobUrl("url");
        when(fetcher.getCucumberReport(projectId, run))
                .thenThrow(new FetchException(new RestClientException(""), ""));

        // WHEN
        cut.crawlCucumberRun(projectId, fetcher, run);

        // THEN
        verify(executedScenarioExtractorService, never()).extractExecutedScenarios(any(), any(), any());
    }

    @Test
    public void crawlCucumberRun_should_continue_if_no_stepDefinitions_json_present() throws FetchException {
        // GIVEN
        long projectId = 1;
        Run run = new Run().withJobUrl("url");
        final List<Feature> features = Collections.emptyList();
        when(fetcher.getCucumberReport(projectId, run))
                .thenReturn(Optional.of(features));
        when(fetcher.getCucumberStepDefinitions(projectId, run))
                .thenReturn(Optional.empty());

        // WHEN
        cut.crawlCucumberRun(projectId, fetcher, run);

        // THEN
        verify(executedScenarioExtractorService, only()).extractExecutedScenarios(
                features,
                Collections.emptyList(),
                "url");
    }

    @Test
    public void crawlCucumberRun_should_continue_if_stepDefinitions_json_failed_to_download()
            throws FetchException {
        // GIVEN
        long projectId = 1;
        Run run = new Run().withJobUrl("url");
        final List<Feature> features = Collections.emptyList();
        when(fetcher.getCucumberReport(projectId, run))
                .thenReturn(Optional.of(features));
        when(fetcher.getCucumberStepDefinitions(projectId, run))
                .thenThrow(new FetchException(new RestClientException(""), ""));

        // WHEN
        cut.crawlCucumberRun(projectId, fetcher, run);

        // THEN
        verify(executedScenarioExtractorService, only()).extractExecutedScenarios(
                features,
                Collections.emptyList(),
                "url");
    }

    @Test
    public void crawlCucumberRun_should_extract_scenarios_if_all_files_are_present() throws FetchException {
        // GIVEN
        long projectId = 1;
        Run run = new Run().withJobUrl("url");
        final List<Feature> features = Collections.emptyList();
        final List<String> stepDefinitions = Collections.emptyList();
        when(fetcher.getCucumberReport(projectId, run))
                .thenReturn(Optional.of(features));
        when(fetcher.getCucumberStepDefinitions(projectId, run))
                .thenReturn(Optional.of(stepDefinitions));

        // WHEN
        cut.crawlCucumberRun(projectId, fetcher, run);

        // THEN
        verify(executedScenarioExtractorService, only()).extractExecutedScenarios(
                features,
                stepDefinitions,
                "url");
    }

    @Test
    public void finalizeExecutionHierarchy_should_do_nothing_if_not_done() {
        // GIVEN
        Execution execution = new Execution()
                .withCountryDeployments(Collections.singleton(new CountryDeployment()))
                .withRuns(Collections.singleton(new Run()));

        // WHEN
        cut.finalizeExecutionHierarchy(execution);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getStatus()).isNull();
        assertThat(get(execution.getRuns(), 0).getStatus()).isNull();
    }

    @Test
    public void finalizeExecutionHierarchy_should_change_countryDeployment_status_to_UNAVAILABLE_if_was_PENDING() {
        // GIVEN
        Execution execution = new Execution()
                .withStatus(JobStatus.DONE)
                .withCountryDeployments(
                        Collections.singleton(new CountryDeployment()
                                .withStatus(JobStatus.PENDING)));

        // WHEN
        cut.finalizeExecutionHierarchy(execution);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getStatus()).isEqualTo(JobStatus.UNAVAILABLE);
    }

    @Test
    public void finalizeExecutionHierarchy_should_change_countryDeployment_status_to_DONE_if_was_RUNNING() {
        // GIVEN
        Execution execution = new Execution()
                .withStatus(JobStatus.DONE)
                .withCountryDeployments(
                        Collections.singleton(new CountryDeployment()
                                .withStatus(JobStatus.RUNNING)));

        // WHEN
        cut.finalizeExecutionHierarchy(execution);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getStatus()).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void finalizeExecutionHierarchy_should_change_run_status_to_UNAVAILABLE_if_was_PENDING() {
        // GIVEN
        Execution execution = new Execution()
                .withStatus(JobStatus.DONE)
                .withRuns(
                        Collections.singleton(new Run()
                                .withStatus(JobStatus.PENDING)));

        // WHEN
        cut.finalizeExecutionHierarchy(execution);

        // THEN
        assertThat(get(execution.getRuns(), 0).getStatus()).isEqualTo(JobStatus.UNAVAILABLE);
    }

    @Test
    public void finalizeExecutionHierarchy_should_change_run_status_to_DONE_if_was_RUNNING() {
        // GIVEN
        Execution execution = new Execution()
                .withStatus(JobStatus.DONE)
                .withRuns(
                        Collections.singleton(new Run()
                                .withStatus(JobStatus.RUNNING)));

        // WHEN
        cut.finalizeExecutionHierarchy(execution);

        // THEN
        assertThat(get(execution.getRuns(), 0).getStatus()).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void updateExecutionHierarchyJobUrls_should_update_countryDeployment_jobUrl_and_jobLink() {
        // GIVEN
        Execution execution = new Execution()
                .withCountryDeployments(
                        Collections.singleton(new CountryDeployment()
                                .withCountry(new Country().withCode("be"))));
        ExecutionTree executionTree = new ExecutionTree().withDeployedCountries(Collections.singletonList(
                new CountryDeploymentExecution()
                        .withCountry("be")
                        .withBuild(new Build().withUrl("url").withLink("link"))));

        // WHEN
        cut.updateExecutionHierarchyJobUrls(execution, executionTree);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getJobUrl()).isEqualTo("url");
        assertThat(get(execution.getCountryDeployments(), 0).getJobLink()).isEqualTo("link");
    }

    @Test
    public void updateExecutionHierarchyJobUrls_should_update_run_jobUrl_and_jobLink() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(
                        Collections.singleton(new Run()
                                .withCountry(new Country().withCode("be"))
                                .withType(new Type().withCode("api"))));
        ExecutionTree executionTree = new ExecutionTree().withNonRegressionTests(Collections.singletonList(
                new NrtExecution()
                        .withCountry("be")
                        .withType("api")
                        .withBuild(new Build().withUrl("url").withLink("link"))));

        // WHEN
        cut.updateExecutionHierarchyJobUrls(execution, executionTree);

        // THEN
        assertThat(get(execution.getRuns(), 0).getJobUrl()).isEqualTo("url");
        assertThat(get(execution.getRuns(), 0).getJobLink()).isEqualTo("link");
    }

    @Test
    public void updateExecutionHierarchyStatuses_should_update_countryDeployment_status_and_result() {
        // GIVEN
        Execution execution = new Execution()
                .withCountryDeployments(
                        Collections.singleton(new CountryDeployment()
                                .withCountry(new Country().withCode("be"))));
        ExecutionTree executionTree = new ExecutionTree().withDeployedCountries(Collections.singletonList(
                new CountryDeploymentExecution()
                        .withCountry("be")
                        .withBuild(new Build().withResult(Result.NOT_BUILT))));

        // WHEN
        cut.updateExecutionHierarchyStatuses(execution, executionTree);

        // THEN
        assertThat(get(execution.getCountryDeployments(), 0).getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(get(execution.getCountryDeployments(), 0).getResult()).isEqualTo(Result.NOT_BUILT);
    }

    @Test
    public void updateExecutionHierarchyStatuses_should_update_run_status_and_result() {
        // GIVEN
        Execution execution = new Execution()
                .withRuns(
                        Collections.singleton(new Run()
                                .withCountry(new Country().withCode("be"))
                                .withType(new Type().withCode("api"))));
        ExecutionTree executionTree = new ExecutionTree().withNonRegressionTests(Collections.singletonList(
                new NrtExecution()
                        .withCountry("be")
                        .withType("api")
                        .withBuild(new Build().withResult(Result.NOT_BUILT))));

        // WHEN
        cut.updateExecutionHierarchyStatuses(execution, executionTree);

        // THEN
        assertThat(get(execution.getRuns(), 0).getStatus()).isEqualTo(JobStatus.PENDING);
    }

    @Test
    public void initializeExecutionHierarchy_should_set_blockingValidation() {
        // GIVEN
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition()
                        .withProjectId(-42));
        CycleDef formerDefinition = new CycleDef()
                .withBlockingValidation(true)
                .withPlatformsRules(Collections.emptyMap());

        // WHEN
        cut.initializeExecutionHierarchy(execution, formerDefinition);

        // THEN
        assertThat(execution.getBlockingValidation()).isTrue();
    }

    @Test
    public void initializeExecutionHierarchy_should_initialize_runs_and_countryDeployments() {
        // GIVEN
        long projectId = 42;
        Execution execution = new Execution()
                .withCycleDefinition(new CycleDefinition()
                        .withProjectId(projectId));
        Map<String, List<PlatformRule>> countryRules = new HashMap<>();
        countryRules.put("any-platform",
                Arrays.asList(
                        new PlatformRule()
                                .withEnabled(true)
                                .withCountry("be")
                                .withTestTypes("firefox"),
                        new PlatformRule()
                                .withEnabled(false)));
        CycleDef formerDefinition = new CycleDef()
                .withBlockingValidation(true)
                .withPlatformsRules(countryRules);
        when(countryRepository.findAllByProjectIdOrderByCode(projectId))
                .thenReturn(Collections.singletonList(new Country().withCode("be")));
        when(typeRepository.findAllByProjectIdOrderByCode(projectId))
                .thenReturn(Collections.singletonList(new Type()
                        .withCode("firefox")
                        .withSource(new Source()
                                .withTechnology(Technology.CUCUMBER))));

        // WHEN
        cut.initializeExecutionHierarchy(execution, formerDefinition);

        // THEN
        assertThat(execution.getCountryDeployments()).hasSize(1);
        assertThat(execution.getRuns()).hasSize(1);
    }

    @Test
    public void createCountryDeployment_should_return_a_new_and_initialized_countryDeployment() {
        // GIVEN
        final Country country = new Country().withCode("be");

        // WHEN
        final CountryDeployment countryDeployment = cut.createCountryDeployment(country, "platform");

        // THEN
        assertThat(countryDeployment.getCountry()).isSameAs(country);
        assertThat(countryDeployment.getPlatform()).isEqualTo("platform");
        assertThat(countryDeployment.getStatus()).isEqualTo(JobStatus.PENDING);
    }

    @Test
    public void createRuns_should_return_a_new_and_initialized_run() {
        // GIVEN
        final Country country = new Country().withCode("be");
        PlatformRule rule = new PlatformRule()
                .withTestTypes("firefox")
                .withCountryTags("all,be")
                .withSeverityTags("all")
                .withBlockingValidation(true);
        List<Type> types = Collections.singletonList(new Type()
                .withCode("firefox")
                .withSource(new Source()
                        .withTechnology(Technology.CUCUMBER)));

        // WHEN
        final List<Run> runs = cut.createRuns(country, "platform", rule, types);

        // THEN
        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).getCountry()).isSameAs(country);
        assertThat(runs.get(0).getType()).isSameAs(types.get(0));
        assertThat(runs.get(0).getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(runs.get(0).getPlatform()).isEqualTo("platform");
        assertThat(runs.get(0).getCountryTags()).isEqualTo("all,be");
        assertThat(runs.get(0).getSeverityTags()).isEqualTo("all");
        assertThat(runs.get(0).getIncludeInThresholds()).isTrue();
    }

    @Test
    public void createRuns_should_return_nothing_on_empty_types() {
        // GIVEN
        PlatformRule rule = new PlatformRule().withTestTypes("");

        // WHEN
        final List<Run> runs = cut.createRuns(null, null, rule, null);

        // THEN
        assertThat(runs).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRuns_should_throw_exception_on_unknown_type() {
        // GIVEN
        PlatformRule rule = new PlatformRule().withTestTypes("unknown");
        List<Type> types = Collections.emptyList();

        // WHEN
        cut.createRuns(null, null, rule, types);
    }

    @Test
    public void createRuns_should_return_nothing_on_non_nrt_type() {
        // GIVEN
        PlatformRule rule = new PlatformRule().withTestTypes("performance");
        List<Type> types = Collections.singletonList(new Type()
                .withCode("performance")
                .withSource(null));

        // WHEN
        final List<Run> runs = cut.createRuns(null, null, rule, types);

        // THEN
        assertThat(runs).isEmpty();
    }

    @Test
    public void toJobStatus_should_return_PENDING_on_null_build() {
        assertThat(cut.toJobStatus(null)).isEqualTo(JobStatus.PENDING);
    }

    @Test
    public void toJobStatus_should_return_PENDING_on_null_build_url() {
        assertThat(cut.toJobStatus(new Build())).isEqualTo(JobStatus.PENDING);
    }

    @Test
    public void toJobStatus_should_return_PENDING_on_empty_build_url() {
        assertThat(cut.toJobStatus(new Build().withUrl(""))).isEqualTo(JobStatus.PENDING);
    }

    @Test
    public void toJobStatus_should_return_RUNNING_if_building() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withBuilding(true))).isEqualTo(JobStatus.RUNNING);
    }

    @Test
    public void toJobStatus_should_return_RUNNING_if_no_result() {
        assertThat(cut.toJobStatus(new Build().withUrl("any"))).isEqualTo(JobStatus.RUNNING);
    }

    @Test
    public void toJobStatus_should_return_UNAVAILABLE_if_NOT_BUILT() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withResult(Result.NOT_BUILT)))
                .isEqualTo(JobStatus.UNAVAILABLE);
    }

    @Test
    public void toJobStatus_should_return_DONE_if_ABORTED() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withResult(Result.ABORTED))).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void toJobStatus_should_return_DONE_if_FAILURE() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withResult(Result.FAILURE))).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void toJobStatus_should_return_DONE_if_SUCCESS() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withResult(Result.SUCCESS))).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void toJobStatus_should_return_DONE_if_UNSTABLE() {
        assertThat(cut.toJobStatus(new Build().withUrl("any").withResult(Result.UNSTABLE))).isEqualTo(JobStatus.DONE);
    }

    @Test
    public void findRunJobBuild_should_return_the_requested_job_amongst_several() {
        // GIVEN
        Build build = new Build();
        ExecutionTree executionTree = new ExecutionTree().withNonRegressionTests(Arrays.asList(
                new NrtExecution().withCountry("be").withType("firefox"),
                new NrtExecution().withCountry("cn").withType("firefox"),
                new NrtExecution().withCountry("cn").withType("api").withBuild(build)));
        Run run = new Run()
                .withCountry(new Country().withCode("cn"))
                .withType(new Type().withCode("api"));

        // WHEN
        final Optional<Build> result = cut.findRunJobBuild(executionTree, run);

        // THEN
        assertThat(result.orElse(new Build())).isSameAs(build);
    }

    @Test
    public void findRunJobBuild_should_search_with_web_prepended_to_browser_type() {
        // GIVEN
        Build build = new Build();
        ExecutionTree executionTree = new ExecutionTree().withNonRegressionTests(Collections.singletonList(
                new NrtExecution()
                        .withCountry("us")
                        .withType("web-firefox")
                        .withBuild(build)));
        Run run = new Run()
                .withCountry(new Country().withCode("us"))
                .withType(new Type().withCode("web-firefox").withBrowser(true));

        // WHEN
        final Optional<Build> result = cut.findRunJobBuild(executionTree, run);

        // THEN
        assertThat(result.orElse(new Build())).isSameAs(build);
    }

    @Test
    public void getOrCreateExecution_should_return_the_execution_in_database() throws FetchException {
        // GIVEN
        long projectId = 10;
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(new CycleDefinition().withProjectId(projectId))
                .withBuild(new Build().withUrl("http://the-one/"));
        Execution existingExecution = new Execution();
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "http://the-one/", null))
                .thenReturn(existingExecution);

        // WHEN
        Execution result = cut.getOrCreateExecution(projectId, fetcher, buildToIndex);

        // THEN
        assertThat(result).isSameAs(existingExecution);
    }

    @Test
    public void getOrCreateExecution_should_return_a_new_and_initialized_execution() throws FetchException {
        // GIVEN
        long projectId = 10;
        Date buildDateTime = timestamp(2018, 1, 1, 1, 1, 1);
        Date versionDateTime = new Date();
        final CycleDefinition cycleDefinition = new CycleDefinition()
                .withProjectId(projectId)
                .withName("name")
                .withBranch("branch");
        final Build build = new Build()
                .withLink("/opt/the-one/")
                .withTimestamp(buildDateTime.getTime())
                .withRelease("release")
                .withVersion("version")
                .withVersionTimestamp(Long.valueOf(versionDateTime.getTime()));
        BuildToIndex buildToIndex = new BuildToIndex()
                .withCycleDefinition(cycleDefinition)
                .withBuild(build);
        doAnswer(invocation -> {
            build.setUrl("http://the-one/");
            return null;
        }).when(fetcher).completeBuildInformation(projectId, build);
        when(executionRepository.findByProjectIdAndJobUrlOrJobLink(projectId, "http://the-one/", "/opt/the-one/"))
                .thenReturn(null);

        // WHEN
        Execution result = cut.getOrCreateExecution(projectId, fetcher, buildToIndex);

        // THEN
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getBranch()).isEqualTo("branch");
        assertThat(result.getRelease()).isEqualTo("release");
        assertThat(result.getVersion()).isEqualTo("version");
        assertThat(result.getBuildDateTime()).isEqualTo(versionDateTime);
        assertThat(result.getTestDateTime()).isEqualTo(buildDateTime);
        assertThat(result.getJobUrl()).isEqualTo("http://the-one/");
        assertThat(result.getJobLink()).isEqualTo("/opt/the-one/");
        assertThat(result.getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(result.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(result.getCycleDefinition()).isSameAs(cycleDefinition);
        assertThat(result.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
    }

}
