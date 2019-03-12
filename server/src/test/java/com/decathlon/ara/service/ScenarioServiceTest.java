package com.decathlon.ara.service;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import com.decathlon.ara.postman.service.PostmanScenarioIndexerService;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSourceDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.mapper.ScenarioSummaryMapper;
import com.decathlon.ara.service.mapper.SourceMapper;
import com.decathlon.ara.repository.SeverityRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScenarioServiceTest {

    private static final long PROJECT_ID = 1;

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private SourceMapper sourceMapper;

    @Mock
    private ScenarioSummaryMapper scenarioSummaryMapper;

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
    public void findAllWithFunctionalityErrors_ShouldReturnMappedDataFromScenarioRepositoryFindAllWithFunctionalityErrors_WhenCalled() {
        // GIVEN
        List<ScenarioSummary> functionalities = Collections.emptyList();
        when(scenarioRepository.findAllWithFunctionalityErrors(PROJECT_ID)).thenReturn(functionalities);
        List<ScenarioSummaryDTO> functionalitiesDTOs = Collections.emptyList();
        when(scenarioSummaryMapper.toDto(same(functionalities))).thenReturn(functionalitiesDTOs);

        // WHEN
        final List<ScenarioSummaryDTO> result = cut.findAllWithFunctionalityErrors(PROJECT_ID);

        // THEN
        assertThat(result).isSameAs(functionalitiesDTOs);
    }

    @Test
    public void getIgnoredScenarioCounts_ShouldComputeAndAggregateIgnoredScenarioCounts_WhenCalled() {
        // GIVEN
        final List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO().withCode("high").withPosition(Integer.valueOf(1)),
                new SeverityDTO().withCode("medium").withPosition(Integer.valueOf(2)).withDefaultOnMissing(true),
                new SeverityDTO().withCode("*").withPosition(Integer.valueOf(3)));
        when(severityService.getSeveritiesWithAll(PROJECT_ID)).thenReturn(severities);
        when(severityService.getDefaultSeverityCode(severities)).thenReturn("medium");
        final Source sourceA = new Source().withCode("A");
        final Source sourceB = new Source().withCode("B");
        when(scenarioRepository.findIgnoreCounts(PROJECT_ID)).thenReturn(Arrays.asList(
                new ScenarioIgnoreCount().withSource(sourceA).withSeverityCode("high").withIgnored(true).withCount(2),
                new ScenarioIgnoreCount().withSource(sourceA).withSeverityCode("medium").withIgnored(true).withCount(1),
                new ScenarioIgnoreCount().withSource(sourceA).withSeverityCode("medium").withIgnored(false).withCount(1),
                new ScenarioIgnoreCount().withSource(sourceB).withSeverityCode("").withIgnored(false).withCount(1)));
        when(scenarioRepository.findIgnoredScenarios(PROJECT_ID)).thenReturn(Arrays.asList(
                new IgnoredScenario(sourceA, "f1", "F 1", "high", "Name 1"),
                new IgnoredScenario(sourceA, "f1", "F 1", "high", "Name 2"),
                new IgnoredScenario(sourceA, "f2", "F 2", "medium", "Name 3"),
                new IgnoredScenario(sourceB, "f1", "F 1", "", "Name 4")));
        when(sourceMapper.toDto(same(sourceA))).thenReturn(new SourceDTO().withCode("A"));
        when(sourceMapper.toDto(same(sourceB))).thenReturn(new SourceDTO().withCode("B"));

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

    @Test
    public void assignWrongSeverityCode_ShouldComputeAWrongSeverityCode_WhenCalled() {

        //GIVEN
        final List<String> severityCodes = Arrays.asList("existing");
        final List<Scenario> scenarios = Arrays.asList(new Scenario()
                .withSeverity("existing"),
                new Scenario()
                        .withSeverity("nonexitent"));

        //WHEN
        cut.assignWrongSeverityCode(severityCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongSeverityCode()).isNull();
        assertThat(scenarios.get(1).getWrongSeverityCode()).isEqualTo("nonexitent");
        assertThat(scenarios).hasSize(2);

    }

    public void assignWrongCountryCodes_ShouldSetWrongCountryCodeToNull_WhenAllCountriesExist() {

        //GIVEN
        List<String> countryCodes = Arrays.asList("existing");
        final List<Scenario> scenarios = Arrays.asList(
                new Scenario()
                        .withSeverity("existing"));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongCountryCodes()).isNull();

    }

    public void assignWrongCountryCodes_ShouldComputeWrongCountryCode_WhenCountriesDoNotExist() {

        //GIVEN
        List<String> countryCodes = Arrays.asList("be", "cn", "de", "hk", "nl");
        final List<Scenario> scenarios = Arrays.asList(new Scenario()
                .withCountryCodes("nl,XX,YY"));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongCountryCodes()).isEqualTo("XX,YY");

    }

    @Test
    public void assignWrongCountryCodes_ShouldAssignWrongCountryCodesOnlyToWrongOnes_WhenCalledWithSeveralScenarios() {

        //GIVEN
        List<String> countryCodes = Arrays.asList("be", "cn", "de", "hk", "nl");
        final List<Scenario> scenarios = Arrays.asList(
                new Scenario()
                        .withCountryCodes("nl"),
                new Scenario()
                        .withCountryCodes("nonexitent"),
                new Scenario()
                        .withCountryCodes("de,AA,ZZ"));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios).hasSize(3);
        assertThat(scenarios.get(2).getWrongCountryCodes()).isEqualTo("AA,ZZ");

        assertThat(scenarios.get(1).getWrongCountryCodes()).isEqualTo("nonexitent");

        assertThat(scenarios.get(0).getWrongCountryCodes()).isNull();

    }

}
