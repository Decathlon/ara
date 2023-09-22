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

package com.decathlon.ara.scenario.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.scenario.postman.service.PostmanScenarioIndexerService;
import com.decathlon.ara.service.SeverityService;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSourceDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.mapper.GenericMapper;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

    private static final long PROJECT_ID = 1;

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private GenericMapper mapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SeverityService severityService;

    @Mock
    private PostmanScenarioIndexerService postmanScenarioIndexerService;

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private ScenarioService cut;

    @Test
    void findAllWithFunctionalityErrors_ShouldReturnMappedDataFromScenarioRepositoryFindAllWithFunctionalityErrors_WhenCalled() {
        // GIVEN
        List<ScenarioSummary> functionalities = Collections.emptyList();
        when(scenarioRepository.findAllWithFunctionalityErrors(PROJECT_ID)).thenReturn(functionalities);
        List<ScenarioSummaryDTO> functionalitiesDTOs = Collections.emptyList();
        when(mapper.mapCollection(same(functionalities), Mockito.eq(ScenarioSummaryDTO.class))).thenReturn(functionalitiesDTOs);

        // WHEN
        final List<ScenarioSummaryDTO> result = cut.findAllWithFunctionalityErrors(PROJECT_ID);

        // THEN
        assertThat(result).isSameAs(functionalitiesDTOs);
    }

    @Test
    void getIgnoredScenarioCounts_ShouldComputeAndAggregateIgnoredScenarioCounts_WhenCalled() {
        // GIVEN
        final List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO("high", Integer.valueOf(1), null, null, null, false),
                new SeverityDTO("medium", Integer.valueOf(2), null, null, null, true),
                new SeverityDTO("*", Integer.valueOf(3), null, null, null, false));
        when(severityService.getSeveritiesWithAll(PROJECT_ID)).thenReturn(severities);
        when(severityService.getDefaultSeverityCode(severities)).thenReturn("medium");
        final Source sourceA = new Source();
        sourceA.setCode("A");
        final Source sourceB = new Source();
        sourceB.setCode("B");
        when(scenarioRepository.findIgnoreCounts(PROJECT_ID)).thenReturn(Arrays.asList(
                scenarioIgnoreCount(sourceA, "high", true, 2),
                scenarioIgnoreCount(sourceA, "medium", true, 1),
                scenarioIgnoreCount(sourceA, "medium", false, 1),
                scenarioIgnoreCount(sourceB, "", false, 1)));
        when(scenarioRepository.findIgnoredScenarios(PROJECT_ID)).thenReturn(Arrays.asList(
                ignoredScenario(sourceA, "f1", "F 1", "high", "Name 1"),
                ignoredScenario(sourceA, "f1", "F 1", "high", "Name 2"),
                ignoredScenario(sourceA, "f2", "F 2", "medium", "Name 3"),
                ignoredScenario(sourceB, "f1", "F 1", "", "Name 4")));
        final SourceDTO sourceDTOA = new SourceDTO();
        sourceDTOA.setCode("A");
        final SourceDTO sourceDTOB = new SourceDTO();
        sourceDTOB.setCode("B");
        when(mapper.map(same(sourceA), Mockito.eq(SourceDTO.class))).thenReturn(sourceDTOA);
        when(mapper.map(same(sourceB), Mockito.eq(SourceDTO.class))).thenReturn(sourceDTOB);

        // WHEN
        final List<ScenarioIgnoreSourceDTO> result = cut.getIgnoredScenarioCounts(PROJECT_ID);

        // THEN
        assertThat(result).hasSize(3);

        // Sorted by source code, '*' first
        assertThat(result.get(0).getSource().getCode()).isEqualTo("*");
        assertThat(result.get(0).getSeverities()).hasSize(3);

        assertThat(result.get(0).getSeverities().get(0).getSeverity().getCode()).isEqualTo("high");
        assertThat(result.get(0).getSeverities().get(0).getCounts().getIgnored()).isEqualTo(2);
        assertThat(result.get(0).getSeverities().get(0).getCounts().getTotal()).isEqualTo(2);
        assertThat(result.get(0).getSeverities().get(0).getFeatures()).isNull();

        assertThat(result.get(0).getSeverities().get(1).getSeverity().getCode()).isEqualTo("medium");
        assertThat(result.get(0).getSeverities().get(1).getCounts().getIgnored()).isEqualTo(1);
        assertThat(result.get(0).getSeverities().get(1).getCounts().getTotal()).isEqualTo(3);
        assertThat(result.get(0).getSeverities().get(1).getFeatures()).isNull();

        assertThat(result.get(0).getSeverities().get(2).getSeverity().getCode()).isEqualTo("*");
        assertThat(result.get(0).getSeverities().get(2).getCounts().getIgnored()).isEqualTo(3);
        assertThat(result.get(0).getSeverities().get(2).getCounts().getTotal()).isEqualTo(5);
        assertThat(result.get(0).getSeverities().get(2).getFeatures()).isNull();

        assertThat(result.get(1).getSource().getCode()).isEqualTo("A");
        assertThat(result.get(1).getSeverities()).hasSize(3);

        assertThat(result.get(1).getSeverities().get(0).getSeverity().getCode()).isEqualTo("high");
        assertThat(result.get(1).getSeverities().get(0).getCounts().getIgnored()).isEqualTo(2);
        assertThat(result.get(1).getSeverities().get(0).getCounts().getTotal()).isEqualTo(2);
        assertThat(result.get(1).getSeverities().get(0).getFeatures()).hasSize(1);
        assertThat(result.get(1).getSeverities().get(0).getFeatures().get(0).getName()).isEqualTo("F 1");
        assertThat(result.get(1).getSeverities().get(0).getFeatures().get(0).getFile()).isEqualTo("f1");
        assertThat(result.get(1).getSeverities().get(0).getFeatures().get(0).getScenarios()).hasSize(2);
        assertThat(result.get(1).getSeverities().get(0).getFeatures().get(0).getScenarios().get(0)).isEqualTo("Name 1");
        assertThat(result.get(1).getSeverities().get(0).getFeatures().get(0).getScenarios().get(1)).isEqualTo("Name 2");

        assertThat(result.get(1).getSeverities().get(1).getSeverity().getCode()).isEqualTo("medium");
        assertThat(result.get(1).getSeverities().get(1).getCounts().getIgnored()).isEqualTo(1);
        assertThat(result.get(1).getSeverities().get(1).getCounts().getTotal()).isEqualTo(2);
        assertThat(result.get(1).getSeverities().get(1).getFeatures()).hasSize(1);
        assertThat(result.get(1).getSeverities().get(1).getFeatures().get(0).getName()).isEqualTo("F 2");
        assertThat(result.get(1).getSeverities().get(1).getFeatures().get(0).getFile()).isEqualTo("f2");
        assertThat(result.get(1).getSeverities().get(1).getFeatures().get(0).getScenarios()).hasSize(1);
        assertThat(result.get(1).getSeverities().get(1).getFeatures().get(0).getScenarios().get(0)).isEqualTo("Name 3");

        assertThat(result.get(1).getSeverities().get(2).getSeverity().getCode()).isEqualTo("*");
        assertThat(result.get(1).getSeverities().get(2).getCounts().getIgnored()).isEqualTo(3);
        assertThat(result.get(1).getSeverities().get(2).getCounts().getTotal()).isEqualTo(4);
        assertThat(result.get(1).getSeverities().get(2).getFeatures()).isNull();

        assertThat(result.get(2).getSource().getCode()).isEqualTo("B");
        assertThat(result.get(2).getSeverities()).hasSize(2);
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

}
