package com.decathlon.ara.scenario.common.upload;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioUploaderTest {

    @Mock
    private ScenarioRepository scenarioRepository;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private ScenarioUploader cut;

    @Test
    void assignWrongSeverityCode_ShouldComputeAWrongSeverityCode_WhenCalled() {
        //GIVEN
        final List<String> severityCodes = Arrays.asList("existing");
        final List<Scenario> scenarios = Arrays.asList(scenario(null, "existing"), scenario(null, "nonexitent"));

        //WHEN
        cut.assignWrongSeverityCode(severityCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongSeverityCode()).isNull();
        assertThat(scenarios.get(1).getWrongSeverityCode()).isEqualTo("nonexitent");
        assertThat(scenarios).hasSize(2);

    }

    @Test
    void assignWrongCountryCodes_ShouldSetWrongCountryCodeToNull_WhenAllCountriesExist() {
        //GIVEN
        List<String> countryCodes = Arrays.asList("existing");
        final List<Scenario> scenarios = Arrays.asList(scenario(null, "existing"));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongCountryCodes()).isNull();

    }

    @Test
    void assignWrongCountryCodes_ShouldComputeWrongCountryCode_WhenCountriesDoNotExist() {
        //GIVEN
        List<String> countryCodes = Arrays.asList("be", "cn", "de", "hk", "nl");
        final List<Scenario> scenarios = Arrays.asList(scenario("nl,XX,YY", null));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongCountryCodes()).isEqualTo("XX,YY");

    }

    @Test
    void assignWrongCountryCodes_ShouldAssignWrongCountryCodesOnlyToWrongOnes_WhenCalledWithSeveralScenarios() {
        //GIVEN
        List<String> countryCodes = Arrays.asList("be", "cn", "de", "hk", "nl");
        final List<Scenario> scenarios = Arrays.asList(scenario("nl", null), scenario("nonexitent", null), scenario("de,AA,ZZ", null));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios).hasSize(3);
        assertThat(scenarios.get(2).getWrongCountryCodes()).isEqualTo("AA,ZZ");

        assertThat(scenarios.get(1).getWrongCountryCodes()).isEqualTo("nonexitent");

        assertThat(scenarios.get(0).getWrongCountryCodes()).isNull();
    }

    private Scenario scenario(String countryCodes, String severity) {
        Scenario scenario = new Scenario();
        scenario.setCountryCodes(countryCodes);
        scenario.setSeverity(severity);
        return scenario;
    }

    @Test
    void getCoverageAggregatesFromFunctionality_returnCoverageAggregates() {
        // Given
        Functionality functionality = mock(Functionality.class);
        Scenario scenario1 = mock(Scenario.class);
        Scenario scenario2 = mock(Scenario.class);
        Scenario scenario3 = mock(Scenario.class);
        Scenario scenario4 = mock(Scenario.class);
        Scenario scenario5 = mock(Scenario.class);
        var scenarios = new HashSet<>(List.of(scenario1, scenario2, scenario3, scenario4, scenario5));

        Source source1 = mock(Source.class);
        Source source2 = mock(Source.class);

        // When
        when(functionality.getScenarios()).thenReturn(scenarios);

        when(scenario1.isIgnored()).thenReturn(false);
        when(scenario1.getSource()).thenReturn(source1);
        when(scenario1.getCountryCodes()).thenReturn("fr,us");
        when(scenario2.isIgnored()).thenReturn(true);
        when(scenario2.getSource()).thenReturn(source2);
        when(scenario2.getCountryCodes()).thenReturn("fr,xx");
        when(scenario3.isIgnored()).thenReturn(false);
        when(scenario3.getSource()).thenReturn(source2);
        when(scenario3.getCountryCodes()).thenReturn("fr,us");
        when(scenario4.isIgnored()).thenReturn(false);
        when(scenario4.getSource()).thenReturn(source2);
        when(scenario4.getCountryCodes()).thenReturn("us,all");
        when(scenario5.isIgnored()).thenReturn(true);
        when(scenario5.getSource()).thenReturn(source1);
        when(scenario5.getCountryCodes()).thenReturn(null);

        when(source1.getCode()).thenReturn("source_1");
        when(source2.getCode()).thenReturn("source_2");

        // Then
        var coverageAggregates = cut.getCoverageAggregatesFromFunctionality(functionality);
        var coveredScenariosAggregate = coverageAggregates.get(false);
        assertThat(coveredScenariosAggregate).isEqualTo("source_2:*=2,all=1,fr=1,us=2|source_1:*=1,fr=1,us=1");
        var ignoredScenariosAggregate = coverageAggregates.get(true);
        assertThat(ignoredScenariosAggregate).isEqualTo("source_2:*=1,fr=1,xx=1|source_1:*=1");
    }

    @Test
    void getCoverageNumbersFromFunctionality_returnCoverageNumbers() {
        // Given
        Functionality functionality = mock(Functionality.class);
        Scenario scenario1 = mock(Scenario.class);
        Scenario scenario2 = mock(Scenario.class);
        Scenario scenario3 = mock(Scenario.class);
        Scenario scenario4 = mock(Scenario.class);
        Scenario scenario5 = mock(Scenario.class);
        var scenarios = new HashSet<>(List.of(scenario1, scenario2, scenario3, scenario4, scenario5));

        // When
        when(functionality.getScenarios()).thenReturn(scenarios);
        when(scenario1.isIgnored()).thenReturn(false);
        when(scenario2.isIgnored()).thenReturn(true);
        when(scenario3.isIgnored()).thenReturn(false);
        when(scenario4.isIgnored()).thenReturn(false);
        when(scenario5.isIgnored()).thenReturn(true);

        // Then
        var coverageNumbers = cut.getCoverageNumbersFromFunctionality(functionality);
        var coveredScenariosNumber = coverageNumbers.get(false);
        assertThat(coveredScenariosNumber).isEqualTo(3L);
        var ignoredScenariosNumber = coverageNumbers.get(true);
        assertThat(ignoredScenariosNumber).isEqualTo(2L);
    }
}
