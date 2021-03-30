package com.decathlon.ara.v2.service.migration.scenario;

import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.v2.domain.Project;
import com.decathlon.ara.v2.domain.Scenario;
import com.decathlon.ara.v2.domain.*;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.IncompleteProjectException;
import com.decathlon.ara.v2.exception.project.ProjectRequiredException;
import com.decathlon.ara.v2.repository.V2ScenarioRepository;
import com.decathlon.ara.v2.repository.V2ScenarioVersionRepository;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class V2ScenarioMigrationServiceTest {

    @Mock
    private ScenarioRepository legacyScenarioRepository;
    @Mock
    private ExecutionRepository legacyExecutionRepository;

    @Mock
    private V2ScenarioRepository migrationScenarioRepository;
    @Mock
    private V2ScenarioVersionRepository migrationScenarioVersionRepository;

    @InjectMocks
    private V2ScenarioMigrationService scenarioMigrationService;

    @Test
    void migrateScenarios_throwProjectRequiredException_whenLegacyProjectIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() ->
                scenarioMigrationService.migrateScenarios(
                        null,
                        mock(Project.class),
                        null,
                        null,
                        new ArrayList<>()
                )
        ).isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateScenarios_throwIncompleteProjectException_whenLegacyProjectHasNoId() {
        // Given
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(null);

        // Then
        assertThatThrownBy(() ->
                scenarioMigrationService.migrateScenarios(
                        legacyProject,
                        mock(Project.class),
                        null,
                        null,
                        new ArrayList<>()
                )
        ).isExactlyInstanceOf(IncompleteProjectException.class);
    }

    @Test
    void migrateScenarios_throwProjectRequiredException_whenDestinationProjectIsNull() {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);

        // Then
        assertThatThrownBy(() ->
                scenarioMigrationService.migrateScenarios(
                        legacyProject,
                        null,
                        null,
                        null,
                        new ArrayList<>()
                )
        ).isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateScenarios_returnEmptyList_whenNoLegacyScenarioFound() throws BusinessException {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);
        when(legacyScenarioRepository.findAllBySourceProjectId(projectId)).thenReturn(null);

        // Then
        List<Scenario> scenarios = scenarioMigrationService.migrateScenarios(
                legacyProject,
                mock(Project.class),
                null,
                null,
                new ArrayList<>()
        );
        assertThat(scenarios).isNotNull().isEmpty();
    }

    @Test
    void migrateScenarios_saveMigratedVersionedScenarios_whenLegacyScenariosAndExecutionsFound() throws BusinessException, ParseException {
        // Given
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        Project migrationProject = mock(Project.class);

        com.decathlon.ara.domain.Scenario legacyScenario1 = mock(com.decathlon.ara.domain.Scenario.class);
        com.decathlon.ara.domain.Scenario legacyScenario2 = mock(com.decathlon.ara.domain.Scenario.class);
        com.decathlon.ara.domain.Scenario legacyScenario3 = mock(com.decathlon.ara.domain.Scenario.class);
        com.decathlon.ara.domain.Scenario legacyScenario4 = mock(com.decathlon.ara.domain.Scenario.class);

        com.decathlon.ara.domain.Execution legacyExecution1 = mock(com.decathlon.ara.domain.Execution.class);
        com.decathlon.ara.domain.Execution legacyExecution2 = mock(com.decathlon.ara.domain.Execution.class);

        Run legacyRun11 = mock(Run.class);
        Run legacyRun12 = mock(Run.class);
        Run legacyRun13 = mock(Run.class);
        Run legacyRun14 = mock(Run.class);
        Run legacyRun21 = mock(Run.class);
        Run legacyRun22 = mock(Run.class);
        Run legacyRun23 = mock(Run.class);
        Run legacyRun24 = mock(Run.class);

        // Executed scenarios not matching any scenario (1)
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_11 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_12 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_13 = mock(ExecutedScenario.class);

        // Executed scenarios not matching any scenario (2)
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_21 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_22 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_23 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioNotMatchingAnyScenario_24 = mock(ExecutedScenario.class);

        // Executed scenarios matching scenario 1
        ExecutedScenario legacyExecutedScenarioMatchingScenario1_1 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario1_2 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario1_3 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario1_4 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario1_5 = mock(ExecutedScenario.class);

        // Executed scenarios matching scenario 2
        ExecutedScenario legacyExecutedScenarioMatchingScenario2_1 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario2_2 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario2_3 = mock(ExecutedScenario.class);

        // Executed scenarios matching scenario 3
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_1 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_2 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_3 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_4 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_5 = mock(ExecutedScenario.class);
        ExecutedScenario legacyExecutedScenarioMatchingScenario3_6 = mock(ExecutedScenario.class);

        Source legacySource1 = mock(Source.class);
        Source legacySource2 = mock(Source.class);
        Source legacySource3 = mock(Source.class);
        Source legacySource4 = mock(Source.class);

        Type legacyType11 = mock(Type.class);
        Type legacyType12 = mock(Type.class);
        Type legacyType21 = mock(Type.class);
        Type legacyType22 = mock(Type.class);
        Type legacyType3 = mock(Type.class);
        Type legacyType4 = mock(Type.class);

        CycleDefinition legacyCycleDefinition1 = mock(CycleDefinition.class);
        CycleDefinition legacyCycleDefinition2 = mock(CycleDefinition.class);

        Tag migrationTag1 = mock(Tag.class);
        Tag migrationTag2 = mock(Tag.class);
        Tag migrationTag3 = mock(Tag.class);

        Feature migrationFeature1 = mock(Feature.class);
        Feature migrationFeature2 = mock(Feature.class);
        Feature migrationFeature3 = mock(Feature.class);
        Feature migrationFeature4 = mock(Feature.class);
        Feature migrationFeature5 = mock(Feature.class);
        Feature migrationFeature6 = mock(Feature.class);

        Scenario newScenario_1 = mock(Scenario.class);
        Scenario newScenario_2 = mock(Scenario.class);
        Scenario migratedScenario1 = mock(Scenario.class);
        Scenario migratedScenario2 = mock(Scenario.class);
        Scenario migratedScenario3 = mock(Scenario.class);
        Scenario migratedScenario4 = mock(Scenario.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);
        when(legacyScenarioRepository.findAllBySourceProjectId(projectId)).thenReturn(
                List.of(legacyScenario1, legacyScenario2, legacyScenario3, legacyScenario4)
        );
        when(legacyScenario1.getId()).thenReturn(1L);
        when(legacyScenario1.getFeatureName()).thenReturn("Feature file 1");
        when(legacyScenario1.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyScenario1.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("1", "2", "3")));
        when(legacyScenario1.isIgnored()).thenReturn(true);
        when(legacyScenario1.getSource()).thenReturn(legacySource1);
        when(legacyScenario1.getSeverity()).thenReturn("severity-1");
        when(legacyScenario1.getCountryCodes()).thenReturn("all");
        when(legacyScenario1.getScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(323, "Given a delivery content"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(324, "| SKU       | Quantity |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(325, "| NORMALSKU |        1 |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(326, "And a delivery to \"some_address\""),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(327, "And an existing home delivery"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(328, "And a delivery start time for tomorrow"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(329, "When I call the API to find the best scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(330, "Then there is no scenario")
        ));
        when(legacyScenario2.getId()).thenReturn(2L);
        when(legacyScenario2.getFeatureName()).thenReturn("feature file 2");
        when(legacyScenario2.getFeatureFile()).thenReturn("/feature-file2.json");
        when(legacyScenario2.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("scenario name 2", List.of("4", "5", "6")));
        when(legacyScenario2.isIgnored()).thenReturn(false);
        when(legacyScenario2.getSource()).thenReturn(legacySource2);
        when(legacyScenario2.getSeverity()).thenReturn("severity-2");
        when(legacyScenario2.getCountryCodes()).thenReturn(" tag-2,  tag-1 , tag-2");
        when(legacyScenario2.getScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "<Pre-Request Script>"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-1, "POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(0, "Status code is 200"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Is the response valid and has a body ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Does data.id contain any Data ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "<Test Script>")
        ));
        when(legacyScenario3.getId()).thenReturn(3L);
        when(legacyScenario3.getFeatureName()).thenReturn("feature file 3 in tests");
        when(legacyScenario3.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyScenario3.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "4", "6")));
        when(legacyScenario3.isIgnored()).thenReturn(false);
        when(legacyScenario3.getSource()).thenReturn(legacySource3);
        when(legacyScenario3.getSeverity()).thenReturn("severity-3");
        when(legacyScenario3.getCountryCodes()).thenReturn("  tag-2,tag-3 ,     , unknown-tag, tag-1 ");
        when(legacyScenario3.getScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Preparing for data fetching"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Calling the data API"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Successfully retrieving the data")
        ));
        when(legacyScenario4.getId()).thenReturn(4L);
        when(legacyScenario4.getFeatureName()).thenReturn("Feature file 4 in other tests");
        when(legacyScenario4.getFeatureFile()).thenReturn("/other-tests/feature-file4.json");
        when(legacyScenario4.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("last scenario", List.of("1", "3", "5")));
        when(legacyScenario4.isIgnored()).thenReturn(true);
        when(legacyScenario4.getSource()).thenReturn(legacySource3);
        when(legacyScenario4.getSeverity()).thenReturn("severity-1");
        when(legacyScenario4.getCountryCodes()).thenReturn(null);
        when(legacyScenario4.getScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Connecting user to its account"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Selecting item1 and item2"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Adding selection to basket"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(4, "Paying the command"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(5, "Disconnecting user")
        ));

        when(legacyExecution1.getRuns()).thenReturn(new HashSet<>(List.of(legacyRun11, legacyRun12, legacyRun13, legacyRun14)));
        when(legacyExecution1.getCycleDefinition()).thenReturn(legacyCycleDefinition1);
        when(legacyExecution2.getRuns()).thenReturn(new HashSet<>(List.of(legacyRun21, legacyRun22, legacyRun23, legacyRun24)));
        when(legacyExecution2.getCycleDefinition()).thenReturn(legacyCycleDefinition2);

        when(legacyRun11.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario1_3, legacyExecutedScenarioMatchingScenario1_4)));
        when(legacyRun11.getType()).thenReturn(legacyType11);
        when(legacyRun12.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario2_1)));
        when(legacyRun12.getType()).thenReturn(legacyType22);
        when(legacyRun13.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario3_1, legacyExecutedScenarioMatchingScenario3_2, legacyExecutedScenarioMatchingScenario3_3)));
        when(legacyRun13.getType()).thenReturn(legacyType3);
        when(legacyRun14.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(
                legacyExecutedScenarioNotMatchingAnyScenario_11,
                legacyExecutedScenarioNotMatchingAnyScenario_12,
                legacyExecutedScenarioNotMatchingAnyScenario_22,
                legacyExecutedScenarioNotMatchingAnyScenario_23,
                legacyExecutedScenarioNotMatchingAnyScenario_24
        )));
        when(legacyRun14.getType()).thenReturn(legacyType4);
        when(legacyRun21.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario1_1, legacyExecutedScenarioMatchingScenario1_2, legacyExecutedScenarioMatchingScenario1_5)));
        when(legacyRun21.getType()).thenReturn(legacyType12);
        when(legacyRun22.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario2_1, legacyExecutedScenarioMatchingScenario2_2, legacyExecutedScenarioMatchingScenario2_3)));
        when(legacyRun22.getType()).thenReturn(legacyType21);
        when(legacyRun23.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(legacyExecutedScenarioMatchingScenario3_1, legacyExecutedScenarioMatchingScenario3_4, legacyExecutedScenarioMatchingScenario3_5, legacyExecutedScenarioMatchingScenario3_6)));
        when(legacyRun23.getType()).thenReturn(legacyType3);
        when(legacyRun24.getExecutedScenarios()).thenReturn(new HashSet<>(List.of(
                legacyExecutedScenarioNotMatchingAnyScenario_11,
                legacyExecutedScenarioNotMatchingAnyScenario_12,
                legacyExecutedScenarioNotMatchingAnyScenario_13,
                legacyExecutedScenarioNotMatchingAnyScenario_21,
                legacyExecutedScenarioNotMatchingAnyScenario_22,
                legacyExecutedScenarioNotMatchingAnyScenario_23,
                legacyExecutedScenarioNotMatchingAnyScenario_24
        )));
        when(legacyRun24.getType()).thenReturn(legacyType4);

        // Executed scenarios not matching any scenario (1)
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getId()).thenReturn(11L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.matchesScenario(any(com.decathlon.ara.domain.Scenario.class), any(Source.class))).thenReturn(false);
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getStartDateTime()).thenReturn(dateFormat.parse("11/01/2021 11:01:21"));
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getFeatureName()).thenReturn("New feature file");
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getFeatureFile()).thenReturn("new-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("New scenario name", List.of("1", "5", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getSeverity()).thenReturn("severity-1");
        when(legacyExecutedScenarioNotMatchingAnyScenario_11.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-1, "Preparing user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Login with selected user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(200, "Adding a few items to cart"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(500, "Logout")
        ));

        when(legacyExecutedScenarioNotMatchingAnyScenario_12.getId()).thenReturn(12L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_12.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioNotMatchingAnyScenario_11)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_12.shareTheSameStepsAs(legacyExecutedScenarioNotMatchingAnyScenario_11)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_12.getFeatureFile()).thenReturn("new-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_12.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("New scenario name", List.of("1", "5", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_12.getSeverity()).thenReturn("severity-1");

        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getId()).thenReturn(13L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getStartDateTime()).thenReturn(dateFormat.parse("13/03/2021 13:03:21"));
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getFeatureName()).thenReturn("New feature file");
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getFeatureFile()).thenReturn("new-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("New scenario name", List.of("1", "5", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getSeverity()).thenReturn("severity-2");
        when(legacyExecutedScenarioNotMatchingAnyScenario_13.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-1, "Preparing user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Login with selected user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(200, "Adding a few items to cart"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(500, "Logout")
        ));

        // Executed scenarios not matching any scenario (2)
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getId()).thenReturn(21L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.matchesScenario(any(com.decathlon.ara.domain.Scenario.class), any(Source.class))).thenReturn(false);
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getStartDateTime()).thenReturn(dateFormat.parse("21/01/2021 21:01:21"));
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getFeatureName()).thenReturn("Another feature file");
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getFeatureFile()).thenReturn("another-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Another scenario name", List.of("1", "2", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getSeverity()).thenReturn("severity-3");
        when(legacyExecutedScenarioNotMatchingAnyScenario_21.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100, "Clicking on the 'Create user' button"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(200, "Filling the registration form"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(300, "Save the user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(400, "Login with the newly created user")
        ));

        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getId()).thenReturn(22L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioNotMatchingAnyScenario_21)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.shareTheSameStepsAs(legacyExecutedScenarioNotMatchingAnyScenario_21)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getStartDateTime()).thenReturn(dateFormat.parse("22/02/2021 22:02:21"));
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getFeatureName()).thenReturn("Another feature file");
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getFeatureFile()).thenReturn("another-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Another scenario name", List.of("1", "2", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getSeverity()).thenReturn("severity-3");
        when(legacyExecutedScenarioNotMatchingAnyScenario_22.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100, "Clicking on the 'Create user' button"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(200, "Filling the registration form"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(300, "Save the user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(400, "Login with the newly created user")
        ));

        when(legacyExecutedScenarioNotMatchingAnyScenario_23.getId()).thenReturn(23L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioNotMatchingAnyScenario_21)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.shareTheSameStepsAs(legacyExecutedScenarioNotMatchingAnyScenario_21)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioNotMatchingAnyScenario_22)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.shareTheSameStepsAs(legacyExecutedScenarioNotMatchingAnyScenario_22)).thenReturn(true);
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.getFeatureFile()).thenReturn("another-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Another scenario name", List.of("1", "2", "6")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_23.getSeverity()).thenReturn("severity-3");

        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getId()).thenReturn(24L);
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getStartDateTime()).thenReturn(dateFormat.parse("24/04/2021 24:04:21"));
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getFeatureName()).thenReturn("Another feature file");
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getFeatureFile()).thenReturn("another-feature-file.json");
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Another scenario name", List.of("1", "2", "3")));
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getSeverity()).thenReturn("severity-3");
        when(legacyExecutedScenarioNotMatchingAnyScenario_24.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100, "Clicking on the 'Create user' button"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(200, "Filling the registration form"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(300, "Save the user"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(400, "Login with the newly created user")
        ));

        // Executed scenarios matching scenario 1
        when(legacyExecutedScenarioMatchingScenario1_1.getId()).thenReturn(111L);
        when(legacyExecutedScenarioMatchingScenario1_1.matchesScenario(legacyScenario1, legacySource1)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario1_1.getStartDateTime()).thenReturn(dateFormat.parse("01/01/2021 01:01:21"));
        when(legacyExecutedScenarioMatchingScenario1_1.getFeatureName()).thenReturn("Feature file 1");
        when(legacyExecutedScenarioMatchingScenario1_1.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyExecutedScenarioMatchingScenario1_1.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("1", "2", "3")));
        when(legacyExecutedScenarioMatchingScenario1_1.getSeverity()).thenReturn("severity-2");
        when(legacyExecutedScenarioMatchingScenario1_1.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "@Before Hooks.beforeScenario(Scenario)"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(323, "Given a delivery content"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(324, "| SKU       | Quantity |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(325, "| NORMALSKU |        1 |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(326, "And a delivery to \"some_address\""),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(327, "And an existing home delivery"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(328, "And a delivery start time for tomorrow"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(329, "When I call the API to find the best scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(330, "Then there is no scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "@After SapSteps.afterScenario()"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100001, "@After Hooks.afterScenario(Scenario)")
        ));

        when(legacyExecutedScenarioMatchingScenario1_2.getId()).thenReturn(112L);
        when(legacyExecutedScenarioMatchingScenario1_2.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioMatchingScenario1_1)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario1_2.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario1_1)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario1_2.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyExecutedScenarioMatchingScenario1_2.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("1", "2", "3")));
        when(legacyExecutedScenarioMatchingScenario1_2.getSeverity()).thenReturn("severity-2");

        when(legacyExecutedScenarioMatchingScenario1_3.getId()).thenReturn(113L);
        when(legacyExecutedScenarioMatchingScenario1_3.getStartDateTime()).thenReturn(dateFormat.parse("01/03/2021 01:03:21"));
        when(legacyExecutedScenarioMatchingScenario1_3.getFeatureName()).thenReturn("Feature file 1");
        when(legacyExecutedScenarioMatchingScenario1_3.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyExecutedScenarioMatchingScenario1_3.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("1", "2", "3")));
        when(legacyExecutedScenarioMatchingScenario1_3.getSeverity()).thenReturn("severity-2");
        when(legacyExecutedScenarioMatchingScenario1_3.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "@Before Hooks.beforeScenario(Scenario)"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(323, "Given a delivery content"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(324, "| SKU       | Quantity |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(325, "| NORMALSKU |        1 |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(326, "And a delivery to \"some_address\""),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(327, "And an existing home delivery"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(328, "And a delivery start time for tomorrow"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(329, "When I call the API to find the best scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(330, "Then there is no scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(331, "An additional step"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "@After SapSteps.afterScenario()"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100001, "@After Hooks.afterScenario(Scenario)")
        ));

        when(legacyExecutedScenarioMatchingScenario1_4.getId()).thenReturn(114L);
        when(legacyExecutedScenarioMatchingScenario1_4.getStartDateTime()).thenReturn(dateFormat.parse("01/04/2021 01:04:21"));
        when(legacyExecutedScenarioMatchingScenario1_4.getFeatureName()).thenReturn("Feature file 1");
        when(legacyExecutedScenarioMatchingScenario1_4.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyExecutedScenarioMatchingScenario1_4.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("1", "2", "3")));
        when(legacyExecutedScenarioMatchingScenario1_4.getSeverity()).thenReturn("severity-1");
        when(legacyExecutedScenarioMatchingScenario1_4.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "@Before Hooks.beforeScenario(Scenario)"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(323, "Given a delivery content"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(324, "| SKU       | Quantity |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(325, "| NORMALSKU |        1 |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(326, "And a delivery to \"some_address\""),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(327, "And an existing home delivery"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(328, "And a delivery start time for tomorrow"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(329, "When I call the API to find the best scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(330, "Then there is no scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "@After SapSteps.afterScenario()"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100001, "@After Hooks.afterScenario(Scenario)")
        ));

        when(legacyExecutedScenarioMatchingScenario1_5.getId()).thenReturn(115L);
        when(legacyExecutedScenarioMatchingScenario1_5.getStartDateTime()).thenReturn(dateFormat.parse("01/05/2021 01:05:21"));
        when(legacyExecutedScenarioMatchingScenario1_5.getFeatureName()).thenReturn("Feature file 1");
        when(legacyExecutedScenarioMatchingScenario1_5.getFeatureFile()).thenReturn("feature-file1.json");
        when(legacyExecutedScenarioMatchingScenario1_5.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario name n° 1", List.of("4", "5")));
        when(legacyExecutedScenarioMatchingScenario1_5.getSeverity()).thenReturn("severity-1");
        when(legacyExecutedScenarioMatchingScenario1_5.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "@Before Hooks.beforeScenario(Scenario)"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(323, "Given a delivery content"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(324, "| SKU       | Quantity |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(325, "| NORMALSKU |        1 |"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(326, "And a delivery to \"some_address\""),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(327, "And an existing home delivery"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(328, "And a delivery start time for tomorrow"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(329, "When I call the API to find the best scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(330, "Then there is no scenario"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "@After SapSteps.afterScenario()"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100001, "@After Hooks.afterScenario(Scenario)")
        ));

        // Executed scenarios matching scenario 2
        when(legacyExecutedScenarioMatchingScenario2_1.getId()).thenReturn(221L);
        when(legacyExecutedScenarioMatchingScenario2_1.matchesScenario(legacyScenario2, legacySource2)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario2_1.matchesScenario(legacyScenario1, legacySource2)).thenReturn(false);
        when(legacyExecutedScenarioMatchingScenario2_1.getStartDateTime()).thenReturn(dateFormat.parse("02/01/2021 02:01:21"));
        when(legacyExecutedScenarioMatchingScenario2_1.getFeatureName()).thenReturn("feature file 2");
        when(legacyExecutedScenarioMatchingScenario2_1.getFeatureFile()).thenReturn("/feature-file2.json");
        when(legacyExecutedScenarioMatchingScenario2_1.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("scenario name 2", List.of("4", "5", "6")));
        when(legacyExecutedScenarioMatchingScenario2_1.getSeverity()).thenReturn("severity-2");
        when(legacyExecutedScenarioMatchingScenario2_1.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "<Pre-Request Script>"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-1, "POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(0, "Status code is 200"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Is the response valid and has a body ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Does data.id contain any Data ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "<Test Script>")
        ));

        when(legacyExecutedScenarioMatchingScenario2_2.getId()).thenReturn(222L);
        when(legacyExecutedScenarioMatchingScenario2_2.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario2_1)).thenReturn(false);
        when(legacyExecutedScenarioMatchingScenario2_2.getStartDateTime()).thenReturn(dateFormat.parse("02/02/2021 02:02:21"));
        when(legacyExecutedScenarioMatchingScenario2_2.getFeatureName()).thenReturn("feature file 2");
        when(legacyExecutedScenarioMatchingScenario2_2.getFeatureFile()).thenReturn("/feature-file2.json");
        when(legacyExecutedScenarioMatchingScenario2_2.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("scenario name 2", List.of("4", "5", "6")));
        when(legacyExecutedScenarioMatchingScenario2_2.getSeverity()).thenReturn("severity-2");
        when(legacyExecutedScenarioMatchingScenario2_2.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-100000, "<Pre-Request Script>"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(-1, "POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Is the response valid and has a body ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Does data.id contain any Data ?"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(100000, "<Test Script>")
        ));

        when(legacyExecutedScenarioMatchingScenario2_3.getId()).thenReturn(223L);
        when(legacyExecutedScenarioMatchingScenario2_3.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario2_1)).thenReturn(false);
        when(legacyExecutedScenarioMatchingScenario2_3.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioMatchingScenario2_2)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario2_3.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario2_2)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario2_3.getFeatureFile()).thenReturn("/feature-file2.json");
        when(legacyExecutedScenarioMatchingScenario2_3.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("scenario name 2", List.of("4", "5", "6")));
        when(legacyExecutedScenarioMatchingScenario2_3.getSeverity()).thenReturn("severity-2");

        // Executed scenarios matching scenario 3
        when(legacyExecutedScenarioMatchingScenario3_1.getId()).thenReturn(331L);
        when(legacyExecutedScenarioMatchingScenario3_1.matchesScenario(legacyScenario3, legacySource3)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_1.matchesScenario(legacyScenario1, legacySource3)).thenReturn(false);
        when(legacyExecutedScenarioMatchingScenario3_1.matchesScenario(legacyScenario2, legacySource3)).thenReturn(false);
        when(legacyExecutedScenarioMatchingScenario3_1.getStartDateTime()).thenReturn(dateFormat.parse("03/01/2021 03:01:21"));
        when(legacyExecutedScenarioMatchingScenario3_1.getFeatureName()).thenReturn("feature file 3 in tests");
        when(legacyExecutedScenarioMatchingScenario3_1.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_1.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "4", "6")));
        when(legacyExecutedScenarioMatchingScenario3_1.getSeverity()).thenReturn("severity-3");
        when(legacyExecutedScenarioMatchingScenario3_1.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Preparing for data fetching"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Calling the data API [UPDATED]"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Successfully retrieving the data")
        ));

        when(legacyExecutedScenarioMatchingScenario3_2.getId()).thenReturn(332L);
        when(legacyExecutedScenarioMatchingScenario3_2.getStartDateTime()).thenReturn(dateFormat.parse("03/02/2021 03:02:21"));
        when(legacyExecutedScenarioMatchingScenario3_2.getFeatureName()).thenReturn("feature file 3 in tests");
        when(legacyExecutedScenarioMatchingScenario3_2.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_2.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "6")));
        when(legacyExecutedScenarioMatchingScenario3_2.getSeverity()).thenReturn("severity-3");
        when(legacyExecutedScenarioMatchingScenario3_2.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Preparing for data fetching"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Calling the data API"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Successfully retrieving the data")
        ));

        when(legacyExecutedScenarioMatchingScenario3_3.getId()).thenReturn(333L);
        when(legacyExecutedScenarioMatchingScenario3_3.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioMatchingScenario3_2)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_3.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario3_2)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_3.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_3.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "6")));
        when(legacyExecutedScenarioMatchingScenario3_3.getSeverity()).thenReturn("severity-3");

        when(legacyExecutedScenarioMatchingScenario3_4.getId()).thenReturn(334L);
        when(legacyExecutedScenarioMatchingScenario3_4.getStartDateTime()).thenReturn(dateFormat.parse("03/04/2021 03:04:21"));
        when(legacyExecutedScenarioMatchingScenario3_4.getFeatureName()).thenReturn("feature file 3 in tests");
        when(legacyExecutedScenarioMatchingScenario3_4.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_4.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "4", "6")));
        when(legacyExecutedScenarioMatchingScenario3_4.getSeverity()).thenReturn("severity-1");
        when(legacyExecutedScenarioMatchingScenario3_4.getStatelessScenarioSteps()).thenReturn(List.of(
                new com.decathlon.ara.domain.Scenario.ScenarioStep(1, "Preparing for data fetching"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(2, "Calling the data API"),
                new com.decathlon.ara.domain.Scenario.ScenarioStep(3, "Successfully retrieving the data")
        ));

        when(legacyExecutedScenarioMatchingScenario3_5.getId()).thenReturn(335L);
        when(legacyExecutedScenarioMatchingScenario3_5.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioMatchingScenario3_4)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_5.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario3_4)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_5.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_5.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "4", "6")));
        when(legacyExecutedScenarioMatchingScenario3_5.getSeverity()).thenReturn("severity-1");

        when(legacyExecutedScenarioMatchingScenario3_6.getId()).thenReturn(336L);
        when(legacyExecutedScenarioMatchingScenario3_6.shareTheSameFunctionalityCodesAs(legacyExecutedScenarioMatchingScenario3_4)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_6.shareTheSameStepsAs(legacyExecutedScenarioMatchingScenario3_4)).thenReturn(true);
        when(legacyExecutedScenarioMatchingScenario3_6.getFeatureFile()).thenReturn("tests/feature-file3.json");
        when(legacyExecutedScenarioMatchingScenario3_6.getNameWithoutCodesAndFunctionalityCodesFromScenarioName()).thenReturn(Pair.of("Scenario n° 3", List.of("2", "4", "6")));
        when(legacyExecutedScenarioMatchingScenario3_6.getSeverity()).thenReturn("severity-1");

        when(legacyType11.getSource()).thenReturn(legacySource1);
        when(legacyType12.getSource()).thenReturn(legacySource1);
        when(legacySource1.getTechnology()).thenReturn(Technology.CUCUMBER);
        when(legacySource1.getCode()).thenReturn("source-1");
        when(legacySource1.getName()).thenReturn("Source 1");
        when(legacySource1.getDefaultBranch()).thenReturn("default-branch-1");
        when(legacySource1.getVcsUrl()).thenReturn("https://repository.com/project/{{branch}}/tests");
        when(legacyType21.getSource()).thenReturn(legacySource2);
        when(legacyType22.getSource()).thenReturn(legacySource2);
        when(legacySource2.getTechnology()).thenReturn(Technology.POSTMAN);
        when(legacySource2.getCode()).thenReturn("source-2");
        when(legacySource2.getName()).thenReturn("Source 2");
        when(legacySource2.getDefaultBranch()).thenReturn("default-branch-2");
        when(legacySource2.getVcsUrl()).thenReturn("https://repository.com/project/{{branch}}/other-tests/");
        when(legacyType3.getSource()).thenReturn(legacySource3);
        when(legacySource3.getTechnology()).thenReturn(Technology.CYPRESS);
        when(legacySource3.getCode()).thenReturn("source-3");
        when(legacySource3.getName()).thenReturn("Source 3");
        when(legacySource3.getDefaultBranch()).thenReturn("default-branch-3");
        when(legacySource3.getVcsUrl()).thenReturn("https://repository.com/project/{{branch}}/multiple-branch-occurrence/{{branch}}");
        when(legacyType4.getSource()).thenReturn(legacySource4);
        when(legacySource4.getTechnology()).thenReturn(Technology.GENERIC);
        when(legacySource4.getCode()).thenReturn("source-4");
        when(legacySource4.getName()).thenReturn("Source 4");
        when(legacySource4.getDefaultBranch()).thenReturn("default-branch-4");
        when(legacySource4.getVcsUrl()).thenReturn("https://repository.com/project/no-branch-to-replace/tests/");

        when(legacyCycleDefinition1.getBranch()).thenReturn("branch-1");
        when(legacyCycleDefinition2.getBranch()).thenReturn("branch-2");

        when(migrationFeature1.getCode()).thenReturn("1");
        when(migrationFeature2.getCode()).thenReturn("2");
        when(migrationFeature3.getCode()).thenReturn("3");
        when(migrationFeature4.getCode()).thenReturn("4");
        when(migrationFeature5.getCode()).thenReturn("5");
        when(migrationFeature6.getCode()).thenReturn("6");

        when(migrationTag1.getId()).thenReturn(new CodeWithProjectId().withProject(migrationProject).withCode("tag-1"));
        when(migrationTag2.getId()).thenReturn(new CodeWithProjectId().withProject(migrationProject).withCode("tag-2"));
        when(migrationTag3.getId()).thenReturn(new CodeWithProjectId().withProject(migrationProject).withCode("tag-3"));

        when(migrationScenarioRepository.save(any(Scenario.class))).thenReturn(
                newScenario_1,
                newScenario_2,
                migratedScenario1,
                migratedScenario2,
                migratedScenario3,
                migratedScenario4
        );
        when(newScenario_1.withVersions(anyList())).thenReturn(newScenario_1);
        when(newScenario_2.withVersions(anyList())).thenReturn(newScenario_2);
        when(migratedScenario1.withVersions(anyList())).thenReturn(migratedScenario1);
        when(migratedScenario2.withVersions(anyList())).thenReturn(migratedScenario2);
        when(migratedScenario3.withVersions(anyList())).thenReturn(migratedScenario3);
        when(migratedScenario4.withVersions(anyList())).thenReturn(migratedScenario4);

        // Then
        List<Scenario> scenarios = scenarioMigrationService.migrateScenarios(
                legacyProject,
                migrationProject,
                List.of(
                        migrationFeature1,
                        migrationFeature2,
                        migrationFeature3,
                        migrationFeature4,
                        migrationFeature5,
                        migrationFeature6
                ),
                List.of(migrationTag1, migrationTag2, migrationTag3),
                List.of(legacyExecution1, legacyExecution2)
        );
        assertThat(scenarios)
                .hasSize(6)
                .containsExactlyInAnyOrder(
                        newScenario_1,
                        newScenario_2,
                        migratedScenario1,
                        migratedScenario2,
                        migratedScenario3,
                        migratedScenario4
                );
        ArgumentCaptor<Scenario> scenarioArgumentCaptor = ArgumentCaptor.forClass(Scenario.class);
        verify(migrationScenarioRepository, times(6)).save(scenarioArgumentCaptor.capture());
        List<Scenario> migratedScenarios = scenarioArgumentCaptor.getAllValues();
        assertThat(migratedScenarios)
                .hasSize(6)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "type.id.project",
                        "type.id.code",
                        "type.technology",
                        "type.name",
                        "type.description",
                        "tags"
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                migrationProject,
                                "s-1",
                                "Scenario name n° 1",
                                migrationProject,
                                "source-1",
                                "CUCUMBER",
                                "Source 1",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of(migrationTag1, migrationTag2, migrationTag3)
                        ),
                        tuple(
                                migrationProject,
                                "s-2",
                                "scenario name 2",
                                migrationProject,
                                "source-2",
                                "POSTMAN",
                                "Source 2",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of(migrationTag2, migrationTag1)
                        ),
                        tuple(
                                migrationProject,
                                "s-3",
                                "Scenario n° 3",
                                migrationProject,
                                "source-3",
                                "CYPRESS",
                                "Source 3",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of(migrationTag2, migrationTag3, migrationTag1)
                        ),
                        tuple(
                                migrationProject,
                                "s-4",
                                "last scenario",
                                migrationProject,
                                "source-3",
                                "CYPRESS",
                                "Source 3",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of()
                        ),
                        tuple(
                                migrationProject,
                                "e-11",
                                "New scenario name",
                                migrationProject,
                                "source-4",
                                "GENERIC",
                                "Source 4",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of()
                        ),
                        tuple(
                                migrationProject,
                                "e-21",
                                "Another scenario name",
                                migrationProject,
                                "source-4",
                                "GENERIC",
                                "Source 4",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                List.of()
                        )
                );

        ArgumentCaptor<List<ScenarioVersion>> scenarioVersionsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationScenarioVersionRepository, times(6)).saveAll(scenarioVersionsArgumentCaptor.capture());
        List<ScenarioVersion> migratedVersions = scenarioVersionsArgumentCaptor.getAllValues()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        // Scenario steps for executed scenarios not matching any scenario (1)
        List<ScenarioStep> scenarioStepsForExecutedScenariosNotMatchingAnyScenario_1 = List.of(
                new ScenarioStep().withLine(-1).withContent("Preparing user"),
                new ScenarioStep().withLine(1).withContent("Login with selected user"),
                new ScenarioStep().withLine(200).withContent("Adding a few items to cart"),
                new ScenarioStep().withLine(500).withContent("Logout")
        );
        // Scenario steps for executed scenarios not matching any scenario (2)
        List<ScenarioStep> scenarioStepsForExecutedScenariosNotMatchingAnyScenario_2 = List.of(
                new ScenarioStep().withLine(100).withContent("Clicking on the 'Create user' button"),
                new ScenarioStep().withLine(200).withContent("Filling the registration form"),
                new ScenarioStep().withLine(300).withContent("Save the user"),
                new ScenarioStep().withLine(400).withContent("Login with the newly created user")
        );

        // Scenario steps for scenarios 1
        List<ScenarioStep> scenarioStepsForScenario1 = List.of(
                new ScenarioStep().withLine(323).withContent("Given a delivery content"),
                new ScenarioStep().withLine(324).withContent("| SKU       | Quantity |"),
                new ScenarioStep().withLine(325).withContent("| NORMALSKU |        1 |"),
                new ScenarioStep().withLine(326).withContent("And a delivery to \"some_address\""),
                new ScenarioStep().withLine(327).withContent("And an existing home delivery"),
                new ScenarioStep().withLine(328).withContent("And a delivery start time for tomorrow"),
                new ScenarioStep().withLine(329).withContent("When I call the API to find the best scenario"),
                new ScenarioStep().withLine(330).withContent("Then there is no scenario")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario1_1 = List.of(
                new ScenarioStep().withLine(-100000).withContent("@Before Hooks.beforeScenario(Scenario)"),
                new ScenarioStep().withLine(323).withContent("Given a delivery content"),
                new ScenarioStep().withLine(324).withContent("| SKU       | Quantity |"),
                new ScenarioStep().withLine(325).withContent("| NORMALSKU |        1 |"),
                new ScenarioStep().withLine(326).withContent("And a delivery to \"some_address\""),
                new ScenarioStep().withLine(327).withContent("And an existing home delivery"),
                new ScenarioStep().withLine(328).withContent("And a delivery start time for tomorrow"),
                new ScenarioStep().withLine(329).withContent("When I call the API to find the best scenario"),
                new ScenarioStep().withLine(330).withContent("Then there is no scenario"),
                new ScenarioStep().withLine(100000).withContent("@After SapSteps.afterScenario()"),
                new ScenarioStep().withLine(100001).withContent("@After Hooks.afterScenario(Scenario)")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario1_2 = List.of(
                new ScenarioStep().withLine(-100000).withContent("@Before Hooks.beforeScenario(Scenario)"),
                new ScenarioStep().withLine(323).withContent("Given a delivery content"),
                new ScenarioStep().withLine(324).withContent("| SKU       | Quantity |"),
                new ScenarioStep().withLine(325).withContent("| NORMALSKU |        1 |"),
                new ScenarioStep().withLine(326).withContent("And a delivery to \"some_address\""),
                new ScenarioStep().withLine(327).withContent("And an existing home delivery"),
                new ScenarioStep().withLine(328).withContent("And a delivery start time for tomorrow"),
                new ScenarioStep().withLine(329).withContent("When I call the API to find the best scenario"),
                new ScenarioStep().withLine(330).withContent("Then there is no scenario"),
                new ScenarioStep().withLine(331).withContent("An additional step"),
                new ScenarioStep().withLine(100000).withContent("@After SapSteps.afterScenario()"),
                new ScenarioStep().withLine(100001).withContent("@After Hooks.afterScenario(Scenario)")
        );

        // Scenario steps for scenarios 2
        List<ScenarioStep> scenarioStepsForScenario2 = List.of(
                new ScenarioStep().withLine(-100000).withContent("<Pre-Request Script>"),
                new ScenarioStep().withLine(-1).withContent("POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new ScenarioStep().withLine(0).withContent("Status code is 200"),
                new ScenarioStep().withLine(1).withContent("Is the response valid and has a body ?"),
                new ScenarioStep().withLine(2).withContent("Does data.id contain any Data ?"),
                new ScenarioStep().withLine(100000).withContent("<Test Script>")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario2_1 = List.of(
                new ScenarioStep().withLine(-100000).withContent("<Pre-Request Script>"),
                new ScenarioStep().withLine(-1).withContent("POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new ScenarioStep().withLine(0).withContent("Status code is 200"),
                new ScenarioStep().withLine(1).withContent("Is the response valid and has a body ?"),
                new ScenarioStep().withLine(2).withContent("Does data.id contain any Data ?"),
                new ScenarioStep().withLine(100000).withContent("<Test Script>")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario2_2 = List.of(
                new ScenarioStep().withLine(-100000).withContent("<Pre-Request Script>"),
                new ScenarioStep().withLine(-1).withContent("POST http://{{some_url}}:{{some_port}}/rest/model/com/company/orders"),
                new ScenarioStep().withLine(1).withContent("Is the response valid and has a body ?"),
                new ScenarioStep().withLine(2).withContent("Does data.id contain any Data ?"),
                new ScenarioStep().withLine(100000).withContent("<Test Script>")
        );

        // Scenario steps for scenarios 3
        List<ScenarioStep> scenarioStepsForScenario3 = List.of(
                new ScenarioStep().withLine(1).withContent("Preparing for data fetching"),
                new ScenarioStep().withLine(2).withContent("Calling the data API"),
                new ScenarioStep().withLine(3).withContent("Successfully retrieving the data")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario3_1 = List.of(
                new ScenarioStep().withLine(1).withContent("Preparing for data fetching"),
                new ScenarioStep().withLine(2).withContent("Calling the data API [UPDATED]"),
                new ScenarioStep().withLine(3).withContent("Successfully retrieving the data")
        );
        List<ScenarioStep> scenarioStepsForExecutedScenario3_2 = List.of(
                new ScenarioStep().withLine(1).withContent("Preparing for data fetching"),
                new ScenarioStep().withLine(2).withContent("Calling the data API"),
                new ScenarioStep().withLine(3).withContent("Successfully retrieving the data")
        );

        // Scenario steps for scenarios 4
        List<ScenarioStep> scenarioStepsForScenario4 = List.of(
                new ScenarioStep().withLine(1).withContent("Connecting user to its account"),
                new ScenarioStep().withLine(2).withContent("Selecting item1 and item2"),
                new ScenarioStep().withLine(3).withContent("Adding selection to basket"),
                new ScenarioStep().withLine(4).withContent("Paying the command"),
                new ScenarioStep().withLine(5).withContent("Disconnecting user")
        );
        assertThat(migratedVersions)
                .hasSize(22) // 4 (default branches) + 8 (branch 1) + 10 (branch 2) = 22
                .extracting(
                        "id.scenario",
                        "id.commitSHA",
                        "steps",
                        "coveredFeatures",
                        "ignored",
                        "severity.id.project",
                        "severity.id.code",
                        "branch.id.project",
                        "branch.id.code",
                        "fileName",
                        "fileUrl"
                )
                .containsExactlyInAnyOrder(
                        // New scenario versions (1) -> 3 versions (1 + 2)
                        tuple(
                                newScenario_1,
                                "generated_SHA-e-branch-1-11",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_1,
                                List.of(migrationFeature1, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "branch-1",
                                "New feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/new-feature-file.json"
                        ),
                        tuple(
                                newScenario_1,
                                "generated_SHA-e-branch-2-11",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_1,
                                List.of(migrationFeature1, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "branch-2",
                                "New feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/new-feature-file.json"
                        ),
                        tuple(
                                newScenario_1,
                                "generated_SHA-e-branch-2-13",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_1,
                                List.of(migrationFeature1, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-2",
                                "New feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/new-feature-file.json"
                        ),
                        // New scenario versions (2) -> 4 versions (2 + 2)
                        tuple(
                                newScenario_2,
                                "generated_SHA-e-branch-1-22",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_2,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-1",
                                "Another feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/another-feature-file.json"
                        ),
                        tuple(
                                newScenario_2,
                                "generated_SHA-e-branch-1-24",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_2,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-1",
                                "Another feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/another-feature-file.json"
                        ),
                        tuple(
                                newScenario_2,
                                "generated_SHA-e-branch-2-21",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_2,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-2",
                                "Another feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/another-feature-file.json"
                        ),
                        tuple(
                                newScenario_2,
                                "generated_SHA-e-branch-2-24",
                                scenarioStepsForExecutedScenariosNotMatchingAnyScenario_2,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-2",
                                "Another feature file",
                                "https://repository.com/project/no-branch-to-replace/tests/another-feature-file.json"
                        ),
                        // Scenario 1 versions -> 5 versions (2 + 2 + 1)
                        tuple(
                                migratedScenario1,
                                "generated_SHA-e-branch-1-113",
                                scenarioStepsForExecutedScenario1_2,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                true,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-1",
                                "Feature file 1",
                                "https://repository.com/project/branch-1/tests/feature-file1.json"
                        ),
                        tuple(
                                migratedScenario1,
                                "generated_SHA-e-branch-1-114",
                                scenarioStepsForExecutedScenario1_1,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                true,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "branch-1",
                                "Feature file 1",
                                "https://repository.com/project/branch-1/tests/feature-file1.json"
                        ),
                        tuple(
                                migratedScenario1,
                                "generated_SHA-e-branch-2-111",
                                scenarioStepsForExecutedScenario1_1,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                true,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-2",
                                "Feature file 1",
                                "https://repository.com/project/branch-2/tests/feature-file1.json"
                        ),
                        tuple(
                                migratedScenario1,
                                "generated_SHA-e-branch-2-115",
                                scenarioStepsForExecutedScenario1_1,
                                List.of(migrationFeature4, migrationFeature5),
                                true,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "branch-2",
                                "Feature file 1",
                                "https://repository.com/project/branch-2/tests/feature-file1.json"
                        ),
                        tuple(
                                migratedScenario1,
                                "generated_SHA-s-default-branch-1-1",
                                scenarioStepsForScenario1,
                                List.of(migrationFeature1, migrationFeature2, migrationFeature3),
                                true,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "default-branch-1",
                                "Feature file 1",
                                "https://repository.com/project/default-branch-1/tests/feature-file1.json"
                        ),
                        // Scenario 2 versions -> 4 versions (1 + 2 + 1)
                        tuple(
                                migratedScenario2,
                                "generated_SHA-e-branch-1-221",
                                scenarioStepsForExecutedScenario2_1,
                                List.of(migrationFeature4, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-1",
                                "feature file 2",
                                "https://repository.com/project/branch-1/other-tests/feature-file2.json"
                        ),
                        tuple(
                                migratedScenario2,
                                "generated_SHA-e-branch-2-221",
                                scenarioStepsForExecutedScenario2_1,
                                List.of(migrationFeature4, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-2",
                                "feature file 2",
                                "https://repository.com/project/branch-2/other-tests/feature-file2.json"
                        ),
                        tuple(
                                migratedScenario2,
                                "generated_SHA-e-branch-2-222",
                                scenarioStepsForExecutedScenario2_2,
                                List.of(migrationFeature4, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "branch-2",
                                "feature file 2",
                                "https://repository.com/project/branch-2/other-tests/feature-file2.json"
                        ),
                        tuple(
                                migratedScenario2,
                                "generated_SHA-s-default-branch-2-2",
                                scenarioStepsForScenario2,
                                List.of(migrationFeature4, migrationFeature5, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-2",
                                migrationProject,
                                "default-branch-2",
                                "feature file 2",
                                "https://repository.com/project/default-branch-2/other-tests/feature-file2.json"
                        ),
                        // Scenario 3 versions -> 5 versions (2 + 2 + 1)
                        tuple(
                                migratedScenario3,
                                "generated_SHA-e-branch-1-331",
                                scenarioStepsForExecutedScenario3_1,
                                List.of(migrationFeature2, migrationFeature4, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-1",
                                "feature file 3 in tests",
                                "https://repository.com/project/branch-1/multiple-branch-occurrence/branch-1/tests/feature-file3.json"
                        ),
                        tuple(
                                migratedScenario3,
                                "generated_SHA-e-branch-1-332",
                                scenarioStepsForExecutedScenario3_2,
                                List.of(migrationFeature2, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-1",
                                "feature file 3 in tests",
                                "https://repository.com/project/branch-1/multiple-branch-occurrence/branch-1/tests/feature-file3.json"
                        ),
                        tuple(
                                migratedScenario3,
                                "generated_SHA-e-branch-2-331",
                                scenarioStepsForExecutedScenario3_1,
                                List.of(migrationFeature2, migrationFeature4, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "branch-2",
                                "feature file 3 in tests",
                                "https://repository.com/project/branch-2/multiple-branch-occurrence/branch-2/tests/feature-file3.json"
                        ),
                        tuple(
                                migratedScenario3,
                                "generated_SHA-e-branch-2-334",
                                scenarioStepsForExecutedScenario3_2,
                                List.of(migrationFeature2, migrationFeature4, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "branch-2",
                                "feature file 3 in tests",
                                "https://repository.com/project/branch-2/multiple-branch-occurrence/branch-2/tests/feature-file3.json"
                        ),
                        tuple(
                                migratedScenario3,
                                "generated_SHA-s-default-branch-3-3",
                                scenarioStepsForScenario3,
                                List.of(migrationFeature2, migrationFeature4, migrationFeature6),
                                false,
                                migrationProject,
                                "severity-3",
                                migrationProject,
                                "default-branch-3",
                                "feature file 3 in tests",
                                "https://repository.com/project/default-branch-3/multiple-branch-occurrence/default-branch-3/tests/feature-file3.json"
                        ),
                        // Scenario 4 version  -> 1 version  (0 + 0 + 1)
                        tuple(
                                migratedScenario4,
                                "generated_SHA-s-default-branch-3-4",
                                scenarioStepsForScenario4,
                                List.of(migrationFeature1, migrationFeature3, migrationFeature5),
                                true,
                                migrationProject,
                                "severity-1",
                                migrationProject,
                                "default-branch-3",
                                "Feature file 4 in other tests",
                                "https://repository.com/project/default-branch-3/multiple-branch-occurrence/default-branch-3/other-tests/feature-file4.json"
                        )
                );
    }
}
