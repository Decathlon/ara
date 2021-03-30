package com.decathlon.ara.v2.service.migration.execution;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
import com.decathlon.ara.v2.domain.Project;
import com.decathlon.ara.v2.domain.Scenario;
import com.decathlon.ara.v2.domain.*;
import com.decathlon.ara.v2.domain.enumeration.DeploymentValidationStatus;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.IncompleteProjectException;
import com.decathlon.ara.v2.exception.project.ProjectRequiredException;
import com.decathlon.ara.v2.repository.V2BranchRepository;
import com.decathlon.ara.v2.repository.V2DeploymentValidationRepository;
import com.decathlon.ara.v2.repository.V2ScenarioSeverityThresholdRepository;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class V2DeploymentValidationMigrationServiceTest {

    @Mock
    private ExecutionCompletionRequestRepository legacyExecutionCompletionRequestRepository;

    @Mock
    private V2DeploymentValidationRepository migrationDeploymentValidationRepository;
    @Mock
    private V2ScenarioSeverityThresholdRepository migrationScenarioSeverityThresholdRepository;
    @Mock
    private V2BranchRepository migrationBranchRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private V2DeploymentValidationMigrationService deploymentValidationMigrationService;

    @Test
    void migrateDeploymentValidations_throwProjectRequiredException_whenLegacyProjectIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> deploymentValidationMigrationService.migrateDeploymentValidation(
                null,
                mock(Project.class),
                new ArrayList<>(),
                null,
                null
                )
        ).isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateDeploymentValidations_throwIncompleteProjectException_whenLegacyProjectHasNoId() {
        // Given
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> deploymentValidationMigrationService.migrateDeploymentValidation(
                legacyProject,
                mock(Project.class),
                new ArrayList<>(),
                null,
                null
                )
        ).isExactlyInstanceOf(IncompleteProjectException.class);
    }

    @Test
    void migrateDeploymentValidations_throwProjectRequiredException_whenDestinationProjectIsNull() {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);

        // Then
        assertThatThrownBy(() -> deploymentValidationMigrationService.migrateDeploymentValidation(
                legacyProject,
                null,
                new ArrayList<>(),
                null,
                null
                )
        ).isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateDeploymentValidations_returnEmptyList_whenNoLegacyExecutionGiven() throws BusinessException {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);

        // Then
        List<DeploymentValidation> deploymentValidations = deploymentValidationMigrationService.migrateDeploymentValidation(
                legacyProject,
                mock(Project.class),
                null,
                null,
                null
        );
        assertThat(deploymentValidations).isNotNull().isEmpty();
    }

    @Test
    void migrateDeploymentValidations_saveMigratedExecutions_whenLegacyExecutionsFound() throws BusinessException, ParseException, JsonProcessingException {
        // Given
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);
        Project migrationProject = mock(Project.class);

        com.decathlon.ara.domain.Execution legacyExecution1 = mock(com.decathlon.ara.domain.Execution.class);
        com.decathlon.ara.domain.Execution legacyExecution2 = mock(com.decathlon.ara.domain.Execution.class);
        com.decathlon.ara.domain.Execution legacyExecution3 = mock(com.decathlon.ara.domain.Execution.class);
        com.decathlon.ara.domain.Execution legacyExecution4 = mock(com.decathlon.ara.domain.Execution.class);
        com.decathlon.ara.domain.Execution legacyExecution5 = mock(com.decathlon.ara.domain.Execution.class);

        DeploymentValidation migratedDeploymentValidation1 = mock(DeploymentValidation.class);
        DeploymentValidation migratedDeploymentValidation2 = mock(DeploymentValidation.class);
        DeploymentValidation migratedDeploymentValidation3 = mock(DeploymentValidation.class);
        DeploymentValidation migratedDeploymentValidation4 = mock(DeploymentValidation.class);
        DeploymentValidation migratedDeploymentValidation5 = mock(DeploymentValidation.class);

        String mainThresholdsJSON1 = "main thresholds 1 json";
        String overriddenThresholdsJSON1 = "overridden thresholds 1 json";
        String mainThresholdsJSON2 = "main thresholds 2 json";

        Map<String, QualityThreshold> mainThresholds1 = Map.ofEntries(
                entry("sanity-check", new QualityThreshold().withFailure(100).withWarning(100)),
                entry("high", new QualityThreshold().withFailure(95).withWarning(90)),
                entry("medium", new QualityThreshold().withFailure(90).withWarning(70))
        );
        Map<String, QualityThreshold> overriddenThresholds1 = Map.ofEntries(
                entry("sanity-check", new QualityThreshold().withFailure(100).withWarning(95)),
                entry("high", new QualityThreshold().withFailure(90).withWarning(85)),
                entry("medium", new QualityThreshold().withFailure(80).withWarning(50))
        );
        Map<String, QualityThreshold> mainThresholds2 = Map.ofEntries(
                entry("sanity-check", new QualityThreshold().withFailure(90).withWarning(85)),
                entry("high", new QualityThreshold().withFailure(80).withWarning(75)),
                entry("medium", new QualityThreshold().withFailure(75).withWarning(65))
        );

        CycleDefinition legacyCycleDefinition1 = mock(CycleDefinition.class);
        CycleDefinition legacyCycleDefinition2 = mock(CycleDefinition.class);

        Cycle migrationCycle1 = mock(Cycle.class);
        Branch migrationBranch1 = new Branch()
                .withId(new CodeWithProjectId().withCode("branch1").withProject(migrationProject))
                .withCycles(List.of(migrationCycle1));

        Cycle migrationCycle2 = mock(Cycle.class);
        Branch migrationBranch2 = new Branch()
                .withId(new CodeWithProjectId().withCode("branch2").withProject(migrationProject))
                .withCycles(List.of(migrationCycle2));

        Run legacyRun11 = mock(Run.class);
        Run legacyRun12 = mock(Run.class);
        Run legacyRun21 = mock(Run.class);
        Run legacyRun51 = mock(Run.class);
        Run legacyRun52 = mock(Run.class);
        Run legacyRun53 = mock(Run.class);

        Country legacyCountry1 = mock(Country.class);
        Country legacyCountry2 = mock(Country.class);

        Type legacyType1 = mock(Type.class);
        Type legacyType2 = mock(Type.class);
        Type legacyType3 = mock(Type.class);

        Source legacySource1 = mock(Source.class);
        Source legacySource2 = mock(Source.class);
        Source legacySource3 = mock(Source.class);

        Scenario migrationScenario1 = mock(Scenario.class);
        ScenarioVersion migrationScenarioVersion11 = mock(ScenarioVersion.class);
        Scenario migrationScenario2 = mock(Scenario.class);
        ScenarioVersion migrationScenarioVersion21 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion22 = mock(ScenarioVersion.class);
        Scenario migrationScenario3 = mock(Scenario.class);
        ScenarioVersion migrationScenarioVersion31 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion32 = mock(ScenarioVersion.class);
        ScenarioVersion migrationScenarioVersion33 = mock(ScenarioVersion.class);

        List<Scenario> migrationScenarios = List.of(migrationScenario1, migrationScenario2, migrationScenario3);

        ExecutedScenario legacyExecutedScenario111 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario111 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario112 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario112 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario121 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario121 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario122 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario122 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario211 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario211 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario212 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario212 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario213 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario213 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario511 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario511 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario521 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario521 = mock(ExecutedScenario.ExtendedExecutedScenario.class);
        ExecutedScenario legacyExecutedScenario531 = mock(ExecutedScenario.class);
        ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario531 = mock(ExecutedScenario.ExtendedExecutedScenario.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);
        when(legacyExecution1.getJobUrl()).thenReturn("https://company-ci.com/execution-1");
        when(legacyExecution1.getTestDateTime()).thenReturn(dateFormat.parse("01/01/2021 01:10:10"));
        when(legacyExecution1.getAcceptance()).thenReturn(ExecutionAcceptance.DISCARDED);
        when(legacyExecution1.getDiscardReason()).thenReturn("discard reason");
        when(legacyExecution1.getBuildDateTime()).thenReturn(dateFormat.parse("01/01/2021 02:20:20"));
        when(legacyExecution1.getRelease()).thenReturn("release-1");
        when(legacyExecution1.getVersion()).thenReturn("version-1");
        when(legacyExecution1.getQualityThresholds()).thenReturn(mainThresholdsJSON1);
        when(legacyExecution1.getQualityStatus()).thenReturn(QualityStatus.PASSED);
        when(legacyExecution1.getCycleDefinition()).thenReturn(legacyCycleDefinition1);
        when(legacyExecution1.getRuns()).thenReturn(new HashSet<>(Arrays.asList(legacyRun11, legacyRun12)));
        when(legacyExecutionCompletionRequestRepository.existsById("https://company-ci.com/execution-1")).thenReturn(false);
        when(legacyExecution2.getJobUrl()).thenReturn("https://company-ci.com/execution-2");
        when(legacyExecution2.getTestDateTime()).thenReturn(dateFormat.parse("02/02/2021 01:10:10"));
        when(legacyExecution2.getDiscardReason()).thenReturn(null);
        when(legacyExecution2.getBuildDateTime()).thenReturn(dateFormat.parse("02/02/2021 02:20:20"));
        when(legacyExecution2.getRelease()).thenReturn("release-2");
        when(legacyExecution2.getVersion()).thenReturn("version-2");
        when(legacyExecution2.getQualityThresholds()).thenReturn(overriddenThresholdsJSON1);
        when(legacyExecution2.getQualityStatus()).thenReturn(QualityStatus.FAILED);
        when(legacyExecution2.getCycleDefinition()).thenReturn(legacyCycleDefinition1);
        when(legacyExecution2.getRuns()).thenReturn(new HashSet<>(Arrays.asList(legacyRun21)));
        when(legacyExecutionCompletionRequestRepository.existsById("https://company-ci.com/execution-2")).thenReturn(false);
        when(legacyExecution3.getJobUrl()).thenReturn("https://company-ci.com/execution-3");
        when(legacyExecution3.getTestDateTime()).thenReturn(dateFormat.parse("03/03/2021 01:10:10"));
        when(legacyExecution3.getDiscardReason()).thenReturn(null);
        when(legacyExecution3.getBuildDateTime()).thenReturn(dateFormat.parse("03/03/2021 02:20:20"));
        when(legacyExecution3.getRelease()).thenReturn("release-3");
        when(legacyExecution3.getVersion()).thenReturn("version-3");
        when(legacyExecution3.getQualityThresholds()).thenReturn(mainThresholdsJSON1);
        when(legacyExecution3.getQualityStatus()).thenReturn(QualityStatus.INCOMPLETE);
        when(legacyExecution3.getCycleDefinition()).thenReturn(legacyCycleDefinition1);
        when(legacyExecution3.getRuns()).thenReturn(null);
        when(legacyExecutionCompletionRequestRepository.existsById("https://company-ci.com/execution-3")).thenReturn(false);
        when(legacyExecution4.getJobUrl()).thenReturn("https://company-ci.com/execution-4");
        when(legacyExecution4.getTestDateTime()).thenReturn(dateFormat.parse("04/04/2021 01:10:10"));
        when(legacyExecution4.getDiscardReason()).thenReturn(null);
        when(legacyExecution4.getBuildDateTime()).thenReturn(dateFormat.parse("04/04/2021 02:20:20"));
        when(legacyExecution4.getRelease()).thenReturn("release-4");
        when(legacyExecution4.getVersion()).thenReturn("version-4");
        when(legacyExecution4.getQualityThresholds()).thenReturn(mainThresholdsJSON2);
        when(legacyExecution4.getQualityStatus()).thenReturn(QualityStatus.WARNING);
        when(legacyExecution4.getCycleDefinition()).thenReturn(legacyCycleDefinition2);
        when(legacyExecution4.getRuns()).thenReturn(new HashSet<>());
        when(legacyExecutionCompletionRequestRepository.existsById("https://company-ci.com/execution-4")).thenReturn(false);
        when(legacyExecution5.getJobUrl()).thenReturn("https://company-ci.com/execution-5");
        when(legacyExecution5.getTestDateTime()).thenReturn(dateFormat.parse("05/05/2021 01:10:10"));
        when(legacyExecution5.getAcceptance()).thenReturn(ExecutionAcceptance.NEW);
        when(legacyExecution5.getDiscardReason()).thenReturn("some text that should be ignored");
        when(legacyExecution5.getBuildDateTime()).thenReturn(dateFormat.parse("05/05/2021 02:20:20"));
        when(legacyExecution5.getRelease()).thenReturn("release-5");
        when(legacyExecution5.getVersion()).thenReturn("version-5");
        when(legacyExecution5.getQualityThresholds()).thenReturn(mainThresholdsJSON1);
        when(legacyExecution5.getCycleDefinition()).thenReturn(legacyCycleDefinition1);
        when(legacyExecution5.getRuns()).thenReturn(new HashSet<>(Arrays.asList(legacyRun51, legacyRun52, legacyRun53)));
        when(legacyExecutionCompletionRequestRepository.existsById("https://company-ci.com/execution-5")).thenReturn(true);

        when(legacyRun11.getComment()).thenReturn("Comment for run 11");
        when(legacyRun11.getJobUrl()).thenReturn("https://company-ci.com/execution-1/run-11");
        when(legacyRun11.getCountry()).thenReturn(legacyCountry1);
        when(legacyRun11.getType()).thenReturn(legacyType1);
        when(legacyRun11.getPlatform()).thenReturn("platform-2");
        when(legacyRun11.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario111, legacyExecutedScenario112)));
        when(legacyRun12.getComment()).thenReturn("Comment for run 12");
        when(legacyRun12.getJobUrl()).thenReturn("https://company-ci.com/execution-1/run-12");
        when(legacyRun12.getCountry()).thenReturn(legacyCountry1);
        when(legacyRun12.getType()).thenReturn(legacyType2);
        when(legacyRun12.getPlatform()).thenReturn("platform-1");
        when(legacyRun12.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario121, legacyExecutedScenario122)));
        when(legacyRun21.getComment()).thenReturn("Comment for run 21");
        when(legacyRun21.getJobUrl()).thenReturn("https://company-ci.com/execution-2/run-21");
        when(legacyRun21.getCountry()).thenReturn(legacyCountry1);
        when(legacyRun21.getType()).thenReturn(legacyType3);
        when(legacyRun21.getPlatform()).thenReturn("platform-1");
        when(legacyRun21.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario211, legacyExecutedScenario212, legacyExecutedScenario213)));
        when(legacyRun51.getComment()).thenReturn("Comment for run 51");
        when(legacyRun51.getJobUrl()).thenReturn("https://company-ci.com/execution-5/run-51");
        when(legacyRun51.getCountry()).thenReturn(legacyCountry2);
        when(legacyRun51.getType()).thenReturn(legacyType1);
        when(legacyRun51.getPlatform()).thenReturn("platform-3");
        when(legacyRun51.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario511)));
        when(legacyRun52.getComment()).thenReturn("Comment for run 52");
        when(legacyRun52.getJobUrl()).thenReturn("https://company-ci.com/execution-5/run-52");
        when(legacyRun52.getCountry()).thenReturn(legacyCountry2);
        when(legacyRun52.getType()).thenReturn(legacyType2);
        when(legacyRun52.getPlatform()).thenReturn("platform-1");
        when(legacyRun52.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario521)));
        when(legacyRun53.getComment()).thenReturn("Comment for run 53");
        when(legacyRun53.getJobUrl()).thenReturn("https://company-ci.com/execution-5/run-53");
        when(legacyRun53.getCountry()).thenReturn(legacyCountry2);
        when(legacyRun53.getType()).thenReturn(legacyType3);
        when(legacyRun53.getPlatform()).thenReturn("platform-1");
        when(legacyRun53.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenario531)));

        when(legacyExecutedScenario111.getExtendedExecutedScenario("branch1", legacySource1)).thenReturn(legacyExtendedExecutedScenario111);
        when(legacyExtendedExecutedScenario111.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion11));
        when(legacyExecutedScenario111.getCucumberId()).thenReturn("execution-111");
        when(legacyExecutedScenario111.getStartDateTime()).thenReturn(dateFormat.parse("01/01/2021 01:01:01"));
        when(legacyExecutedScenario111.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-111.jpg");
        when(legacyExecutedScenario111.getVideoUrl()).thenReturn("https://company/media/videos/video-111.avi");
        when(legacyExecutedScenario111.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-111");
        when(legacyExecutedScenario111.getLogsUrl()).thenReturn("https://company/logs/trace-111");
        when(legacyExecutedScenario111.getCucumberReportUrl()).thenReturn("https://company/media/other/result-111.html");
        when(legacyExecutedScenario111.getDiffReportUrl()).thenReturn("https://company/logs/diff-111");
        when(legacyExecutedScenario111.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-111");
        when(legacyExecutedScenario111.getApiServer()).thenReturn("server-111");
        when(legacyExecutedScenario111.getSeleniumNode()).thenReturn("comment-111");

        when(legacyExecutedScenario112.getExtendedExecutedScenario("branch1", legacySource1)).thenReturn(legacyExtendedExecutedScenario112);
        when(legacyExtendedExecutedScenario112.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion11));
        when(legacyExecutedScenario112.getCucumberId()).thenReturn("execution-112");
        when(legacyExecutedScenario112.getStartDateTime()).thenReturn(dateFormat.parse("01/01/2021 02:02:02"));
        when(legacyExecutedScenario112.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-112.jpg");
        when(legacyExecutedScenario112.getVideoUrl()).thenReturn("https://company/media/videos/video-112.avi");
        when(legacyExecutedScenario112.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-112");
        when(legacyExecutedScenario112.getLogsUrl()).thenReturn("https://company/logs/trace-112");
        when(legacyExecutedScenario112.getCucumberReportUrl()).thenReturn("https://company/media/other/result-112.html");
        when(legacyExecutedScenario112.getDiffReportUrl()).thenReturn("https://company/logs/diff-112");
        when(legacyExecutedScenario112.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-112");
        when(legacyExecutedScenario112.getApiServer()).thenReturn("server-112");
        when(legacyExecutedScenario112.getSeleniumNode()).thenReturn("comment-112");

        when(legacyExecutedScenario121.getExtendedExecutedScenario("branch1", legacySource2)).thenReturn(legacyExtendedExecutedScenario121);
        when(legacyExtendedExecutedScenario121.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion21));
        when(legacyExecutedScenario121.getCucumberId()).thenReturn("execution-121");
        when(legacyExecutedScenario121.getStartDateTime()).thenReturn(dateFormat.parse("01/01/2021 03:03:03"));
        when(legacyExecutedScenario121.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-121.jpg");
        when(legacyExecutedScenario121.getVideoUrl()).thenReturn("https://company/media/videos/video-121.avi");
        when(legacyExecutedScenario121.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-121");
        when(legacyExecutedScenario121.getLogsUrl()).thenReturn("https://company/logs/trace-121");
        when(legacyExecutedScenario121.getCucumberReportUrl()).thenReturn("https://company/media/other/result-121.html");
        when(legacyExecutedScenario121.getDiffReportUrl()).thenReturn("https://company/logs/diff-121");
        when(legacyExecutedScenario121.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-121");
        when(legacyExecutedScenario121.getApiServer()).thenReturn("server-121");
        when(legacyExecutedScenario121.getSeleniumNode()).thenReturn("comment-121");

        when(legacyExecutedScenario122.getExtendedExecutedScenario("branch1", legacySource2)).thenReturn(legacyExtendedExecutedScenario122);
        when(legacyExtendedExecutedScenario122.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion22));
        when(legacyExecutedScenario122.getCucumberId()).thenReturn("execution-122");
        when(legacyExecutedScenario122.getStartDateTime()).thenReturn(dateFormat.parse("01/01/2021 04:04:04"));
        when(legacyExecutedScenario122.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-122.jpg");
        when(legacyExecutedScenario122.getVideoUrl()).thenReturn("https://company/media/videos/video-122.avi");
        when(legacyExecutedScenario122.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-122");
        when(legacyExecutedScenario122.getLogsUrl()).thenReturn("https://company/logs/trace-122");
        when(legacyExecutedScenario122.getCucumberReportUrl()).thenReturn("https://company/media/other/result-122.html");
        when(legacyExecutedScenario122.getDiffReportUrl()).thenReturn("https://company/logs/diff-122");
        when(legacyExecutedScenario122.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-122");
        when(legacyExecutedScenario122.getApiServer()).thenReturn("server-122");
        when(legacyExecutedScenario122.getSeleniumNode()).thenReturn("comment-122");

        when(legacyExecutedScenario211.getExtendedExecutedScenario("branch1", legacySource3)).thenReturn(legacyExtendedExecutedScenario211);
        when(legacyExtendedExecutedScenario211.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion31));
        when(legacyExecutedScenario211.getCucumberId()).thenReturn("execution-211");
        when(legacyExecutedScenario211.getStartDateTime()).thenReturn(dateFormat.parse("02/01/2021 01:01:01"));
        when(legacyExecutedScenario211.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-211.jpg");
        when(legacyExecutedScenario211.getVideoUrl()).thenReturn("https://company/media/videos/video-211.avi");
        when(legacyExecutedScenario211.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-211");
        when(legacyExecutedScenario211.getLogsUrl()).thenReturn("https://company/logs/trace-211");
        when(legacyExecutedScenario211.getCucumberReportUrl()).thenReturn("https://company/media/other/result-211.html");
        when(legacyExecutedScenario211.getDiffReportUrl()).thenReturn("https://company/logs/diff-211");
        when(legacyExecutedScenario211.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-211");
        when(legacyExecutedScenario211.getApiServer()).thenReturn("server-211");
        when(legacyExecutedScenario211.getSeleniumNode()).thenReturn("comment-211");

        when(legacyExecutedScenario212.getExtendedExecutedScenario("branch1", legacySource3)).thenReturn(legacyExtendedExecutedScenario212);
        when(legacyExtendedExecutedScenario212.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion32));
        when(legacyExecutedScenario212.getCucumberId()).thenReturn("execution-212");
        when(legacyExecutedScenario212.getStartDateTime()).thenReturn(dateFormat.parse("02/01/2021 02:02:02"));
        when(legacyExecutedScenario212.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-212.jpg");
        when(legacyExecutedScenario212.getVideoUrl()).thenReturn("https://company/media/videos/video-212.avi");
        when(legacyExecutedScenario212.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-212");
        when(legacyExecutedScenario212.getLogsUrl()).thenReturn("https://company/logs/trace-212");
        when(legacyExecutedScenario212.getCucumberReportUrl()).thenReturn("https://company/media/other/result-212.html");
        when(legacyExecutedScenario212.getDiffReportUrl()).thenReturn("https://company/logs/diff-212");
        when(legacyExecutedScenario212.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-212");
        when(legacyExecutedScenario212.getApiServer()).thenReturn("server-212");
        when(legacyExecutedScenario212.getSeleniumNode()).thenReturn("comment-212");

        when(legacyExecutedScenario213.getExtendedExecutedScenario("branch1", legacySource3)).thenReturn(legacyExtendedExecutedScenario213);
        when(legacyExtendedExecutedScenario213.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion33));
        when(legacyExecutedScenario213.getCucumberId()).thenReturn("execution-213");
        when(legacyExecutedScenario213.getStartDateTime()).thenReturn(dateFormat.parse("02/01/2021 03:03:03"));
        when(legacyExecutedScenario213.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-213.jpg");
        when(legacyExecutedScenario213.getVideoUrl()).thenReturn("https://company/media/videos/video-213.avi");
        when(legacyExecutedScenario213.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-213");
        when(legacyExecutedScenario213.getLogsUrl()).thenReturn("https://company/logs/trace-213");
        when(legacyExecutedScenario213.getCucumberReportUrl()).thenReturn("https://company/media/other/result-213.html");
        when(legacyExecutedScenario213.getDiffReportUrl()).thenReturn("https://company/logs/diff-213");
        when(legacyExecutedScenario213.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-213");
        when(legacyExecutedScenario213.getApiServer()).thenReturn("server-213");
        when(legacyExecutedScenario213.getSeleniumNode()).thenReturn("comment-213");

        when(legacyExecutedScenario511.getExtendedExecutedScenario("branch1", legacySource1)).thenReturn(legacyExtendedExecutedScenario511);
        when(legacyExtendedExecutedScenario511.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion11));
        when(legacyExecutedScenario511.getCucumberId()).thenReturn("execution-511");
        when(legacyExecutedScenario511.getStartDateTime()).thenReturn(dateFormat.parse("05/01/2021 01:01:01"));
        when(legacyExecutedScenario511.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-511.jpg");
        when(legacyExecutedScenario511.getVideoUrl()).thenReturn("https://company/media/videos/video-511.avi");
        when(legacyExecutedScenario511.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-511");
        when(legacyExecutedScenario511.getLogsUrl()).thenReturn("https://company/logs/trace-511");
        when(legacyExecutedScenario511.getCucumberReportUrl()).thenReturn("https://company/media/other/result-511.html");
        when(legacyExecutedScenario511.getDiffReportUrl()).thenReturn("https://company/logs/diff-511");
        when(legacyExecutedScenario511.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-511");
        when(legacyExecutedScenario511.getApiServer()).thenReturn("server-511");
        when(legacyExecutedScenario511.getSeleniumNode()).thenReturn("comment-511");

        when(legacyExecutedScenario521.getExtendedExecutedScenario("branch1", legacySource2)).thenReturn(legacyExtendedExecutedScenario521);
        when(legacyExtendedExecutedScenario521.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion21));
        when(legacyExecutedScenario521.getCucumberId()).thenReturn("execution-521");
        when(legacyExecutedScenario521.getStartDateTime()).thenReturn(dateFormat.parse("05/01/2021 02:02:02"));
        when(legacyExecutedScenario521.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-521.jpg");
        when(legacyExecutedScenario521.getVideoUrl()).thenReturn("https://company/media/videos/video-521.avi");
        when(legacyExecutedScenario521.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-521");
        when(legacyExecutedScenario521.getLogsUrl()).thenReturn("https://company/logs/trace-521");
        when(legacyExecutedScenario521.getCucumberReportUrl()).thenReturn("https://company/media/other/result-521.html");
        when(legacyExecutedScenario521.getDiffReportUrl()).thenReturn("https://company/logs/diff-521");
        when(legacyExecutedScenario521.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-521");
        when(legacyExecutedScenario521.getApiServer()).thenReturn("server-521");
        when(legacyExecutedScenario521.getSeleniumNode()).thenReturn("comment-521");

        when(legacyExecutedScenario531.getExtendedExecutedScenario("branch1", legacySource3)).thenReturn(legacyExtendedExecutedScenario531);
        when(legacyExtendedExecutedScenario531.getMatchingMigrationScenarioVersion(migrationScenarios)).thenReturn(Optional.of(migrationScenarioVersion31));
        when(legacyExecutedScenario531.getCucumberId()).thenReturn("execution-531");
        when(legacyExecutedScenario531.getStartDateTime()).thenReturn(dateFormat.parse("05/01/2021 03:03:03"));
        when(legacyExecutedScenario531.getScreenshotUrl()).thenReturn("https://company/media/images/screenshot-531.jpg");
        when(legacyExecutedScenario531.getVideoUrl()).thenReturn("https://company/media/videos/video-531.avi");
        when(legacyExecutedScenario531.getJavaScriptErrorsUrl()).thenReturn("https://company/logs/error-stacktrace-531");
        when(legacyExecutedScenario531.getLogsUrl()).thenReturn("https://company/logs/trace-531");
        when(legacyExecutedScenario531.getCucumberReportUrl()).thenReturn("https://company/media/other/result-531.html");
        when(legacyExecutedScenario531.getDiffReportUrl()).thenReturn("https://company/logs/diff-531");
        when(legacyExecutedScenario531.getHttpRequestsUrl()).thenReturn("https://company/logs/execution-531");
        when(legacyExecutedScenario531.getApiServer()).thenReturn("server-531");
        when(legacyExecutedScenario531.getSeleniumNode()).thenReturn("comment-531");

        when(legacyCountry1.getCode()).thenReturn("country-1");
        when(legacyCountry1.getName()).thenReturn("Country 1");
        when(legacyCountry2.getCode()).thenReturn("country-2");
        when(legacyCountry2.getName()).thenReturn("Country 2");

        when(legacyType1.getCode()).thenReturn("type-1");
        when(legacyType1.getName()).thenReturn("Type 1");
        when(legacyType1.getSource()).thenReturn(legacySource1);
        when(legacyType2.getCode()).thenReturn("type-2");
        when(legacyType2.getName()).thenReturn("Type 2");
        when(legacyType2.getSource()).thenReturn(legacySource2);
        when(legacyType3.getCode()).thenReturn("type-3");
        when(legacyType3.getName()).thenReturn("Type 3");
        when(legacyType3.getSource()).thenReturn(legacySource3);

        when(legacyCycleDefinition1.getBranch()).thenReturn("branch1");
        when(legacyCycleDefinition1.getName()).thenReturn("cycle1");
        when(legacyCycleDefinition2.getBranch()).thenReturn("branch2");
        when(legacyCycleDefinition2.getName()).thenReturn("cycle2");

        when(migrationCycle1.getName()).thenReturn("cycle1");
        when(migrationCycle2.getName()).thenReturn("cycle2");

        when(migrationDeploymentValidationRepository.save(any(DeploymentValidation.class))).thenReturn(
                migratedDeploymentValidation1,
                migratedDeploymentValidation2,
                migratedDeploymentValidation3,
                migratedDeploymentValidation4,
                migratedDeploymentValidation5
        );

        when(objectMapper.readValue(mainThresholdsJSON1, Map.class)).thenReturn(mainThresholds1);
        when(objectMapper.readValue(overriddenThresholdsJSON1, Map.class)).thenReturn(overriddenThresholds1);
        when(objectMapper.readValue(mainThresholdsJSON2, Map.class)).thenReturn(mainThresholds2);

        // Then
        List<DeploymentValidation> deploymentValidations = deploymentValidationMigrationService.migrateDeploymentValidation(
                legacyProject,
                migrationProject,
                List.of(legacyExecution1, legacyExecution2, legacyExecution3, legacyExecution4, legacyExecution5),
                List.of(migrationBranch2, migrationBranch1),
                migrationScenarios
        );
        assertThat(deploymentValidations)
                .hasSize(5)
                .containsExactly(
                        migratedDeploymentValidation1,
                        migratedDeploymentValidation2,
                        migratedDeploymentValidation3,
                        migratedDeploymentValidation4,
                        migratedDeploymentValidation5
                );
        ArgumentCaptor<DeploymentValidation> savedDeploymentValidationsArgumentCaptor = ArgumentCaptor.forClass(DeploymentValidation.class);
        verify(migrationDeploymentValidationRepository, times(5)).save(savedDeploymentValidationsArgumentCaptor.capture());
        List<DeploymentValidation> capturedSavedDeploymentValidations = savedDeploymentValidationsArgumentCaptor.getAllValues();
        assertThat(capturedSavedDeploymentValidations)
                .hasSize(5)
                .extracting(
                        "status",
                        "startDateTime",
                        "updateDateTime",
                        "discardReason",
                        "version.creationDateTime",
                        "version.release",
                        "version.value",
                        "cycle",
                        "comment"
                )
                .containsExactly(
                        tuple(
                                DeploymentValidationStatus.SUCCESS,
                                LocalDateTime.of(2021, 1, 1, 1, 10, 10),
                                null,
                                Optional.of("discard reason"),
                                LocalDateTime.of(2021, 1, 1, 2, 20, 20),
                                "release-1",
                                "version-1",
                                migrationCycle1,
                                null
                        ),
                        tuple(
                                DeploymentValidationStatus.FAILURE,
                                LocalDateTime.of(2021, 2, 2, 1, 10, 10),
                                null,
                                Optional.empty(),
                                LocalDateTime.of(2021, 2, 2, 2, 20, 20),
                                "release-2",
                                "version-2",
                                migrationCycle1,
                                null
                        ),
                        tuple(
                                DeploymentValidationStatus.INCOMPLETE,
                                LocalDateTime.of(2021, 3, 3, 1, 10, 10),
                                null,
                                Optional.empty(),
                                LocalDateTime.of(2021, 3, 3, 2, 20, 20),
                                "release-3",
                                "version-3",
                                migrationCycle1,
                                null
                        ),
                        tuple(
                                DeploymentValidationStatus.WARNING,
                                LocalDateTime.of(2021, 4, 4, 1, 10, 10),
                                null,
                                Optional.empty(),
                                LocalDateTime.of(2021, 4, 4, 2, 20, 20),
                                "release-4",
                                "version-4",
                                migrationCycle2,
                                null
                        ),
                        tuple(
                                DeploymentValidationStatus.RUNNING,
                                LocalDateTime.of(2021, 5, 5, 1, 10, 10),
                                null,
                                Optional.empty(),
                                LocalDateTime.of(2021, 5, 5, 2, 20, 20),
                                "release-5",
                                "version-5",
                                migrationCycle1,
                                null
                        )
                );

        DeploymentValidation deploymentValidation1 = capturedSavedDeploymentValidations.get(0);
        DeploymentValidation deploymentValidation2 = capturedSavedDeploymentValidations.get(1);
        DeploymentValidation deploymentValidation3 = capturedSavedDeploymentValidations.get(2);
        DeploymentValidation deploymentValidation4 = capturedSavedDeploymentValidations.get(3);
        DeploymentValidation deploymentValidation5 = capturedSavedDeploymentValidations.get(4);

        assertThat(deploymentValidation1.getJobHistory())
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactly(
                        tuple(
                                "https://company-ci.com/execution-1",
                                LocalDateTime.of(2021, 1, 1, 1, 10, 10)
                        )
                );
        assertThat(deploymentValidation2.getJobHistory())
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactly(
                        tuple(
                                "https://company-ci.com/execution-2",
                                LocalDateTime.of(2021, 2, 2, 1, 10, 10)
                        )
                );
        assertThat(deploymentValidation3.getJobHistory())
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactly(
                        tuple(
                                "https://company-ci.com/execution-3",
                                LocalDateTime.of(2021, 3, 3, 1, 10, 10)
                        )
                );
        assertThat(deploymentValidation4.getJobHistory())
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactly(
                        tuple(
                                "https://company-ci.com/execution-4",
                                LocalDateTime.of(2021, 4, 4, 1, 10, 10)
                        )
                );
        assertThat(deploymentValidation5.getJobHistory())
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactly(
                        tuple(
                                "https://company-ci.com/execution-5",
                                LocalDateTime.of(2021, 5, 5, 1, 10, 10)
                        )
                );

        var taggedExecutions1 = deploymentValidation1.getSubdeploymentValidations();
        assertThat(taggedExecutions1)
                .hasSize(2)
                .extracting(
                        "comment",
                        "tag.id.code",
                        "tag.id.project",
                        "type.id.code",
                        "type.id.project"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "Comment for run 11",
                                "country-1",
                                migrationProject,
                                "type-1",
                                migrationProject
                        ),
                        tuple(
                                "Comment for run 12",
                                "country-1",
                                migrationProject,
                                "type-2",
                                migrationProject
                        )
                );
        var taggedExecutionsJobHistory1 = taggedExecutions1.stream()
                .map(SubdeploymentValidation::getJobHistory)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(taggedExecutionsJobHistory1)
                .hasSize(2)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "https://company-ci.com/execution-1/run-11",
                                LocalDateTime.of(2021, 1, 1, 1, 10, 10)
                        ),
                        tuple(
                                "https://company-ci.com/execution-1/run-12",
                                LocalDateTime.of(2021, 1, 1, 1, 10, 10)
                        )
                );
        var scenarioResultsFromTaggedExecution1 = taggedExecutions1.stream()
                .map(SubdeploymentValidation::getScenarioResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(scenarioResultsFromTaggedExecution1)
                .hasSize(4) // 2 + 2
                .extracting(
                        //"status",
                        "code",
                        "version",
                        "startDate",
                        "screenshotUrl",
                        "videoUrl",
                        "otherDisplayUrl",
                        "scenarioExecutionUrl",
                        "executionTraceUrl",
                        "diffReportUrl",
                        "errorStackTraceUrl",
                        "targetServer",
                        "comment"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "execution-111",
                                migrationScenarioVersion11,
                                LocalDateTime.of(2021, 1, 1, 1, 1, 1),
                                "https://company/media/images/screenshot-111.jpg",
                                "https://company/media/videos/video-111.avi",
                                "https://company/media/other/result-111.html",
                                "https://company/logs/execution-111",
                                "https://company/logs/trace-111",
                                "https://company/logs/diff-111",
                                "https://company/logs/error-stacktrace-111",
                                "server-111",
                                "comment-111"
                        ),
                        tuple(
                                "execution-112",
                                migrationScenarioVersion11,
                                LocalDateTime.of(2021, 1, 1, 2, 2, 2),
                                "https://company/media/images/screenshot-112.jpg",
                                "https://company/media/videos/video-112.avi",
                                "https://company/media/other/result-112.html",
                                "https://company/logs/execution-112",
                                "https://company/logs/trace-112",
                                "https://company/logs/diff-112",
                                "https://company/logs/error-stacktrace-112",
                                "server-112",
                                "comment-112"
                        ),
                        tuple(
                                "execution-121",
                                migrationScenarioVersion21,
                                LocalDateTime.of(2021, 1, 1, 3, 3, 3),
                                "https://company/media/images/screenshot-121.jpg",
                                "https://company/media/videos/video-121.avi",
                                "https://company/media/other/result-121.html",
                                "https://company/logs/execution-121",
                                "https://company/logs/trace-121",
                                "https://company/logs/diff-121",
                                "https://company/logs/error-stacktrace-121",
                                "server-121",
                                "comment-121"
                        ),
                        tuple(
                                "execution-122",
                                migrationScenarioVersion22,
                                LocalDateTime.of(2021, 1, 1, 4, 4, 4),
                                "https://company/media/images/screenshot-122.jpg",
                                "https://company/media/videos/video-122.avi",
                                "https://company/media/other/result-122.html",
                                "https://company/logs/execution-122",
                                "https://company/logs/trace-122",
                                "https://company/logs/diff-122",
                                "https://company/logs/error-stacktrace-122",
                                "server-122",
                                "comment-122"
                        )
                );

        var taggedExecutions2 = deploymentValidation2.getSubdeploymentValidations();
        assertThat(taggedExecutions2)
                .hasSize(1)
                .extracting(
                        "comment",
                        "tag.id.code",
                        "tag.id.project",
                        "type.id.code",
                        "type.id.project"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "Comment for run 21",
                                "country-1",
                                migrationProject,
                                "type-3",
                                migrationProject
                        )
                );
        var taggedExecutionsJobHistory2 = taggedExecutions2.stream()
                .map(SubdeploymentValidation::getJobHistory)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(taggedExecutionsJobHistory2)
                .hasSize(1)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "https://company-ci.com/execution-2/run-21",
                                LocalDateTime.of(2021, 2, 2, 1, 10, 10)
                        )
                );
        var scenarioResultsFromTaggedExecution2 = taggedExecutions2.stream()
                .map(SubdeploymentValidation::getScenarioResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(scenarioResultsFromTaggedExecution2)
                .hasSize(3)
                .extracting(
                        //"status",
                        "code",
                        "version",
                        "startDate",
                        "screenshotUrl",
                        "videoUrl",
                        "otherDisplayUrl",
                        "scenarioExecutionUrl",
                        "executionTraceUrl",
                        "diffReportUrl",
                        "errorStackTraceUrl",
                        "targetServer",
                        "comment"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "execution-211",
                                migrationScenarioVersion31,
                                LocalDateTime.of(2021, 1, 2, 1, 1, 1),
                                "https://company/media/images/screenshot-211.jpg",
                                "https://company/media/videos/video-211.avi",
                                "https://company/media/other/result-211.html",
                                "https://company/logs/execution-211",
                                "https://company/logs/trace-211",
                                "https://company/logs/diff-211",
                                "https://company/logs/error-stacktrace-211",
                                "server-211",
                                "comment-211"
                        ),
                        tuple(
                                "execution-212",
                                migrationScenarioVersion32,
                                LocalDateTime.of(2021, 1, 2, 2, 2, 2),
                                "https://company/media/images/screenshot-212.jpg",
                                "https://company/media/videos/video-212.avi",
                                "https://company/media/other/result-212.html",
                                "https://company/logs/execution-212",
                                "https://company/logs/trace-212",
                                "https://company/logs/diff-212",
                                "https://company/logs/error-stacktrace-212",
                                "server-212",
                                "comment-212"
                        ),
                        tuple(
                                "execution-213",
                                migrationScenarioVersion33,
                                LocalDateTime.of(2021, 1, 2, 3, 3, 3),
                                "https://company/media/images/screenshot-213.jpg",
                                "https://company/media/videos/video-213.avi",
                                "https://company/media/other/result-213.html",
                                "https://company/logs/execution-213",
                                "https://company/logs/trace-213",
                                "https://company/logs/diff-213",
                                "https://company/logs/error-stacktrace-213",
                                "server-213",
                                "comment-213"
                        )
                );

        var taggedExecutions3 = deploymentValidation3.getSubdeploymentValidations();
        assertThat(taggedExecutions3)
                .isNotNull()
                .isEmpty();

        var taggedExecutions4 = deploymentValidation4.getSubdeploymentValidations();
        assertThat(taggedExecutions4)
                .isNotNull()
                .isEmpty();

        var taggedExecutions5 = deploymentValidation5.getSubdeploymentValidations();
        assertThat(taggedExecutions5)
                .hasSize(3)
                .extracting(
                        "comment",
                        "tag.id.code",
                        "tag.id.project",
                        "type.id.code",
                        "type.id.project"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "Comment for run 51",
                                "country-2",
                                migrationProject,
                                "type-1",
                                migrationProject
                        ),
                        tuple(
                                "Comment for run 52",
                                "country-2",
                                migrationProject,
                                "type-2",
                                migrationProject
                        ),
                        tuple(
                                "Comment for run 53",
                                "country-2",
                                migrationProject,
                                "type-3",
                                migrationProject
                        )
                );
        var taggedExecutionsJobHistory5 = taggedExecutions5.stream()
                .map(SubdeploymentValidation::getJobHistory)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(taggedExecutionsJobHistory5)
                .hasSize(3)
                .extracting(
                        "jobUrl",
                        "startDate"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "https://company-ci.com/execution-5/run-51",
                                LocalDateTime.of(2021, 5, 5, 1, 10, 10)
                        ),
                        tuple(
                                "https://company-ci.com/execution-5/run-52",
                                LocalDateTime.of(2021, 5, 5, 1, 10, 10)
                        ),
                        tuple(
                                "https://company-ci.com/execution-5/run-53",
                                LocalDateTime.of(2021, 5, 5, 1, 10, 10)
                        )
                );
        var scenarioResultsFromTaggedExecution5 = taggedExecutions5.stream()
                .map(SubdeploymentValidation::getScenarioResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(scenarioResultsFromTaggedExecution5)
                .hasSize(3) // 1 + 1 + 1
                .extracting(
                        //"status",
                        "code",
                        "version",
                        "startDate",
                        "screenshotUrl",
                        "videoUrl",
                        "otherDisplayUrl",
                        "scenarioExecutionUrl",
                        "executionTraceUrl",
                        "diffReportUrl",
                        "errorStackTraceUrl",
                        "targetServer",
                        "comment"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "execution-511",
                                migrationScenarioVersion11,
                                LocalDateTime.of(2021, 1, 5, 1, 1, 1),
                                "https://company/media/images/screenshot-511.jpg",
                                "https://company/media/videos/video-511.avi",
                                "https://company/media/other/result-511.html",
                                "https://company/logs/execution-511",
                                "https://company/logs/trace-511",
                                "https://company/logs/diff-511",
                                "https://company/logs/error-stacktrace-511",
                                "server-511",
                                "comment-511"
                        ),
                        tuple(
                                "execution-521",
                                migrationScenarioVersion21,
                                LocalDateTime.of(2021, 1, 5, 2, 2, 2),
                                "https://company/media/images/screenshot-521.jpg",
                                "https://company/media/videos/video-521.avi",
                                "https://company/media/other/result-521.html",
                                "https://company/logs/execution-521",
                                "https://company/logs/trace-521",
                                "https://company/logs/diff-521",
                                "https://company/logs/error-stacktrace-521",
                                "server-521",
                                "comment-521"
                        ),
                        tuple(
                                "execution-531",
                                migrationScenarioVersion31,
                                LocalDateTime.of(2021, 1, 5, 3, 3, 3),
                                "https://company/media/images/screenshot-531.jpg",
                                "https://company/media/videos/video-531.avi",
                                "https://company/media/other/result-531.html",
                                "https://company/logs/execution-531",
                                "https://company/logs/trace-531",
                                "https://company/logs/diff-531",
                                "https://company/logs/error-stacktrace-531",
                                "server-531",
                                "comment-531"
                        )
                );

        ArgumentCaptor<List<ScenarioSeverityThreshold>> severityThresholdsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationScenarioSeverityThresholdRepository, times(2)).saveAll(severityThresholdsArgumentCaptor.capture());
        List<List<ScenarioSeverityThreshold>> allCapturedSavedScenarioSeverityThresholds = severityThresholdsArgumentCaptor.getAllValues();

        assertThat(allCapturedSavedScenarioSeverityThresholds.get(0))
                .hasSize(6)
                .extracting(
                        "failure",
                        "warning",
                        "severity.id.project",
                        "severity.id.code",
                        "severity.name",
                        "severity.description",
                        "deploymentValidation",
                        "cycle"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                100,
                                100,
                                migrationProject,
                                "sanity-check",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle1
                        ),
                        tuple(
                                95,
                                90,
                                migrationProject,
                                "high",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle1
                        ),
                        tuple(
                                90,
                                70,
                                migrationProject,
                                "medium",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle1
                        ),
                        tuple(
                                90,
                                85,
                                migrationProject,
                                "sanity-check",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle2
                        ),
                        tuple(
                                80,
                                75,
                                migrationProject,
                                "high",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle2
                        ),
                        tuple(
                                75,
                                65,
                                migrationProject,
                                "medium",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                migrationCycle2
                        )
                );
        assertThat(allCapturedSavedScenarioSeverityThresholds.get(1))
                .hasSize(3)
                .extracting(
                        "failure",
                        "warning",
                        "severity.id.project",
                        "severity.id.code",
                        "severity.name",
                        "severity.description",
                        "deploymentValidation",
                        "cycle"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                100,
                                95,
                                migrationProject,
                                "sanity-check",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.of(migratedDeploymentValidation2),
                                migrationCycle1
                        ),
                        tuple(
                                90,
                                85,
                                migrationProject,
                                "high",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.of(migratedDeploymentValidation2),
                                migrationCycle1
                        ),
                        tuple(
                                80,
                                50,
                                migrationProject,
                                "medium",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.of(migratedDeploymentValidation2),
                                migrationCycle1
                        )
                );

        ArgumentCaptor<List<Branch>> branchesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationBranchRepository).saveAll(branchesArgumentCaptor.capture());
        assertThat(branchesArgumentCaptor.getValue())
                .hasSize(1)
                .extracting(
                        "id.project",
                        "id.code",
                        "environmentName"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                               migrationProject,
                               "branch1",
                               "platform-1"
                        )
                );
    }
}
