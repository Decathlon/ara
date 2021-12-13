/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.util.TestUtil;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@Disabled
@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
@Transactional
class ScenarioRepositoryIT {

    @Autowired
    private ScenarioRepository cut;

    private static Source sourceA() {
        Source source = new Source();
        source.setProjectId(1);
        source.setCode("sourceA");
        return source;
    }

    private static Source sourceB() {
        Source source = new Source();
        source.setProjectId(1);
        source.setCode("sourceB");
        return source;
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findAllWithFunctionalityErrors-allCombinations.xml" })
    void findAllWithFunctionalityErrors_ShouldReturnAllNullEmptyOrWrongAssignations_WhenCalled() {
        // WHEN
        final List<ScenarioSummary> ignoreCounts = cut.findAllWithFunctionalityErrors(1);

        // THEN
        assertThat(ignoreCounts).containsOnly( // Ordered by source.code, featureName, name, line
                scenarioSummary(
                        Long.valueOf(6),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=0 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no attached functionality",
                        0, true, true,
                        null, null, null),
                scenarioSummary(
                        Long.valueOf(7),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=2 | wrongFunctionalityIds=some | countryCodes=existing | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> nonexistent functionality ID",
                        2, true, true,
                        "some", null, null),
                scenarioSummary(
                        Long.valueOf(8),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=null | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no (null) country",
                        1, false, true,
                        null, null, null),
                scenarioSummary(
                        Long.valueOf(9),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=empty | wrongCountryCodes=null | severity=existing | wrongSeverityCode=null -> no (empty) country",
                        1, false, true,
                        null, null, null),
                scenarioSummary(
                        Long.valueOf(10),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=some | severity=existing | wrongSeverityCode=null -> nonexistent country code",
                        1, true, true,
                        null, "some", null),
                scenarioSummary(
                        Long.valueOf(11),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=null | wrongSeverityCode=null -> no (null) severity",
                        1, true, false,
                        null, null, null),
                scenarioSummary(
                        Long.valueOf(12),
                        sourceA(),
                        "any",
                        "any",
                        "functionalities=1 | wrongFunctionalityIds=null | countryCodes=existing | wrongCountryCodes=null | severity=empty | wrongSeverityCode=null -> no (empty) severity",
                        1, true, false,
                        null, null, null),
                scenarioSummary(
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
    void findAllWithFunctionalityErrors_ShouldReturnSortedScenarios_WhenCalled() {
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
    void testFindIgnoreCounts() {
        // WHEN
        final List<ScenarioIgnoreCount> ignoreCounts = cut.findIgnoreCounts(1);

        // THEN
        assertThat(ignoreCounts).containsOnly(
                scenarioIgnoreCount(sourceA(), "medium", false, 1),
                scenarioIgnoreCount(sourceA(), "medium", true, 2),
                scenarioIgnoreCount(sourceB(), "medium", false, 3),
                scenarioIgnoreCount(sourceB(), "medium", true, 3),
                scenarioIgnoreCount(sourceB(), "high", true, 1));
    }

    @Test
    @DatabaseSetup({ "/dbunit/ScenarioRepositoryIT-findIgnoredScenarios.xml" })
    void testFindIgnoredScenarios() {
        // WHEN
        final List<IgnoredScenario> ignoredScenarios = cut.findIgnoredScenarios(1);

        // THEN
        assertThat(ignoredScenarios).containsOnly(
                ignoredScenario(sourceA(), "f1", "Any", "high", "Name 5"),
                ignoredScenario(sourceB(), "f1", "Any", null, "Name 2"),
                ignoredScenario(sourceB(), "f1", "Any", null, "Name 3"),
                ignoredScenario(sourceB(), "f2", "Any", null, "Name 4"));
    }

    private IgnoredScenario ignoredScenario(Source source, String featureFile, String featureName, String severity, String name) {
        IgnoredScenario ignoredScenario = new IgnoredScenario();
        TestUtil.setField(ignoredScenario, "source", source);
        TestUtil.setField(ignoredScenario, "featureFile", featureFile);
        TestUtil.setField(ignoredScenario, "featureName", featureName);
        TestUtil.setField(ignoredScenario, "severity", severity);
        TestUtil.setField(ignoredScenario, "name", name);
        return ignoredScenario;
    }

    private ScenarioIgnoreCount scenarioIgnoreCount(Source source, String severityCode, boolean ignored, long count) {
        ScenarioIgnoreCount scenarioIgnoreCount = new ScenarioIgnoreCount();
        TestUtil.setField(scenarioIgnoreCount, "source", source);
        TestUtil.setField(scenarioIgnoreCount, "severityCode", severityCode);
        TestUtil.setField(scenarioIgnoreCount, "ignored", ignored);
        TestUtil.setField(scenarioIgnoreCount, "count", count);
        return scenarioIgnoreCount;
    }

    private ScenarioSummary scenarioSummary(Long id, Source source, String featureFile, String featureName, String name,
                                            int functionalityCount, boolean hasCountryCodes, boolean hasSeverity, String wrongFunctionalityIds,
                                            String wrongCountryCodes, String wrongSeverityCode) {
        ScenarioSummary scenarioSummary = new ScenarioSummary();
        TestUtil.setField(scenarioSummary, "id", id);
        TestUtil.setField(scenarioSummary, "source", source);
        TestUtil.setField(scenarioSummary, "featureFile", featureFile);
        TestUtil.setField(scenarioSummary, "featureName", featureName);
        TestUtil.setField(scenarioSummary, "name", name);
        TestUtil.setField(scenarioSummary, "functionalityCount", functionalityCount);
        TestUtil.setField(scenarioSummary, "hasCountryCodes", hasCountryCodes);
        TestUtil.setField(scenarioSummary, "hasSeverity", hasSeverity);
        TestUtil.setField(scenarioSummary, "wrongFunctionalityIds", wrongFunctionalityIds);
        TestUtil.setField(scenarioSummary, "wrongCountryCodes", wrongCountryCodes);
        TestUtil.setField(scenarioSummary, "wrongSeverityCode", wrongSeverityCode);
        return scenarioSummary;
    }

}
