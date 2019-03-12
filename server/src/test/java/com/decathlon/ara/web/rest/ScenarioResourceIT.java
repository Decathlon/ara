package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.util.TestUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/functionality.xml")
public class ScenarioResourceIT {

    private static final String PROJECT_CODE = "p";

    private static final int NULL = -42;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private ScenarioResource cut;

    private List<Scenario> scenarios;
    private List<Functionality> functionalities;

    private static String[] names(String... names) {
        return names;
    }

    @Test
    public void testUploadCucumber() throws IOException {
        String reportJson = TestUtil.loadUtf8ResourceAsString("reports/tests/dry-report.json");

        cut.uploadCucumber(PROJECT_CODE, "sourceB", reportJson);

        scenarios = scenarioRepository.findAll();
        assertScenarioHasWrongFunctionalityIds();

        functionalities = functionalityRepository.findAll();
        assertFunctionality(111, 4, "sourceA:*=3,cn=3,nl=1|sourceB:*=1,nl=1", 0, null, names(
                "S1", "S4", "S5", // Other assigned API scenarios (ordered by feature file and then scenario name)
                "Functionality 111: Fail with two errors" // @country-nl*2 (result in only 1 'nl' because tags merged from feature and scenario)
        ));
        assertFunctionality(112, 2, "sourceA:*=1,cn=1|sourceB:*=1,be=1,nl=1", 1, "sourceB:*=1,be=1,nl=1", names(
                "S1", // Other assigned API scenarios (ordered by feature file and then scenario name)
                "Functionality 112: Ignored scenario with undefined step", // @country-be @country-nl @ignore
                "Functionalities 112 & 113: Pass" // @country-nl @country-be
        ));
        assertFunctionality(113, 2, "sourceB:*=2,all=1,be=1,nl=1", 0, null, names(
                "Functionalities 112 & 113: Pass", // @country-nl @country-be
                "Functionality 113: Fail with name example 1" // @country-nl @country-all ('nl' removed because 'all')
        ));
        assertFunctionality(22, 1, "sourceB:=1,*=1", 1, "sourceB:*=1,all=1", names(
                "Functionality 22: Scenario in ignored feature", // @country-all @ignore
                "Functionality 22: Scenario without tags"
        ));
        assertFunctionality(31, 0, null, 1, "sourceB:*=1,be=1,nl=1", names(
                "Functionality 31: Ignored scenario" // @ignore
        ));
        assertFunctionality(1, NULL, null, NULL, null, names()); // Cannot cover a folder
    }

    private void assertScenarioHasWrongFunctionalityIds() {
        Scenario scenario = scenarios.stream()
                .filter(s -> "Functionality not, an & id: Scenario with tags (merging tags with feature ones)".equals(s.getName()))
                .findFirst().orElseThrow(RuntimeException::new);

        assertThat(scenario.getWrongFunctionalityIds()).isEqualTo("not\nan\nid");
    }

    private void assertFunctionality(int id, int coveredCount, String covered, int ignoredCount, String ignored, String[] scenarioNames) {
        Functionality functionality = functionalities.stream()
                .filter(f -> Long.valueOf(id).equals(f.getId()))
                .findFirst().orElseThrow(RuntimeException::new);

        assertThat(functionality.getCoveredScenarios()).isEqualTo(coveredCount == NULL ? null : Integer.valueOf(coveredCount));
        assertThat(functionality.getCoveredCountryScenarios()).isEqualTo(covered);
        assertThat(functionality.getIgnoredScenarios()).isEqualTo(ignoredCount == NULL ? null : Integer.valueOf(ignoredCount));
        assertThat(functionality.getIgnoredCountryScenarios()).isEqualTo(ignored);
        assertThat(functionality.getScenarios().stream().map(Scenario::getName)).containsExactly(scenarioNames);
    }

}
