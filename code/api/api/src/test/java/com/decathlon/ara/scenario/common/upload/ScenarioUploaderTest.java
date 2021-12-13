package com.decathlon.ara.scenario.common.upload;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.repository.SourceRepository;

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

    void assignWrongCountryCodes_ShouldSetWrongCountryCodeToNull_WhenAllCountriesExist() {

        //GIVEN
        List<String> countryCodes = Arrays.asList("existing");
        final List<Scenario> scenarios = Arrays.asList(scenario(null, "existing"));

        //WHEN
        cut.assignWrongCountryCodes(countryCodes, scenarios);

        //THEN
        assertThat(scenarios.get(0).getWrongCountryCodes()).isNull();

    }

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
}
