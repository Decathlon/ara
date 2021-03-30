package com.decathlon.ara.v2.service.migration;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.*;
import com.decathlon.ara.v2.domain.Scenario;
import com.decathlon.ara.v2.domain.*;
import com.decathlon.ara.v2.exception.AraException;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.ProjectAlreadyExistsException;
import com.decathlon.ara.v2.exception.project.UnknownProjectException;
import com.decathlon.ara.v2.repository.*;
import com.decathlon.ara.v2.service.migration.execution.V2DeploymentValidationMigrationService;
import com.decathlon.ara.v2.service.migration.feature.V2FeatureMigrationService;
import com.decathlon.ara.v2.service.migration.scenario.V2ScenarioMigrationService;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class V2ProjectMigrationServiceTest {

    @Mock
    private ProjectRepository legacyProjectRepository;
    @Mock
    private CycleDefinitionRepository legacyCycleDefinitionRepository;
    @Mock
    private SeverityRepository legacySeverityRepository;
    @Mock
    private CountryRepository legacyCountryRepository;
    @Mock
    private SourceRepository legacySourceRepository;
    @Mock
    private TypeRepository legacyTypeRepository;
    @Mock
    private ProblemRepository legacyProblemRepository;
    @Mock
    private ProblemPatternRepository legacyProblemPatternRepository;
    @Mock
    private TeamRepository legacyTeamRepository;
    @Mock
    private RootCauseRepository legacyRootCauseRepository;
    @Mock
    private ExecutionRepository legacyExecutionRepository;

    @Mock
    private V2ProjectRepository migrationProjectRepository;
    @Mock
    private V2BranchRepository migrationBranchRepository;
    @Mock
    private V2ScenarioSeverityRepository migrationSeverityRepository;
    @Mock
    private V2TagRepository migrationTagRepository;
    @Mock
    private V2ScenarioTypeRepository migrationScenarioTypeRepository;
    @Mock
    private V2ScenarioExecutionTypeRepository migrationScenarioExecutionTypeRepository;
    @Mock
    private V2ProblemRepository migrationProblemRepository;
    @Mock
    private V2TeamRepository migrationTeamRepository;
    @Mock
    private V2ProblemRootCauseRepository migrationRootCauseRepository;
    @Mock
    private V2ScenarioErrorTracePatternRepository migrationPatternRepository;
    @Mock
    private V2RepositoryRepository migrationRepositoryRepository;

    @Mock
    private V2FeatureMigrationService featureMigrationService;
    @Mock
    private V2ScenarioMigrationService scenarioMigrationService;
    @Mock
    private V2DeploymentValidationMigrationService executionMigrationService;

    @InjectMocks
    private V2ProjectMigrationService migrationService;

    @Test
    void migrateProject_throwUnknownProjectException_whenProjectNotFoundInLegacyTable() {
        // Given
        String projectCode = "project-code";

        // When
        when(legacyProjectRepository.findOneByCode(projectCode)).thenReturn(null);

        // Then
        assertThatThrownBy(() -> migrationService.migrateProject(projectCode, Optional.empty())).isExactlyInstanceOf(UnknownProjectException.class);
    }

    @Test
    void migrateProject_throwProjectAlreadyExistsException_whenProjectExistsInMigrationTable() {
        // Given
        String projectCode = "project-code";
        Project legacyProject = mock(Project.class);

        // When
        when(legacyProjectRepository.findOneByCode(projectCode)).thenReturn(legacyProject);
        when(migrationProjectRepository.existsById(projectCode)).thenReturn(true);

        // Then
        assertThatThrownBy(() -> migrationService.migrateProject(projectCode, Optional.empty())).isExactlyInstanceOf(ProjectAlreadyExistsException.class);
    }

    @Test
    void migrateProject_getAllExecutions_whenNoDateGiven() throws BusinessException {
        // Given
        Long legacyProjectId = 1L;
        String projectCode = "project-code";
        Project legacyProject = mock(Project.class);

        // When
        when(legacyProjectRepository.findOneByCode(projectCode)).thenReturn(legacyProject);
        when(migrationProjectRepository.existsById(projectCode)).thenReturn(false);
        when(legacyProject.getId()).thenReturn(legacyProjectId);

        // Then
        migrationService.migrateProject(projectCode, Optional.empty());
        verify(legacyExecutionRepository).findAllByProjectId(legacyProjectId);
    }

    @Test
    void migrateProject_saveProject_whenProjectDoesNotExistInMigrationTable() throws AraException, ParseException {
        // Given
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String projectCode = "project-code";
        Date executionsStartDate = mock(Date.class);
        String projectName = "Project name";
        Project legacyProject = mock(Project.class);
        com.decathlon.ara.v2.domain.Project savedMigrationProject = mock(com.decathlon.ara.v2.domain.Project.class);

        Long legacyProjectId = 1L;
        CycleDefinition legacyDevelopDayCycle = mock(CycleDefinition.class);
        CycleDefinition legacyDevelopNightCycle = mock(CycleDefinition.class);
        CycleDefinition legacyMasterDayCycle = mock(CycleDefinition.class);
        CycleDefinition legacyMasterNightCycle = mock(CycleDefinition.class);

        Severity legacySanityCheckSeverity = mock(Severity.class);
        Severity legacyHighSeverity = mock(Severity.class);
        Severity legacyMediumSeverity = mock(Severity.class);

        Country legacyFranceCountry = mock(Country.class);
        Country legacyUnitedStateCountry = mock(Country.class);

        Source legacyPostmanSource = mock(Source.class);
        Type legacyApiType = mock(Type.class);
        Source legacyCucumberSource = mock(Source.class);
        Type legacyWebType = mock(Type.class);
        Type legacyMobileType = mock(Type.class);

        Problem legacyProblem1 = mock(Problem.class);
        Problem legacyProblem2 = mock(Problem.class);
        Problem legacyProblem3 = mock(Problem.class);

        Team legacyTeam1 = mock(Team.class);
        Team legacyTeam2 = mock(Team.class);

        RootCause legacyRootCause1 = mock(RootCause.class);
        RootCause legacyRootCause2 = mock(RootCause.class);

        ProblemPattern legacyProblemPattern1 = mock(ProblemPattern.class);
        ProblemPattern legacyProblemPattern2 = mock(ProblemPattern.class);

        List<Execution> legacyExecutions = mock(List.class);

        List<Feature> migratedFeatures = mock(List.class);
        List<Tag> migratedTags = mock(List.class);
        List<Scenario> migratedScenarios = mock(List.class);
        List<Branch> migratedBranches = mock(List.class);

        com.decathlon.ara.v2.domain.Team migratedTeam1 = new com.decathlon.ara.v2.domain.Team().withName("Department 1 / Team 1");
        com.decathlon.ara.v2.domain.Team migratedTeam2 = new com.decathlon.ara.v2.domain.Team().withName("Another Dep. / Team 2");

        ProblemRootCause migratedRootCause1 = new ProblemRootCause().withName("Some ROOT cause");
        ProblemRootCause migratedRootCause2 = new ProblemRootCause().withName("Another root cause, but with some << $p€çi@L çh@r >>");

        com.decathlon.ara.v2.domain.Problem migratedProblem1 = new com.decathlon.ara.v2.domain.Problem().withName("problem-1");
        com.decathlon.ara.v2.domain.Problem migratedProblem2 = new com.decathlon.ara.v2.domain.Problem().withName("problem-2");
        com.decathlon.ara.v2.domain.Problem migratedProblem3 = new com.decathlon.ara.v2.domain.Problem().withName("problem-3");

        // When
        when(legacyProjectRepository.findOneByCode(projectCode)).thenReturn(legacyProject);
        when(legacyProject.getCode()).thenReturn(projectCode);
        when(legacyProject.getName()).thenReturn(projectName);
        when(migrationProjectRepository.existsById(projectCode)).thenReturn(false);
        when(migrationProjectRepository.save(any())).thenReturn(savedMigrationProject);
        when(legacyProject.getId()).thenReturn(legacyProjectId);

        when(legacyCycleDefinitionRepository.findAllByProjectIdOrderByBranchPositionAscBranchAscNameAsc(legacyProjectId))
                .thenReturn(List.of(legacyDevelopNightCycle, legacyDevelopDayCycle, legacyMasterDayCycle, legacyMasterNightCycle));
        when(legacyDevelopDayCycle.getBranch()).thenReturn("DEVELOP");
        when(legacyDevelopDayCycle.getName()).thenReturn("DAY");
        when(legacyDevelopDayCycle.getBranchPosition()).thenReturn(1);
        when(legacyDevelopNightCycle.getBranch()).thenReturn("DEVELOP");
        when(legacyDevelopNightCycle.getName()).thenReturn("NIGHT");
        when(legacyDevelopNightCycle.getBranchPosition()).thenReturn(100);
        when(legacyMasterDayCycle.getBranch()).thenReturn("MASTER");
        when(legacyMasterDayCycle.getName()).thenReturn("DAY");
        when(legacyMasterDayCycle.getBranchPosition()).thenReturn(2);
        when(legacyMasterNightCycle.getBranch()).thenReturn("MASTER");
        when(legacyMasterNightCycle.getName()).thenReturn("NIGHT");
        when(legacyMasterNightCycle.getBranchPosition()).thenReturn(200);

        when(migrationBranchRepository.saveAll(anyList())).thenReturn(migratedBranches);

        when(legacySeverityRepository.findAllByProjectIdOrderByPosition(legacyProjectId)).thenReturn(
                List.of(legacySanityCheckSeverity, legacyHighSeverity, legacyMediumSeverity)
        );
        when(legacySanityCheckSeverity.getCode()).thenReturn("sanity-check");
        when(legacySanityCheckSeverity.getName()).thenReturn("Sanity Check");
        when(legacySanityCheckSeverity.getPosition()).thenReturn(1);
        when(legacyHighSeverity.getCode()).thenReturn("high");
        when(legacyHighSeverity.getName()).thenReturn("High");
        when(legacyHighSeverity.getPosition()).thenReturn(2);
        when(legacyMediumSeverity.getCode()).thenReturn("medium");
        when(legacyMediumSeverity.getName()).thenReturn("Medium");
        when(legacyMediumSeverity.getPosition()).thenReturn(3);

        when(legacyCountryRepository.findAllByProjectIdOrderByCode(legacyProjectId)).thenReturn(
                List.of(legacyFranceCountry, legacyUnitedStateCountry)
        );
        when(legacyFranceCountry.getCode()).thenReturn("france");
        when(legacyFranceCountry.getName()).thenReturn("France");
        when(legacyUnitedStateCountry.getCode()).thenReturn("US");
        when(legacyUnitedStateCountry.getName()).thenReturn("United State");
        when(migrationTagRepository.saveAll(anyList())).thenReturn(migratedTags);

        when(legacySourceRepository.findAllByProjectIdOrderByName(legacyProjectId)).thenReturn(
                List.of(legacyPostmanSource, legacyCucumberSource)
        );
        when(legacyPostmanSource.getName()).thenReturn("API");
        when(legacyPostmanSource.getCode()).thenReturn("api");
        when(legacyPostmanSource.getTechnology()).thenReturn(Technology.POSTMAN);
        when(legacyCucumberSource.getName()).thenReturn("Web");
        when(legacyCucumberSource.getCode()).thenReturn("web");
        when(legacyCucumberSource.getTechnology()).thenReturn(Technology.CUCUMBER);

        when(legacyTypeRepository.findAllByProjectIdOrderByCode(legacyProjectId)).thenReturn(
                List.of(legacyApiType, legacyWebType, legacyMobileType)
        );
        when(legacyApiType.getName()).thenReturn("Back office testing");
        when(legacyApiType.getCode()).thenReturn("back-office-testing");
        when(legacyApiType.getSource()).thenReturn(legacyPostmanSource);
        when(legacyWebType.getName()).thenReturn("Firefox Desktop");
        when(legacyWebType.getCode()).thenReturn("firefox-desktop");
        when(legacyWebType.getSource()).thenReturn(legacyCucumberSource);
        when(legacyMobileType.getName()).thenReturn("Firefox Mobile");
        when(legacyMobileType.getCode()).thenReturn("firefox-mobile");
        when(legacyMobileType.getSource()).thenReturn(legacyCucumberSource);

        when(legacyProblemRepository.findByProjectId(legacyProjectId)).thenReturn(List.of(legacyProblem1, legacyProblem2, legacyProblem3));

        when(legacyProblem1.getName()).thenReturn("problem-1");
        when(legacyProblem1.getComment()).thenReturn("Problem comment 1");
        when(legacyProblem1.getStatus()).thenReturn(ProblemStatus.OPEN);
        when(legacyProblem1.getDefectId()).thenReturn("defect-123");
        when(legacyProblem1.getCreationDateTime()).thenReturn(dateFormat.parse("01/01/2021 01:01:21"));
        when(legacyProblem1.getClosingDateTime()).thenReturn(dateFormat.parse("02/01/2021 02:01:21"));
        when(legacyProblem1.getFirstSeenDateTime()).thenReturn(dateFormat.parse("03/01/2021 03:01:21"));
        when(legacyProblem1.getLastSeenDateTime()).thenReturn(dateFormat.parse("04/01/2021 04:01:21"));
        when(legacyProblem1.getBlamedTeam()).thenReturn(legacyTeam1);
        when(legacyProblem1.getRootCause()).thenReturn(legacyRootCause1);
        when(legacyProblem2.getName()).thenReturn("problem-2");
        when(legacyProblem2.getComment()).thenReturn("Problem comment 2");
        when(legacyProblem2.getStatus()).thenReturn(ProblemStatus.CLOSED);
        when(legacyProblem2.getDefectId()).thenReturn("defect-abc");
        when(legacyProblem2.getCreationDateTime()).thenReturn(dateFormat.parse("01/02/2021 01:02:21"));
        when(legacyProblem2.getClosingDateTime()).thenReturn(dateFormat.parse("02/02/2021 02:02:21"));
        when(legacyProblem2.getFirstSeenDateTime()).thenReturn(dateFormat.parse("03/02/2021 03:02:21"));
        when(legacyProblem2.getLastSeenDateTime()).thenReturn(dateFormat.parse("04/02/2021 04:02:21"));
        when(legacyProblem2.getBlamedTeam()).thenReturn(legacyTeam2);
        when(legacyProblem2.getRootCause()).thenReturn(null);
        when(legacyProblem3.getName()).thenReturn("problem-3");
        when(legacyProblem3.getComment()).thenReturn("Problem comment 3");
        when(legacyProblem3.getStatus()).thenReturn(ProblemStatus.OPEN);
        when(legacyProblem3.getDefectId()).thenReturn(null);
        when(legacyProblem3.getCreationDateTime()).thenReturn(dateFormat.parse("01/03/2021 01:03:21"));
        when(legacyProblem3.getClosingDateTime()).thenReturn(dateFormat.parse("02/03/2021 02:03:21"));
        when(legacyProblem3.getFirstSeenDateTime()).thenReturn(dateFormat.parse("03/03/2021 03:03:21"));
        when(legacyProblem3.getLastSeenDateTime()).thenReturn(dateFormat.parse("04/03/2021 04:03:21"));
        when(legacyProblem3.getBlamedTeam()).thenReturn(null);
        when(legacyProblem3.getRootCause()).thenReturn(legacyRootCause2);

        when(legacyProblemPatternRepository.findAllByProjectId(legacyProjectId)).thenReturn(
                List.of(legacyProblemPattern1, legacyProblemPattern2)
        );

        when(legacyProblemPattern1.getProblem()).thenReturn(legacyProblem1);
        when(legacyProblemPattern1.getCountry()).thenReturn(legacyFranceCountry);
        when(legacyProblemPattern1.getException()).thenReturn("trace-1");
        when(legacyProblemPattern1.getType()).thenReturn(legacyApiType);
        when(legacyProblemPattern1.getPlatform()).thenReturn("env-1");
        when(legacyProblemPattern1.getRelease()).thenReturn("r1");
        when(legacyProblemPattern1.getScenarioName()).thenReturn("scenario-1");
        when(legacyProblemPattern1.getStep()).thenReturn("step-1");
        when(legacyProblemPattern2.getProblem()).thenReturn(legacyProblem2);
        when(legacyProblemPattern2.getCountry()).thenReturn(legacyUnitedStateCountry);
        when(legacyProblemPattern2.getException()).thenReturn("trace-2");
        when(legacyProblemPattern2.getType()).thenReturn(legacyWebType);
        when(legacyProblemPattern2.getPlatform()).thenReturn("env-2");
        when(legacyProblemPattern2.getRelease()).thenReturn("r2");
        when(legacyProblemPattern2.getScenarioName()).thenReturn("scenario-2");
        when(legacyProblemPattern2.getStep()).thenReturn("step-2");

        when(migrationProblemRepository.saveAll(anyList())).thenReturn(
                List.of(migratedProblem1, migratedProblem2, migratedProblem3)
        );

        when(legacyTeamRepository.findAllByProjectIdOrderByName(legacyProjectId)).thenReturn(
                List.of(legacyTeam1, legacyTeam2)
        );
        when(legacyTeam1.getName()).thenReturn("Department 1 / Team 1");
        when(legacyTeam1.isAssignableToFunctionalities()).thenReturn(true);
        when(legacyTeam1.isAssignableToProblems()).thenReturn(false);
        when(legacyTeam2.getName()).thenReturn("Another Dep. / Team 2");
        when(legacyTeam2.isAssignableToFunctionalities()).thenReturn(false);
        when(legacyTeam2.isAssignableToProblems()).thenReturn(true);

        when(migrationTeamRepository.saveAll(anyList())).thenReturn(List.of(migratedTeam1, migratedTeam2));

        when(legacyRootCauseRepository.findAllByProjectIdOrderByName(legacyProjectId)).thenReturn(
                List.of(legacyRootCause1, legacyRootCause2)
        );
        when(legacyRootCause1.getName()).thenReturn("Some ROOT cause");
        when(legacyRootCause2.getName()).thenReturn("Another root cause, but with some << $p€çi@L çh@r >>");
        when(migrationRootCauseRepository.saveAll(anyList())).thenReturn(
                List.of(migratedRootCause1, migratedRootCause2)
        );

        when(featureMigrationService.migrateFeatures(legacyProject, savedMigrationProject)).thenReturn(migratedFeatures);

        when(legacyExecutionRepository.findAllByProjectIdAndTestDateTimeAfter(legacyProjectId, executionsStartDate)).thenReturn(legacyExecutions);

        when(scenarioMigrationService.migrateScenarios(legacyProject, savedMigrationProject, migratedFeatures, migratedTags, legacyExecutions)).thenReturn(migratedScenarios);

        // Then
        ArgumentCaptor<com.decathlon.ara.v2.domain.Project> migrationProjectToSaveArgumentCaptor = ArgumentCaptor.forClass(com.decathlon.ara.v2.domain.Project.class);
        com.decathlon.ara.v2.domain.Project migrationProject = migrationService.migrateProject(projectCode, Optional.of(executionsStartDate));
        verify(legacyExecutionRepository).findAllByProjectIdAndTestDateTimeAfter(legacyProjectId, executionsStartDate);
        verify(migrationProjectRepository).save(migrationProjectToSaveArgumentCaptor.capture());
        assertThat(migrationProject).isEqualTo(savedMigrationProject);
        assertThat(migrationProjectToSaveArgumentCaptor.getValue())
                .extracting(
                        "code",
                        "name",
                        "description",
                        "enabled"
                )
                .contains(
                        projectCode,
                        projectName,
                        V2ProjectMigrationService.NEW_FIELD_GENERATION,
                        true
                );

        ArgumentCaptor<List<Branch>> migrationBranchesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationBranchRepository).saveAll(migrationBranchesArgumentCaptor.capture());
        List<Branch> capturedBranches = migrationBranchesArgumentCaptor.getValue();
        assertThat(capturedBranches)
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "position",
                        "description"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "develop",
                                "Develop",
                                1,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                savedMigrationProject,
                                "master",
                                "Master",
                                2,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        List<Cycle> developCycles = capturedBranches.stream()
                .filter(branch -> "develop".equals(branch.getId().getCode()))
                .map(Branch::getCycles)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(developCycles)
                .hasSize(2)
                .extracting(
                        "name",
                        "position",
                        "description"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "day",
                                1,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                "night",
                                2,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        List<Cycle> masterCycles = capturedBranches.stream()
                .filter(branch -> "master".equals(branch.getId().getCode()))
                .map(Branch::getCycles)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(masterCycles)
                .hasSize(2)
                .extracting(
                        "name",
                        "position",
                        "description"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "day",
                                1,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                "night",
                                2,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        ArgumentCaptor<Repository> migrationRepositoryArgumentCaptor = ArgumentCaptor.forClass(Repository.class);
        verify(migrationRepositoryRepository).save(migrationRepositoryArgumentCaptor.capture());
        assertThat(migrationRepositoryArgumentCaptor.getValue())
                .extracting(
                        "url",
                        "description",
                        "comment",
                        "branches"
                )
                .contains(
                        "url-to-update",
                        V2ProjectMigrationService.NEW_FIELD_GENERATION,
                        V2ProjectMigrationService.NEW_FIELD_GENERATION,
                        migratedBranches
                );

        ArgumentCaptor<List<ScenarioSeverity>> migrationSeveritiesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationSeverityRepository).saveAll(migrationSeveritiesArgumentCaptor.capture());
        assertThat(migrationSeveritiesArgumentCaptor.getValue())
                .hasSize(3)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description",
                        "level"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "sanity-check",
                                "Sanity Check",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                1
                        ),
                        tuple(
                                savedMigrationProject,
                                "high",
                                "High",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                2
                        ),
                        tuple(
                                savedMigrationProject,
                                "medium",
                                "Medium",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                3
                        )
                );

        ArgumentCaptor<List<Tag>> migrationTagsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationTagRepository).saveAll(migrationTagsArgumentCaptor.capture());
        assertThat(migrationTagsArgumentCaptor.getValue())
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "france",
                                "France",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                savedMigrationProject,
                                "us",
                                "United State",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        ArgumentCaptor<List<ScenarioType>> migrationScenarioTypesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationScenarioTypeRepository).saveAll(migrationScenarioTypesArgumentCaptor.capture());
        assertThat(migrationScenarioTypesArgumentCaptor.getValue())
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description",
                        "technology"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "api",
                                "API",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "POSTMAN"
                        ),
                        tuple(
                                savedMigrationProject,
                                "web",
                                "Web",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "CUCUMBER"
                        )
                );

        ArgumentCaptor<List<ScenarioExecutionType>> migrationScenarioExecutionTypesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationScenarioExecutionTypeRepository).saveAll(migrationScenarioExecutionTypesArgumentCaptor.capture());
        assertThat(migrationScenarioExecutionTypesArgumentCaptor.getValue())
                .hasSize(3)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description",
                        "scenarioType.id.code"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "back-office-testing",
                                "Back office testing",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "api"
                        ),
                        tuple(
                                savedMigrationProject,
                                "firefox-desktop",
                                "Firefox Desktop",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "web"
                        ),
                        tuple(
                                savedMigrationProject,
                                "firefox-mobile",
                                "Firefox Mobile",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "web"
                        )
                );

        ArgumentCaptor<List<com.decathlon.ara.v2.domain.Team>> migrationTeamsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationTeamRepository).saveAll(migrationTeamsArgumentCaptor.capture());
        var migrationTeams = migrationTeamsArgumentCaptor.getValue();
        assertThat(migrationTeams)
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description",
                        "assignableToFeatures",
                        "assignableToProblems"
                )
                .containsExactly(
                        tuple(
                                savedMigrationProject,
                                "department_1_team_1",
                                "Department 1 / Team 1",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                true,
                                false
                        ),
                        tuple(
                                savedMigrationProject,
                                "another_dep_team_2",
                                "Another Dep. / Team 2",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                false,
                                true
                        )
                );

        ArgumentCaptor<List<com.decathlon.ara.v2.domain.Problem>> migrationProblemsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationProblemRepository).saveAll(migrationProblemsArgumentCaptor.capture());
        assertThat(migrationProblemsArgumentCaptor.getValue())
                .hasSize(3)
                .extracting(
                        "name",
                        "description",
                        "defectCode",
                        "status",
                        "creationDateTime",
                        "defectClosingDateTime",
                        "firstOccurrenceDateTime",
                        "lastOccurrenceDateTime",
                        "comment",
                        "team",
                        "rootCause"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "problem-1",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.of("defect-123"),
                                com.decathlon.ara.v2.domain.enumeration.ProblemStatus.OPEN,
                                LocalDateTime.of(2021, 1, 1, 1, 1, 21),
                                LocalDateTime.of(2021, 1, 2, 2, 1, 21),
                                LocalDateTime.of(2021, 1, 3, 3, 1, 21),
                                LocalDateTime.of(2021, 1, 4, 4, 1, 21),
                                "Problem comment 1",
                                migratedTeam1,
                                migratedRootCause1
                        ),
                        tuple(
                                "problem-2",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.of("defect-abc"),
                                com.decathlon.ara.v2.domain.enumeration.ProblemStatus.CLOSED,
                                LocalDateTime.of(2021, 2, 1, 1, 2, 21),
                                LocalDateTime.of(2021, 2, 2, 2, 2, 21),
                                LocalDateTime.of(2021, 2, 3, 3, 2, 21),
                                LocalDateTime.of(2021, 2, 4, 4, 2, 21),
                                "Problem comment 2",
                                migratedTeam2,
                                null
                        ),
                        tuple(
                                "problem-3",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                Optional.empty(),
                                com.decathlon.ara.v2.domain.enumeration.ProblemStatus.OPEN,
                                LocalDateTime.of(2021, 3, 1, 1, 3, 21),
                                LocalDateTime.of(2021, 3, 2, 2, 3, 21),
                                LocalDateTime.of(2021, 3, 3, 3, 3, 21),
                                LocalDateTime.of(2021, 3, 4, 4, 3, 21),
                                "Problem comment 3",
                                null,
                                migratedRootCause2
                        )
                );

        ArgumentCaptor<List<ScenarioErrorTracePattern>> migrationPatternsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationPatternRepository).saveAll(migrationPatternsArgumentCaptor.capture());
        assertThat(migrationPatternsArgumentCaptor.getValue())
                .hasSize(2)
                .extracting(
                        "pattern",
                        "snapshotFilterValue.release",
                        "snapshotFilterValue.technology",
                        "snapshotFilterValue.tag",
                        "snapshotFilterValue.environment",
                        "snapshotFilterValue.scenario",
                        "snapshotFilterValue.step",
                        "problem"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "trace-1",
                                "r1",
                                "back-office-testing",
                                "france",
                                "env-1",
                                "scenario-1",
                                "step-1",
                                migratedProblem1
                        ),
                        tuple(
                                "trace-2",
                                "r2",
                                "firefox-desktop",
                                "US",
                                "env-2",
                                "scenario-2",
                                "step-2",
                                migratedProblem2
                        )
                );

        ArgumentCaptor<List<ProblemRootCause>> migrationRootCausesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationRootCauseRepository).saveAll(migrationRootCausesArgumentCaptor.capture());
        assertThat(migrationRootCausesArgumentCaptor.getValue())
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                savedMigrationProject,
                                "another_root_cause_but_with_some_p_i_l_h_r",
                                "Another root cause, but with some << $p€çi@L çh@r >>",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                savedMigrationProject,
                                "some_root_cause",
                                "Some ROOT cause",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        verify(featureMigrationService).migrateFeatures(legacyProject, savedMigrationProject);
        verify(scenarioMigrationService).migrateScenarios(legacyProject, savedMigrationProject, migratedFeatures, migratedTags, legacyExecutions);
        verify(executionMigrationService).migrateDeploymentValidation(legacyProject, savedMigrationProject, legacyExecutions, migratedBranches, migratedScenarios);
    }
}
