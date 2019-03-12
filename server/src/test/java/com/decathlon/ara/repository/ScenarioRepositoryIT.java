package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ScenarioRepositoryIT {

    @Autowired
    private ScenarioRepository cut;

    private static Source sourceA() {
        return new Source().withProjectId(1).withCode("sourceA");
    }

    private static Source sourceB() {
        return new Source().withProjectId(1).withCode("sourceB");
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findAllWithFunctionalityErrors-allCombinations.xml" })
    public void findAllWithFunctionalityErrors_ShouldReturnAllNullEmptyOrWrongAssignations_WhenCalled() {
        // WHEN
        final List<ScenarioSummary> ignoreCounts = cut.findAllWithFunctionalityErrors(1);

        // THEN
        assertThat(ignoreCounts).containsOnly( // Ordered by source.code, featureName, name, line
                new ScenarioSummary(
                        Long.valueOf(6),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=0 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no attached functionality",
                        0, true, true,
                        null, null, null),
                new ScenarioSummary(
                        Long.valueOf(7),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=2 | wrongFunctionalityIds=some | countryCodes=existing | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> nonexistent functionality ID",
                        2, true, true,
                        "some", null, null),
                new ScenarioSummary(
                        Long.valueOf(8),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=null | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no (null) country",
                        1, false, true,
                        null, null, null),
                new ScenarioSummary(
                        Long.valueOf(9),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=empty | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no (empty) country",
                        1, false, true,
                        null, null, null),
                new ScenarioSummary(
                        Long.valueOf(10),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=some | severity=existing | wrongSeverityCode=null -> nonexistent country code",
                        1, true, true,
                        null, "some", null),
                new ScenarioSummary(
                        Long.valueOf(11),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=null | wrongSeverityCode=null -> no (null) severity",
                        1, true, false,
                        null, null, null),
                new ScenarioSummary(
                        Long.valueOf(12),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=empty | wrongSeverityCode=null -> no (empty) severity",
                        1, true, false,
                        null, null, null),
                new ScenarioSummary(
                        Long.valueOf(13),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=wrong | wrongSeverityCode=some -> nonexistent severity code",
                        1, true, true,
                        null, null, "some"));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findAllWithFunctionalityErrors-sort.xml" })
    public void findAllWithFunctionalityErrors_ShouldReturnSortedScenarios_WhenCalled() {
        // WHEN
        final List<ScenarioSummary> ignoreCounts = cut.findAllWithFunctionalityErrors(1);

        // THEN
        assertThat(ignoreCounts.stream()
                .map(s -> "" +
                        s.getSource().getCode() + " " +
                        s.getFeatureName() + " " +
                        s.getName() + " " +
                        s.getId())) // The DTO has no line
                .containsExactly( // Ordered by source.code, featureName, name, line
                        "sourceA featureB nameC 6", // Line 4
                        "sourceB featureA nameB 5", // Line 5
                        "sourceB featureB nameA 3", // Line 6
                        "sourceB featureB nameC 4", // Line 1
                        "sourceB featureB nameC 1", // Line 2
                        "sourceB featureB nameC 2"); // Line 3
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findIgnoreCounts.xml" })
    public void testFindIgnoreCounts() {
        // WHEN
        final List<ScenarioIgnoreCount> ignoreCounts = cut.findIgnoreCounts(1);

        // THEN
        assertThat(ignoreCounts).containsOnly(
                new ScenarioIgnoreCount(sourceA(), "medium", false, 1),
                new ScenarioIgnoreCount(sourceA(), "medium", true, 2),
                new ScenarioIgnoreCount(sourceB(), "medium", false, 3),
                new ScenarioIgnoreCount(sourceB(), "medium", true, 3),
                new ScenarioIgnoreCount(sourceB(), "high", true, 1));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findIgnoredScenarios.xml" })
    public void testFindIgnoredScenarios() {
        // WHEN
        final List<IgnoredScenario> ignoredScenarios = cut.findIgnoredScenarios(1);

        // THEN
        assertThat(ignoredScenarios).containsOnly(
                new IgnoredScenario(sourceA(), "f1", "Any", "high", "Name 5"),
                new IgnoredScenario(sourceB(), "f1", "Any", null, "Name 2"),
                new IgnoredScenario(sourceB(), "f1", "Any", null, "Name 3"),
                new IgnoredScenario(sourceB(), "f2", "Any", null, "Name 4"));
    }

}
