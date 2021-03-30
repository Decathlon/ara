package com.decathlon.ara.v2.service.migration.execution;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class V2DeploymentValidationMigrationService {

    private final ExecutionCompletionRequestRepository legacyExecutionCompletionRequestRepository;

    private final V2DeploymentValidationRepository migrationDeploymentValidationRepository;
    private final V2ScenarioSeverityThresholdRepository migrationScenarioSeverityThresholdRepository;
    private final V2BranchRepository migrationBranchRepository;

    private final ObjectMapper objectMapper;

    /**
     * Migrate executions
     * @param legacyProject the legacy project
     * @param migrationProject the migration project
     * @param legacyExecutions the legacy executions
     * @param allAvailableBranchesAndCycles the previously migrated branches and cycles
     * @param allAvailableScenarios the previously migrated scenarios
     * @return the migrated executions
     * @throws BusinessException if the legacy project, project id or the migration project is null
     */
    public List<DeploymentValidation> migrateDeploymentValidation(
            com.decathlon.ara.domain.Project legacyProject,
            Project migrationProject,
            List<com.decathlon.ara.domain.Execution> legacyExecutions,
            List<Branch> allAvailableBranchesAndCycles,
            List<Scenario> allAvailableScenarios
    ) throws BusinessException {
        if (legacyProject == null) {
            log.error("Could not migrate executions because no legacy project was given");
            throw new ProjectRequiredException();
        }

        var projectId = legacyProject.getId();
        if (projectId == null) {
            log.error("Could not migrate executions because no id found in the given legacy project");
            throw new IncompleteProjectException();
        }

        if (migrationProject == null) {
            log.error("Could not migrate executions because no destination project was given");
            throw new ProjectRequiredException();
        }

        if (CollectionUtils.isEmpty(legacyExecutions)) {
            return new ArrayList<>();
        }

        updateMigrationBranches(migrationProject, allAvailableBranchesAndCycles, legacyExecutions);

        var rawThresholdsJSONByLegacyCycleDefinition = legacyExecutions.stream()
                .collect(Collectors.groupingBy(com.decathlon.ara.domain.Execution::getCycleDefinition))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .collect(Collectors.groupingBy(
                                        com.decathlon.ara.domain.Execution::getQualityThresholds,
                                        Collectors.counting()
                                ))
                                .entrySet()
                                .stream()
                                .max(Comparator.comparingLong(Map.Entry::getValue))
                                .get()
                                .getKey()
                        )
                );
        var allMainMigrationScenarioSeverityThresholds = rawThresholdsJSONByLegacyCycleDefinition
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> getMigrationCycleFromLegacyCycleDefinitionAndAllAvailableBranches(entry.getKey(), allAvailableBranchesAndCycles),
                        Map.Entry::getValue
                        )
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> getMigrationScenarioSeverityThresholdsFromLegacyThresholdsJSON(
                                entry.getValue(),
                                migrationProject,
                                entry.getKey(),
                                Optional.empty()
                        )
                ))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        migrationScenarioSeverityThresholdRepository.saveAll(allMainMigrationScenarioSeverityThresholds);

        var migratedExecutions = legacyExecutions.stream()
                .map(currentExecution -> getSavedMigrationDeploymentValidationFromLegacyExecution(
                        currentExecution,
                        migrationProject,
                        rawThresholdsJSONByLegacyCycleDefinition,
                        allAvailableBranchesAndCycles,
                        allAvailableScenarios
                ))
                .collect(Collectors.toList());
        return migratedExecutions;
    }

    /**
     * Update the migration branches
     * @param migrationProject the migration project
     * @param allBranches the branches previously migrated
     * @param legacyExecutions the legacy executions
     */
    private void updateMigrationBranches(
            Project migrationProject,
            List<Branch> allBranches,
            List<com.decathlon.ara.domain.Execution> legacyExecutions
    ) {
        var mostRecurrentPlatformByBranch = legacyExecutions.stream()
                .collect(Collectors.groupingBy(execution -> execution.getCycleDefinition().getBranch()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue()
                                .stream()
                                .map(com.decathlon.ara.domain.Execution::getRuns)
                                .filter(Objects::nonNull)
                                .flatMap(Collection::stream)
                                .collect(Collectors.groupingBy(
                                        Run::getPlatform,
                                        Collectors.counting()
                                ))
                                .entrySet()
                                .stream()
                                .max(Comparator.comparingLong(Map.Entry::getValue))
                                .map(Map.Entry::getKey)
                ))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
        var migrationBranchesToUpdate = mostRecurrentPlatformByBranch.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getValue(), getMigrationBranchFromCodeAndMigrationProject(allBranches, entry.getKey(), migrationProject)))
                .filter(pair -> pair.getSecond().isPresent())
                .map(pair -> pair.getSecond().get().withEnvironmentName(pair.getFirst()))
                .collect(Collectors.toList());
        migrationBranchRepository.saveAll(migrationBranchesToUpdate);
    }

    /**
     * Get the migration branch from code, if found
     * @param allMigrationBranches all the previously migrated branches
     * @param branchCode the branch code
     * @param migrationProject the migration project
     * @return the migration branch from code, if found
     */
    private Optional<Branch> getMigrationBranchFromCodeAndMigrationProject(
            List<Branch> allMigrationBranches,
            String branchCode,
            Project migrationProject
    ) {
        return allMigrationBranches.stream()
                .filter(branch -> branch.getId().getProject().equals(migrationProject))
                .filter(branch -> branch.getId().getCode().equals(branchCode))
                .findFirst();
    }

    /**
     * Migrate execution
     * @param legacyExecution the legacy execution
     * @param migrationProject the migration project
     * @param rawThresholdsJSONByLegacyCycleDefinition the raw severity thresholds JSON
     * @param allBranches all the migration branches
     * @param migrationScenarios the migration scenarios
     * @return the migrated execution
     */
    private DeploymentValidation getSavedMigrationDeploymentValidationFromLegacyExecution(
            com.decathlon.ara.domain.Execution legacyExecution,
            Project migrationProject,
            Map<CycleDefinition, String> rawThresholdsJSONByLegacyCycleDefinition,
            List<Branch> allBranches,
            List<Scenario> migrationScenarios
    ) {
        var executionStartDate = V2ProjectMigrationService.getDateFromLocalDateTime(legacyExecution.getTestDateTime());
        var versionCreationDate = V2ProjectMigrationService.getDateFromLocalDateTime(legacyExecution.getBuildDateTime());

        String discardReason = null;
        var isDiscarded = StringUtils.isNotBlank(legacyExecution.getDiscardReason()) && ExecutionAcceptance.DISCARDED.equals(legacyExecution.getAcceptance());
        if (isDiscarded) {
            discardReason = legacyExecution.getDiscardReason();
        }

        var legacyCycleDefinition = legacyExecution.getCycleDefinition();
        var migrationCycle = getMigrationCycleFromLegacyCycleDefinitionAndAllAvailableBranches(legacyCycleDefinition, allBranches);

        List<SubdeploymentValidation> migrationSubdeploymentValidations = getMigrationTaggedExecutionsFromLegacyRuns(
                legacyExecution.getRuns(),
                migrationProject,
                legacyCycleDefinition.getBranch(),
                migrationScenarios,
                executionStartDate
        );

        var jobUrl = legacyExecution.getJobUrl();
        var job = new DeploymentValidationJob().withJobUrl(jobUrl).withStartDate(executionStartDate);
        var executionNotCompleted = legacyExecutionCompletionRequestRepository.existsById(jobUrl);
        var status = executionNotCompleted ?
                DeploymentValidationStatus.RUNNING :
                getMigrationExecutionStatusFromLegacyQualityStatus(legacyExecution.getQualityStatus());

        var deploymentValidationToMigrate = new DeploymentValidation()
                .withJobHistory(List.of(job))
                .withStartDateTime(executionStartDate)
                .withUpdateDateTime(null)
                .withDiscardReason(discardReason)
                .withVersion(
                        new DeploymentValidation
                                .Version()
                                .withCreationDateTime(versionCreationDate)
                                .withRelease(legacyExecution.getRelease())
                                .withValue(legacyExecution.getVersion())
                )
                .withCycle(migrationCycle)
                .withComment(null)
                .withSubdeploymentValidations(migrationSubdeploymentValidations)
                .withStatus(status);

        var savedDeploymentValidation = migrationDeploymentValidationRepository.save(deploymentValidationToMigrate);

        var legacyExecutionSeverityThresholdsJSON = legacyExecution.getQualityThresholds();
        var legacyCycleDefinitionThresholdsJSON = rawThresholdsJSONByLegacyCycleDefinition.get(legacyCycleDefinition);
        var thresholdsAreOverridden = !(StringUtils.isBlank(legacyCycleDefinitionThresholdsJSON) || legacyCycleDefinitionThresholdsJSON.equals(legacyExecutionSeverityThresholdsJSON));
        if (thresholdsAreOverridden) {
            var scenarioSeverityThresholdsReadyToBeSaved = getMigrationScenarioSeverityThresholdsFromLegacyThresholdsJSON(
                    legacyExecutionSeverityThresholdsJSON,
                    migrationProject,
                    migrationCycle,
                    Optional.of(savedDeploymentValidation)
            );
            migrationScenarioSeverityThresholdRepository.saveAll(scenarioSeverityThresholdsReadyToBeSaved);
        }

        return savedDeploymentValidation;
    }

    /**
     * Migrate tagged executions
     * @param legacyRuns the legacy runs
     * @param migrationProject the migration project
     * @param branchName the branch name
     * @param migrationScenarios the migration scenarios
     * @param executionStartDate the execution start date
     * @return the migrated tagged executions
     */
    private List<SubdeploymentValidation> getMigrationTaggedExecutionsFromLegacyRuns(
            Set<Run> legacyRuns,
            Project migrationProject,
            String branchName,
            List<Scenario> migrationScenarios,
            LocalDateTime executionStartDate
    ) {
        var migrationTaggedExecutions = CollectionUtils.isEmpty(legacyRuns) ? new ArrayList<SubdeploymentValidation>() : legacyRuns.stream()
                .map(run -> getMigrationTaggedExecutionFromLegacyRun(run, migrationProject, branchName, migrationScenarios, executionStartDate))
                .collect(Collectors.toList());
        return migrationTaggedExecutions;
    }

    /**
     * Migrate tagged execution
     * @param legacyRun the legacy run
     * @param migrationProject the migration project
     * @param branchName the branch name
     * @param migrationScenarios the migration scenarios
     * @param executionStartDate the execution start date
     * @return the migrated tagged execution
     */
    private SubdeploymentValidation getMigrationTaggedExecutionFromLegacyRun(
            Run legacyRun,
            Project migrationProject,
            String branchName,
            List<Scenario> migrationScenarios,
            LocalDateTime executionStartDate
    ) {
        return new SubdeploymentValidation()
                .withJobHistory(List.of(new SubdeploymentValidationJob().withJobUrl(legacyRun.getJobUrl()).withStartDate(executionStartDate)))
                .withComment(legacyRun.getComment())
                .withTag(V2ProjectMigrationService.getMigrationTagFromLegacyCountry(legacyRun.getCountry(), migrationProject))
                .withType(V2ProjectMigrationService.getMigrationScenarioExecutionTypeFromLegacyType(legacyRun.getType(), migrationProject, new ArrayList<>()))
                .withScenarioResults(getMigrationScenarioResultsFromLegacyRun(legacyRun, branchName, migrationScenarios));
    }

    /**
     * Convert legacy run to migration scenario results
     * @param legacyRun the legacy run
     * @param branchName the branch name
     * @param migrationScenarios the migration scenarios
     * @return the scenario results
     */
    private List<ScenarioResult> getMigrationScenarioResultsFromLegacyRun(Run legacyRun, String branchName, List<Scenario> migrationScenarios) {
        return legacyRun.getExecutedScenarios().stream()
                .map(executedScenario -> Pair.of(
                        executedScenario,
                        getMigrationScenarioVersion(branchName, legacyRun, executedScenario, migrationScenarios)
                        )
                )
                .filter(pair -> pair.getSecond().isPresent())
                .map(pair -> Pair.of(pair.getFirst(), pair.getSecond().get()))
                .map(pair -> {
                    final var legacyExecutedScenario = pair.getFirst();
                    final var migrationScenarioVersion = pair.getSecond();
                    return new ScenarioResult()
                            .withCode(legacyExecutedScenario.getCucumberId())
                            .withVersion(migrationScenarioVersion)
                            .withStartDate(legacyExecutedScenario.getStartDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                            .withScreenshotUrl(legacyExecutedScenario.getScreenshotUrl())
                            .withVideoUrl(legacyExecutedScenario.getVideoUrl())
                            .withOtherDisplayUrl(legacyExecutedScenario.getCucumberReportUrl())
                            .withDiffReportUrl(legacyExecutedScenario.getDiffReportUrl())
                            .withScenarioExecutionUrl(legacyExecutedScenario.getHttpRequestsUrl())
                            .withExecutionTraceUrl(legacyExecutedScenario.getLogsUrl())
                            .withErrorStackTraceUrl(legacyExecutedScenario.getJavaScriptErrorsUrl())
                            .withTargetServer(legacyExecutedScenario.getApiServer())
                            .withComment(legacyExecutedScenario.getSeleniumNode());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get migration scenario version, if found
     * @param branchName the branch name
     * @param legacyRun the legacy run
     * @param legacyExecutedScenario the legacy executed scenario
     * @param migrationScenarios the migration scenarios
     * @return the migration scenario version, if found
     */
    private Optional<ScenarioVersion> getMigrationScenarioVersion(
            String branchName,
            Run legacyRun,
            ExecutedScenario legacyExecutedScenario,
            List<Scenario> migrationScenarios
            )
    {
        final var legacyType = legacyRun.getType();
        final var legacySource = legacyType.getSource();
        final var legacyExtendedExecutedScenario = legacyExecutedScenario.getExtendedExecutedScenario(branchName, legacySource);
        return legacyExtendedExecutedScenario.getMatchingMigrationScenarioVersion(migrationScenarios);
    }

    /**
     * Convert a legacy QualityStatus into a migration ExecutionStatus
     * @param legacyQualityStatus the legacy quality status
     * @return the converted execution status
     */
    private DeploymentValidationStatus getMigrationExecutionStatusFromLegacyQualityStatus(QualityStatus legacyQualityStatus) {
        switch(legacyQualityStatus) {
            case PASSED -> {
                return DeploymentValidationStatus.SUCCESS;
            }
            case FAILED -> {
                return DeploymentValidationStatus.FAILURE;
            }
            case INCOMPLETE -> {
                return DeploymentValidationStatus.INCOMPLETE;
            }
            case WARNING -> {
                return DeploymentValidationStatus.WARNING;
            }
            default -> {
                return DeploymentValidationStatus.CREATED;
            }
        }
    }

    /**
     * Get migration scenario severity
     * @param rawThresholds the raw severity thresholds JSON
     * @param migrationProject the migration project
     * @param migrationCycle the migration cycle
     * @param deploymentValidation the previously saved deployment validation
     * @return the migration scenario severity
     */
    private List<ScenarioSeverityThreshold> getMigrationScenarioSeverityThresholdsFromLegacyThresholdsJSON(
            String rawThresholds,
            Project migrationProject,
            Cycle migrationCycle,
            Optional<DeploymentValidation> deploymentValidation
    ) {
        Map<String, QualityThreshold> legacyMainSeverityThresholds;
        try {
            legacyMainSeverityThresholds = objectMapper.readValue(rawThresholds, Map.class);
        } catch (JsonProcessingException e) {
            var thresholdScope = deploymentValidation
                    .map(execution -> String.format("execution %d", execution.getId()))
                    .orElse("cycle");
            log.warn(String.format("Project %s: scenario severity thresholds not saved (for %s) because the JSON was malformed. Check below", migrationProject.getCode(), thresholdScope));
            log.warn(rawThresholds);
            return new ArrayList<>();
        }
        return legacyMainSeverityThresholds
                .entrySet()
                .stream()
                .map(entry -> {
                    var legacyQualityThreshold = entry.getValue();
                    var severityCode = entry.getKey();
                    return new ScenarioSeverityThreshold()
                            .withFailure(legacyQualityThreshold.getFailure())
                            .withWarning(legacyQualityThreshold.getWarning())
                            .withSeverity(
                                    new ScenarioSeverity()
                                            .withId(
                                                    new CodeWithProjectId()
                                                            .withProject(migrationProject)
                                                            .withCode(severityCode)
                                            )
                                            .withName(V2ProjectMigrationService.FIELD_TO_RENAME)
                                            .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION)
                            )
                            .withCycle(migrationCycle)
                            .withDeploymentValidation(deploymentValidation.orElse(null));
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert legacy cycle definition into migration cycle
     * @param legacyCycleDefinition the legacy cycle definition
     * @param allBranches the migration branches
     * @return the migration cycle
     */
    private Cycle getMigrationCycleFromLegacyCycleDefinitionAndAllAvailableBranches(
            CycleDefinition legacyCycleDefinition,
            List<Branch> allBranches
    ) {
        var migrationBranch = allBranches.stream()
                .filter(branch -> legacyCycleDefinition.getBranch().equals(branch.getId().getCode()))
                .findFirst();
        if (migrationBranch.isEmpty()) {
            return null;
        }

        return migrationBranch
                .get()
                .getCycles()
                .stream()
                .filter(cycle -> legacyCycleDefinition.getName().equals(cycle.getName()))
                .findFirst()
                .orElse(null);
    }
}
