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

package com.decathlon.ara.web.rest;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.firstPageOf10;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DistinctStatisticsDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import com.decathlon.ara.util.builder.ProblemPatternDTOBuilder;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@Disabled
@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@TestPropertySource(
        locations = "classpath:application-db-h2.properties")
@Transactional
@DatabaseSetup("/dbunit/full-small-fake-dataset.xml")
class ErrorResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private ErrorResource cut;

    private static CountryDTO country(String code) {
        return new CountryDTO(code, null);
    }

    private static TypeWithSourceDTO type() {
        final TypeWithSourceDTO typeWithSourceDTO = new TypeWithSourceDTO();
        typeWithSourceDTO.setCode("api");
        return typeWithSourceDTO;
    }

    @Test
    void testGetOne() {
        ResponseEntity<ErrorWithExecutedScenarioAndRunAndExecutionDTO> response = cut.getOne(PROJECT_CODE, 123);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ErrorWithExecutedScenarioAndRunAndExecutionDTO error = response.getBody();

        assertThat(error.getId()).isEqualTo(123);
        assertThat(error.getStep()).isEqualTo("Step 1");
        assertThat(error.getStepDefinition()).isEqualTo("^Step 1$");
        assertThat(error.getStepLine()).isEqualTo(49);
        assertThat(error.getException()).isEqualTo("Exception 5");

        final ExecutedScenarioWithRunAndExecutionDTO executedScenario = error.getExecutedScenario();
        assertThat(executedScenario.getFeatureFile()).isEqualTo("a.feature");
        assertThat(executedScenario.getFeatureName()).isEqualTo("Feature A");
        assertThat(executedScenario.getSeverity()).isEqualTo("high");
        assertThat(executedScenario.getName()).isEqualTo("Scenario c");
        assertThat(executedScenario.getLine()).isEqualTo(48);
        assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://screenshot.org/");
        assertThat(executedScenario.getVideoUrl()).isEqualTo("http://video.org/");
        assertThat(executedScenario.getContent()).isEqualTo("49:failed:Step 1");

        RunWithExecutionDTO run = executedScenario.getRun();
        assertThat(run.getId()).isEqualTo(12);
        assertThat(run.getCountry().getCode()).isEqualTo("cn");
        assertThat(run.getCountry().getName()).isEqualTo("China");
        assertThat(run.getType().getCode()).isEqualTo("api");
        assertThat(run.getType().getName()).isEqualTo("API");
        assertThat(run.getPlatform()).isEqualTo("euin");
        assertThat(run.getJobUrl()).isEqualTo("http://run.jobs.org/12/");

        ExecutionDTO execution = run.getExecution();
        assertThat(execution.getId()).isEqualTo(1);
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getBranch()).isEqualTo("develop");
        assertThat(execution.getRelease()).isEqualTo("1711");
        assertThat(execution.getVersion()).isEqualTo("1711.1709245958");
        assertThat(execution.getBuildDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 24, 11, 59, 58));
        assertThat(execution.getTestDateTime()).isEqualTo(timestamp(2017, Calendar.SEPTEMBER, 25, 11, 59, 58));
        assertThat(execution.getJobUrl()).isEqualTo("http://execution.jobs.org/1/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
    }

    @Test
    void testGetOneNonexistent() {
        ResponseEntity<ErrorWithExecutedScenarioAndRunAndExecutionDTO> response = cut.getOne(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetMatchingErrors() {
        // Most fields are EQUALS matches
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withFeatureFile("a.feature").build(), 9);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withFeatureName("Feature C").build(), 3);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withScenarioName("Scenario d").build(), 5);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withStep("Step 8").build(), 1);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withStepDefinition("^Step 7$").build(), 2);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withRelease("1710").build(), 4);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withCountry(country("cn")).build(), 14);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withType(type()).build(), 11);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withPlatform("euin2").build(), 5);

        // Exception is a LIKE matching
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("Exception 7").build(), 3);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("E%7").build(), 3);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("Exception").build(), 19);

        // Types with TRUE/FALSE are exact matches, null will exclude that type for matching algorithm
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withTypeIsBrowser(Boolean.TRUE).build(), 8);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withTypeIsBrowser(Boolean.FALSE).build(), 11);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withTypeIsMobile(Boolean.TRUE).build(), 0);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withTypeIsMobile(Boolean.FALSE).build(), 19);

        // Typical combinations of several patterns: it's an AND matching
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("Exc%2").withScenarioName("Scenario d").build(), 2);
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("Exc%2").withScenarioName("Scenario d").withStepDefinition("^Step 8$").build(), 1);

        // Nothing matches
        assertGetMatchingErrors(new ProblemPatternDTOBuilder().withException("Nothing matches").build(), 0);
    }

    @Test
    void testProblemsFromGetMatchingErrors() {
        // GIVEN
        ProblemPatternDTO pattern = new ProblemPatternDTOBuilder().withCountry(country("nl")).build();

        // WHEN
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO>> response =
                cut.getMatchingErrors(PROJECT_CODE, pattern, firstPageOf10());

        // THEN
        final Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO> page = response.getBody();
        assertThat(page.getTotalElements()).isEqualTo(5);

        final List<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO> content = page.getContent();
        assertThat(content.get(0).getId()).isEqualTo(211);
        assertThat(content.get(0).getProblems()).hasSize(1);
        assertThat(content.get(0).getProblems().get(0).getId()).isEqualTo(1001);
        assertThat(content.get(0).getProblems().get(0).getName()).isEqualTo("Step 2 needs rework");

        assertThat(content.get(1).getId()).isEqualTo(212);
        assertThat(content.get(1).getProblems()).isNull();

        assertThat(content.get(2).getId()).isEqualTo(221);
        assertThat(content.get(2).getProblems().stream().map(ProblemDTO::getId))
                .containsOnly(Long.valueOf(1001), Long.valueOf(1002));

        assertThat(content.get(3).getId()).isEqualTo(222);
        assertThat(content.get(3).getProblems()).hasSize(1);
        assertThat(content.get(3).getProblems().get(0).getId()).isEqualTo(1002);

        assertThat(content.get(4).getId()).isEqualTo(223);
        assertThat(content.get(4).getProblems()).hasSize(1);
        assertThat(content.get(4).getProblems().get(0).getId()).isEqualTo(1001);
    }

    @Test
    void testGetDistinctReleases() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "releases");

        // THEN
        assertThat(response.getBody().getReleases()).containsExactly("1710", "1711");
    }

    @Test
    void testGetDistinctCountries() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "countries");

        // THEN
        assertThat(response.getBody().getCountries()).hasSize(2);
        assertThat(response.getBody().getCountries().get(0).getCode()).isEqualTo("cn");
        assertThat(response.getBody().getCountries().get(0).getName()).isEqualTo("China");
        assertThat(response.getBody().getCountries().get(1).getCode()).isEqualTo("nl");
        assertThat(response.getBody().getCountries().get(1).getName()).isEqualTo("Netherlands");
    }

    @Test
    void testGetDistinctTypes() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "types");

        // THEN
        assertThat(response.getBody().getTypes()).hasSize(2);
        assertThat(response.getBody().getTypes().get(0).getCode()).isEqualTo("api");
        assertThat(response.getBody().getTypes().get(0).getName()).isEqualTo("API");
        assertThat(response.getBody().getTypes().get(1).getCode()).isEqualTo("firefox");
        assertThat(response.getBody().getTypes().get(1).getName()).isEqualTo("Desktop");
    }

    @Test
    void testGetDistinctPlatforms() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "platforms");

        // THEN
        assertThat(response.getBody().getPlatforms()).containsExactly("euin", "euin2");
    }

    @Test
    void testGetDistinctFeatureNames() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "featureNames");

        // THEN
        assertThat(response.getBody().getFeatureNames()).containsExactly("Feature A", "Feature B", "Feature C");
    }

    @Test
    void testGetDistinctFeatureFiles() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "featureFiles");

        // THEN
        assertThat(response.getBody().getFeatureFiles()).containsExactly("a.feature", "b.feature", "c.feature");
    }

    @Test
    void testGetDistinctScenarioNames() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "scenarioNames");

        // THEN
        assertThat(response.getBody().getScenarioNames()).containsExactly(
                "Functionalities 42, 666: Scenario e", "Scenario a", "Scenario b", "Scenario c", "Scenario d");
    }

    @Test
    void testGetDistinctSteps() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "steps");

        // THEN
        assertThat(response.getBody().getSteps()).containsExactly(
                "Step 1", "Step 2", "Step 3", "Step 4", "Step 5", "Step 6", "Step 7", "Step 8");
    }

    @Test
    void testGetDistinctStepDefinitions() {
        // WHEN
        ResponseEntity<DistinctStatisticsDTO> response = cut.getDistinct(PROJECT_CODE, "stepDefinitions");

        // THEN
        assertThat(response.getBody().getStepDefinitions()).containsExactly(
                "^Step 1$", "^Step 2$", "^Step 3$", "^Step 4$", "^Step 5$", "^Step 6$", "^Step 7$", "^Step 8$");
    }

    /**
     * Calls errorResource.getMatchingErrors(..) with the given pattern, and assert the total number of element, returning the first one if
     * found, or null if not found.
     *
     * @param pattern               the pattern to use to search for matching errors
     * @param expectedTotalElements the total number of elements to assert
     */
    private void assertGetMatchingErrors(ProblemPatternDTO pattern, int expectedTotalElements) {
        // GIVEN
        int pageIndex = 0;
        int pageSize = 1;

        // WHEN
        ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionAndProblemsDTO>> response =
                cut.getMatchingErrors(PROJECT_CODE, pattern, PageRequest.of(pageIndex, pageSize));

        // THEN
        assertThat(response.getBody().getTotalElements()).isEqualTo(expectedTotalElements);
    }

}
