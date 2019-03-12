package com.decathlon.ara.report.util;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.util.TestUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class ScenarioExtractorUtilTest {

    private static void removeFunctionalitiesFromScenarioCucumberId(String cucumberId, String expected) {
        assertThat(ScenarioExtractorUtil.removeFunctionalitiesFromScenarioCucumberId(cucumberId)).isEqualTo(expected);
    }

    private static void removeFunctionalitiesFromScenarioName(String name) {
        assertThat(ScenarioExtractorUtil.removeFunctionalitiesFromScenarioName(name)).isEqualTo("Title");
    }

    private static void assertWrongFunctionalityIds(String scenarioName, List<Functionality> functionalities, String... expected) {
        assertThat(ScenarioExtractorUtil.extractWrongFunctionalityIds(scenarioName, functionalities)).containsExactly(expected);
    }

    private static void assertScenario1(Scenario scenario) {
        assertThat(scenario.getSource().getCode()).isEqualTo("B");
        assertThat(scenario.getFeatureFile()).isEqualTo("ara/test/features/bad-feature.feature");
        assertThat(scenario.getFeatureName()).isEqualTo("Ignored feature");
        assertThat(scenario.getFeatureTags()).isEqualTo("@ignore");
        assertThat(scenario.getTags()).isEqualTo("@country-all @severity-sanity-check");
        assertThat(scenario.isIgnored()).isTrue();
        assertThat(scenario.getCountryCodes()).isEqualTo("all");
        assertThat(scenario.getSeverity()).isEqualTo("sanity-check");
        assertThat(scenario.getName()).isEqualTo("Functionality 22: Scenario in ignored feature");
        assertThat(scenario.getLine()).isEqualTo(6);
        assertThat(scenario.getContent()).isEqualTo("" +
                "7:skipped:0:Given A step that works\n" +
                "8:skipped:0:When A step number 1 that fails with error \"not-run-anyway\"");
    }

    private static void assertScenario2(Scenario scenario) {
        assertThat(scenario.getSource().getCode()).isEqualTo("B");
        assertThat(scenario.getFeatureFile()).isEqualTo("ara/test/features/bad-feature.feature");
        assertThat(scenario.getFeatureName()).isEqualTo("Ignored feature");
        assertThat(scenario.getFeatureTags()).isEqualTo("@ignore");
        assertThat(scenario.getTags()).isEqualTo("@country-be @country-nl @severity-high");
        assertThat(scenario.isIgnored()).isTrue();
        assertThat(scenario.getCountryCodes()).isEqualTo("be,nl");
        assertThat(scenario.getSeverity()).isEqualTo("high");
        assertThat(scenario.getName()).isEqualTo("Functionality 112: Ignored scenario with undefined step");
        assertThat(scenario.getLine()).isEqualTo(18);
        assertThat(scenario.getContent()).isEqualTo("" +
                "19:skipped:0:Given A step that works\n" +
                "20:undefined:0:Then A step that does not exist!");
    }

    private static void assertScenarioWithoutTag(Scenario scenario) {
        // Return "" (and not ignored) when there is no tags
        assertThat(scenario.getFeatureTags()).isEmpty();
        assertThat(scenario.getTags()).isEmpty();
        assertThat(scenario.isIgnored()).isFalse();
        assertThat(scenario.getCountryCodes()).isEmpty();
        assertThat(scenario.getSeverity()).isEmpty();
    }

    private static void assertFunctionalityIds(String scenarioName, int... expectedIds) {
        assertThat(ScenarioExtractorUtil.extractFunctionalityIds(scenarioName)).containsExactly(longs(expectedIds));
    }

    @Test
    public void testExtractScenarios() throws IOException {
        List<Feature> features = CucumberReportUtil.parseReportJson(TestUtil.loadUtf8ResourceAsString("reports/tests/dry-report.json"));
        List<Scenario> scenarios = ScenarioExtractorUtil.extractScenarios(new Source().withCode("B"), features);

        assertThat(scenarios.size()).isEqualTo(19);
        assertScenario1(scenarios.get(0));
        assertScenario2(scenarios.get(1));
        assertScenarioWithoutTag(scenarios.get(12));

        // Check tags from both Feature and Scenario are used and merged

        assertThat(scenarios.get(0).isIgnored()).isTrue(); // Defined on Feature
        assertThat(scenarios.get(11).isIgnored()).isTrue(); // Defined on Scenario

        assertThat(scenarios.get(13).getCountryCodes()).isEqualTo("nl"); // Defined on Feature
        assertThat(scenarios.get(5).getCountryCodes()).isEqualTo("nl"); // Defined on Scenario
        assertThat(scenarios.get(2).getCountryCodes()).isEqualTo("nl"); // Duplicate on both Feature Scenario
        assertThat(scenarios.get(3).getCountryCodes()).isEqualTo("be,nl"); // Defined on both Feature Scenario (returned in alphabetical order)
        assertThat(scenarios.get(4).getCountryCodes()).isEqualTo("all"); // 'all' takes precedence and removes other countries

        assertThat(scenarios.get(13).getSeverity()).isEqualTo("high"); // Defined on Feature
        assertThat(scenarios.get(0).getSeverity()).isEqualTo("sanity-check"); // Defined on Scenario
        assertThat(scenarios.get(14).getSeverity()).isEqualTo("high"); // Duplicate on both Feature Scenario

        assertThat(scenarios.get(16).getContent()).isEqualTo("" +
                "0:element:Background:\n" +
                "6:skipped:0:Given A step that works\n" +
                "7:skipped:0:And A step number 1 that fails with error \"bad-background\"\n" +
                "0:element:Scenario:\n" +
                "11:skipped:0:Given A step that works");

        assertThat(scenarios.get(18).getContent()).isEqualTo("" +
                "4:skipped:0:Given A doc string:\n" +
                "4:skipped:\"\"\"\n" +
                "4:skipped:Line 1\n" +
                "4:skipped:Line 2\n" +
                "4:skipped:\"\"\"\n" +
                "9:skipped:0:When A doc string:\n" +
                "9:skipped:\"\"\"ruby\n" +
                "9:skipped:# With content type\n" +
                "9:skipped:\"\"\"");
    }

    @Test
    public void testExtractFunctionalityIds() {
        // Correct single
        assertFunctionalityIds("Functionality 42: Title", 42);

        // Single with typos
        assertFunctionalityIds("Functionality 42 : Title", 42);
        assertFunctionalityIds("Functionaliti 42: Title", 42);
        assertFunctionalityIds("Functionalitie 42: Title", 42);
        assertFunctionalityIds("Functionalities 42: Title", 42);
        assertFunctionalityIds("Functionalitys 42: Title", 42);
        assertFunctionalityIds("Functionalityes 42: Title", 42);
        assertFunctionalityIds("Functionality 42 : Title", 42);
        assertFunctionalityIds("FONCTIONNALITY 42 : Title", 42);
        assertFunctionalityIds("functionalityes 42: Title", 42);
        assertFunctionalityIds("FUNCtionalitIes 42: Title", 42);
        assertFunctionalityIds("Functionality 42  : Title", 42);
        assertFunctionalityIds("Functionality 42\t: Title", 42);
        assertFunctionalityIds("Functionality\t42\t: Title", 42);
        assertFunctionalityIds("Functionality 42 \t : Title", 42);

        // Correct twin
        assertFunctionalityIds("Functionalities 42 & 43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42 and 43: Title", 42, 43);

        // Twin with typos
        assertFunctionalityIds("Functionalities 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42, 43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42 , 43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42  ,  43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42&43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42& 43: Title", 42, 43);
        assertFunctionalityIds("Functionalities 42 &43: Title", 42, 43);
        assertFunctionalityIds("Functionality 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionaliti 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionalitie 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionalityes 42,43: Title", 42, 43);
        assertFunctionalityIds("Fonctionalityes 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionnalityes 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionallities 42,43: Title", 42, 43);
        assertFunctionalityIds("Fonctionnallitys 42,43: Title", 42, 43);
        assertFunctionalityIds("Functionalities\t42,\t43\t : Title", 42, 43);

        // More
        assertFunctionalityIds("Functionalityes 42, 43, 44, 45 & 46: Title", 42, 43, 44, 45, 46);
        assertFunctionalityIds("Functionalityes 42 & 43 & 44 & 45 & 46: Title", 42, 43, 44, 45, 46);
        assertFunctionalityIds("Functionalityes 42,43 , 44 & 45 and 46: Title", 42, 43, 44, 45, 46);
        assertFunctionalityIds("Functionalityes 42,43 and 44 & 45 and 46: Title", 42, 43, 44, 45, 46);

        // Invalid
        assertFunctionalityIds("Functionalities : Title");
        assertFunctionalityIds("Functionality STRING: Title");
        assertFunctionalityIds("Functionality 42STRING: Title");
        assertFunctionalityIds("Functionality 42, STRING: Title", 42);
        assertFunctionalityIds("Functionality STRING and 42: Title", 42);
    }

    @Test
    public void testExtractWrongFunctionalityIds() {
        List<Functionality> functionalities = Arrays.asList(
                new Functionality().withId(Long.valueOf(42)),
                new Functionality().withId(Long.valueOf(43)));

        assertWrongFunctionalityIds("Functionality 42: OK", functionalities);
        assertWrongFunctionalityIds("Functionality 42, 43: OK", functionalities);

        assertWrongFunctionalityIds("Functionality 404: Nonexistent", functionalities, "404");
        assertWrongFunctionalityIds("Functionality -42: Nonexistent", functionalities, "-42");

        assertWrongFunctionalityIds("Functionality foobar: ______Parsing error", functionalities, "foobar");
        assertWrongFunctionalityIds("Functionality foo, 43, bar: Parsing error", functionalities, "foo", "bar");
        assertWrongFunctionalityIds("Functionality 42, 43foo: ___Parsing error", functionalities, "43foo");
    }

    @Test
    public void testRemoveFunctionalitiesFromScenarioCucumberId() {
        // Remove single functionality
        removeFunctionalitiesFromScenarioCucumberId("feature-1;functionality-113:-fail-with-name-\u003cname\u003e;;2", "feature-1;fail-with-name-\u003cname\u003e;;2");

        // Remove multiple functionalities
        removeFunctionalitiesFromScenarioCucumberId("feature-1;functionalities-112-\u0026-113:-pass", "feature-1;pass");

        // Do not remove anything if no functionalities are present
        removeFunctionalitiesFromScenarioCucumberId("a-feature;a-scenario", "a-feature;a-scenario");

        // Do not remove anything if there is no scenario name after the functionality (the functionality will serve as a scenario name for the ID)
        removeFunctionalitiesFromScenarioCucumberId("feature-1;functionality-113:", "feature-1;functionality-113:");

        // Do not crash when no data are provided
        removeFunctionalitiesFromScenarioCucumberId("", "");
        removeFunctionalitiesFromScenarioCucumberId(null, null);
    }

    @Test
    public void testRemoveFunctionalitiesFromScenarioName() {
        // Correct single
        removeFunctionalitiesFromScenarioName("Functionality 42: Title");

        // Single with typos
        removeFunctionalitiesFromScenarioName("Functionality 42 : Title");
        removeFunctionalitiesFromScenarioName("Functionaliti 42: Title");
        removeFunctionalitiesFromScenarioName("Functionalitie 42: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42: Title");
        removeFunctionalitiesFromScenarioName("Functionalitys 42: Title");
        removeFunctionalitiesFromScenarioName("Functionalityes 42: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42 : Title");
        removeFunctionalitiesFromScenarioName("FONCTIONNALITY 42 : Title");
        removeFunctionalitiesFromScenarioName("functionalityes 42: Title");
        removeFunctionalitiesFromScenarioName("FUNCtionalitIes 42: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42  : Title");
        removeFunctionalitiesFromScenarioName("Functionality 42\t: Title");
        removeFunctionalitiesFromScenarioName("Functionality\t42\t: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42 \t : Title");

        // Correct twin
        removeFunctionalitiesFromScenarioName("Functionalities 42 & 43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42 and 43: Title");

        // Twin with typos
        removeFunctionalitiesFromScenarioName("Functionalities 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42, 43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42 , 43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42  ,  43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42&43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42& 43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities 42 &43: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionaliti 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionalitie 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionalityes 42,43: Title");
        removeFunctionalitiesFromScenarioName("Fonctionalityes 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionnalityes 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionallities 42,43: Title");
        removeFunctionalitiesFromScenarioName("Fonctionnallitys 42,43: Title");
        removeFunctionalitiesFromScenarioName("Functionalities\t42,\t43\t : Title");

        // More
        removeFunctionalitiesFromScenarioName("Functionalityes 42, 43, 44, 45 & 46: Title");
        removeFunctionalitiesFromScenarioName("Functionalityes 42 & 43 & 44 & 45 & 46: Title");
        removeFunctionalitiesFromScenarioName("Functionalityes 42,43 , 44 & 45 and 46: Title");
        removeFunctionalitiesFromScenarioName("Functionalityes 42,43 and 44 & 45 and 46: Title");

        // Invalid
        removeFunctionalitiesFromScenarioName("Functionalities : Title");
        removeFunctionalitiesFromScenarioName("Functionality STRING: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42STRING: Title");
        removeFunctionalitiesFromScenarioName("Functionality 42, STRING: Title");
        removeFunctionalitiesFromScenarioName("Functionality STRING and 42: Title");
    }

}
