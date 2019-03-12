package com.decathlon.ara.report.service;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.report.asset.AssetService;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.report.util.CucumberReportUtil;
import com.decathlon.ara.report.util.StepDefinitionUtil;
import com.decathlon.ara.util.TestUtil;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.decathlon.ara.util.TestUtil.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutedScenarioExtractorServiceTest {

    private static final String STEP_DEF_ERROR = "^A step number (\\d+) that fails with error \"([^\"]*)\"$";

    private static final String FEATURE_1_FILE = "ara/test/features/feature1.feature";
    private static final String FEATURE_1_NAME = "Feature 1";

    @Mock
    private AssetService assetService;

    @InjectMocks
    private ExecutedScenarioExtractorService cut;

    @Test
    public void testErrorsGeneration() throws IOException {
        when(assetService.saveScreenshot(any(), anyString())).thenAnswer(call ->
                "http://fake.screenshot.server/" + call.getArguments()[1] + ".png"
        );

        List<Feature> features = CucumberReportUtil.parseReportJson(TestUtil.loadUtf8ResourceAsString("reports/tests/report.json"));
        List<String> stepDefinitions = StepDefinitionUtil.parseStepDefinitionsJson(TestUtil.loadUtf8ResourceAsString("reports/tests/stepDefinitions.json"));
        List<ExecutedScenario> executedScenarios = cut.extractExecutedScenarios(features, stepDefinitions, "http://job-url/");

        // Uncomment it for debug purpose
        // System.out.println(new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(errors));

        try (AutoCloseableSoftAssertions asserts = new AutoCloseableSoftAssertions()) {
            asserts.assertThat(executedScenarios).hasSize(18);
            asserts.assertThat(executedScenarios.stream().flatMap(e -> e.getErrors().stream())).hasSize(13);

            {
                final ExecutedScenario executedScenario = executedScenarios.get(0);
                asserts.assertThat(executedScenario.getRun()).isNull();
                asserts.assertThat(executedScenario.getFeatureFile()).isEqualTo(FEATURE_1_FILE);
                asserts.assertThat(executedScenario.getFeatureName()).isEqualTo(FEATURE_1_NAME);
                asserts.assertThat(executedScenario.getSeverity()).isEqualTo("sanity-check");
                asserts.assertThat(executedScenario.getName()).isEqualTo("Functionality 111: Fail with two errors");
                asserts.assertThat(executedScenario.getCucumberId()).isEqualTo("feature-1;fail-with-two-errors");
                asserts.assertThat(executedScenario.getLine()).isEqualTo(7);
                asserts.assertThat(executedScenario.getContent()).isEqualTo("" +
                        "8:passed:123000000:Given A step that works\n" +
                        "9:failed:123000000:Then A step number 1 that fails with error \"string parameter 1\"\n" +
                        "10:failed:123000000:And A step number 2 that fails with error \"string parameter 2\"\n" +
                        "100000:passed:123000000:@After TestGlue.after(Scenario)");
                asserts.assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://fake.screenshot.server/Functionality 111: Fail with two errors.png");
                asserts.assertThat(executedScenario.getVideoUrl()).isEqualTo("http://fake.video.server/feature-1;functionality-111:-fail-with-two-errors.mp4");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("A step number 1 that fails with error \"string parameter 1\"");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo(STEP_DEF_ERROR);
                    asserts.assertThat(error.getStepLine()).isEqualTo(9);
                    asserts.assertThat(error.getException()).isEqualTo("" +
                            "java.lang.RuntimeException: Error message string parameter 1\n" +
                            "\tat ara.test.TestGlue.a_step_number_that_fails_with_error(TestGlue.java:59)\n" +
                            "\tat ✽.Then A step number 1 that fails with error \"string parameter 1\"(ara/test/features/feature1.feature:9)\n");
                }
                {
                    Error error = get(executedScenario.getErrors(), 1);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("A step number 2 that fails with error \"string parameter 2\"");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo(STEP_DEF_ERROR);
                    asserts.assertThat(error.getStepLine()).isEqualTo(10);
                    asserts.assertThat(error.getException()).isEqualTo("" +
                            "java.lang.RuntimeException: Error message string parameter 2\n" +
                            "\tat ara.test.TestGlue.a_step_number_that_fails_with_error(TestGlue.java:59)\n" +
                            "\tat ✽.And A step number 2 that fails with error \"string parameter 2\"(ara/test/features/feature1.feature:10)\n");
                }
            }

            {
                final ExecutedScenario executedScenario = executedScenarios.get(1);
                asserts.assertThat(executedScenario.getErrors()).isEmpty();
            }

            {
                final ExecutedScenario executedScenario = executedScenarios.get(2);
                asserts.assertThat(executedScenario.getRun()).isNull();
                asserts.assertThat(executedScenario.getFeatureFile()).isEqualTo(FEATURE_1_FILE);
                asserts.assertThat(executedScenario.getFeatureName()).isEqualTo(FEATURE_1_NAME);
                asserts.assertThat(executedScenario.getSeverity()).isEqualTo("high");
                asserts.assertThat(executedScenario.getName()).isEqualTo("Functionality 113: Fail with name example 1");
                asserts.assertThat(executedScenario.getLine()).isEqualTo(22);
                asserts.assertThat(executedScenario.getContent()).isEqualTo("" +
                        "18:failed:123000000:When A step number 3 that fails with error \"example 1\"\n" +
                        "100000:passed:123000000:@After TestGlue.after(Scenario)");
                asserts.assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://fake.screenshot.server/Functionality 113: Fail with name example 1.png");
                asserts.assertThat(executedScenario.getVideoUrl()).isEqualTo("http://fake.video.server/feature-1;functionality-113:-fail-with-name-<name>;;2.mp4");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("A step number 3 that fails with error \"example 1\"");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo(STEP_DEF_ERROR);
                    asserts.assertThat(error.getStepLine()).isEqualTo(18);
                    asserts.assertThat(error.getException()).isEqualTo("" +
                            "java.lang.RuntimeException: Error message example 1\n" +
                            "\tat ara.test.TestGlue.a_step_number_that_fails_with_error(TestGlue.java:59)\n" +
                            "\tat ✽.When A step number 3 that fails with error \"example 1\"(ara/test/features/feature1.feature:18)\n");
                }
            }

            {
                final ExecutedScenario executedScenario = executedScenarios.get(3);
                asserts.assertThat(executedScenario.getRun()).isNull();
                asserts.assertThat(executedScenario.getFeatureFile()).isEqualTo(FEATURE_1_FILE);
                asserts.assertThat(executedScenario.getFeatureName()).isEqualTo(FEATURE_1_NAME);
                asserts.assertThat(executedScenario.getSeverity()).isEqualTo("high");
                asserts.assertThat(executedScenario.getName()).isEqualTo("Functionality 113: Fail with name example 2");
                asserts.assertThat(executedScenario.getLine()).isEqualTo(23);
                asserts.assertThat(executedScenario.getContent()).isEqualTo("" +
                        "18:failed:123000000:When A step number 3 that fails with error \"example 2\"\n" +
                        "100000:passed:123000000:@After TestGlue.after(Scenario)");
                asserts.assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://fake.screenshot.server/Functionality 113: Fail with name example 2.png");
                asserts.assertThat(executedScenario.getVideoUrl()).isEqualTo("http://fake.video.server/feature-1;functionality-113:-fail-with-name-<name>;;3.mp4");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("A step number 3 that fails with error \"example 2\"");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo(STEP_DEF_ERROR);
                    asserts.assertThat(error.getStepLine()).isEqualTo(18);
                    asserts.assertThat(error.getException()).isEqualTo("" +
                            "java.lang.RuntimeException: Error message example 2\n" +
                            "\tat ara.test.TestGlue.a_step_number_that_fails_with_error(TestGlue.java:59)\n" +
                            "\tat ✽.When A step number 3 that fails with error \"example 2\"(ara/test/features/feature1.feature:18)\n");
                }
            }

            {
                final ExecutedScenario executedScenario = executedScenarios.get(4);
                asserts.assertThat(executedScenario.getRun()).isNull();
                asserts.assertThat(executedScenario.getFeatureFile()).isEqualTo("ara/test/features/feature2.feature");
                asserts.assertThat(executedScenario.getFeatureName()).isEqualTo("Feature 2");
                asserts.assertThat(executedScenario.getSeverity()).isEqualTo("high");
                asserts.assertThat(executedScenario.getName()).isEqualTo("Fail before it");
                asserts.assertThat(executedScenario.getLine()).isEqualTo(8);
                // This scenario has two @Before hooks, but their execution order is not guaranteed
                String begin1 = "" +
                        "-100000:failed:123000000:@Before TestGlue.failOnBefore()\n" +
                        "-99999:passed:123000000:@Before TestGlue.anotherBeforeHook()";
                String begin2 = "" +
                        "-100000:passed:123000000:@Before TestGlue.anotherBeforeHook()\n" +
                        "-99999:failed:123000000:@Before TestGlue.failOnBefore()";
                String end = "\n" +
                        "9:skipped:0:Given A step that works\n" +
                        "100000:passed:123000000:@After TestGlue.after(Scenario)";
                asserts.assertThat(executedScenario.getContent()).isIn(begin1 + end, begin2 + end);
                asserts.assertThat(executedScenario.getScreenshotUrl()).isEqualTo("http://fake.screenshot.server/Fail before it.png");
                asserts.assertThat(executedScenario.getVideoUrl()).isEqualTo("http://fake.video.server/feature-2;fail-before-it.mp4");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("@Before");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo("TestGlue.failOnBefore()");
                    asserts.assertThat(error.getStepLine()).isIn(-100000, -99999);
                    asserts.assertThat(error.getException()).startsWith("" + // startsWith because it contains Cucumber & Maven stack-traces!
                            "java.lang.RuntimeException: This scenario fails on before\n" +
                            "\tat ara.test.TestGlue.failOnBefore(TestGlue.java:35)\n");
                }
            }

            {
                final ExecutedScenario executedScenario = executedScenarios.get(5);
                asserts.assertThat(executedScenario.getRun()).isNull();
                asserts.assertThat(executedScenario.getFeatureFile()).isEqualTo("ara/test/features/feature2.feature");
                asserts.assertThat(executedScenario.getFeatureName()).isEqualTo("Feature 2");
                asserts.assertThat(executedScenario.getFeatureTags()).isEqualTo("@feature-2-tag");
                asserts.assertThat(executedScenario.getTags()).isEqualTo("@add-structured-embeddings @severity-medium");
                asserts.assertThat(executedScenario.getSeverity()).isEqualTo("medium");
                asserts.assertThat(executedScenario.getName()).isEqualTo("Table step");
                asserts.assertThat(executedScenario.getLine()).isEqualTo(12);
                // This scenario has two @After hooks, but their execution order is not guaranteed
                String begin = "" +
                        "13:passed:123000000:Given These values are true:\n" +
                        "14:passed:| Value 1       |  1 |\n" +
                        "15:passed:| Another value | 42 |\n" +
                        "16:failed:123000000:And A step number 4 that fails with error \"string parameter 4\"";
                String end1 = "\n" +
                        "100000:passed:123000000:@After TestGlue.after(Scenario)\n" +
                        "100001:passed:123000000:@After TestGlue.addStructuredEmbeddings(Scenario)";
                String end2 = "\n" +
                        "100000:passed:123000000:@After TestGlue.addStructuredEmbeddings(Scenario)\n" +
                        "100001:passed:123000000:@After TestGlue.after(Scenario)";
                asserts.assertThat(executedScenario.getContent()).isIn(begin + end1, begin + end2);

                // These data are from structured embeddings added to this scenario only (and overriding regular embeddings)
                asserts.assertThat(executedScenario.getScreenshotUrl()).isEqualTo("screenshot.com");
                asserts.assertThat(executedScenario.getVideoUrl()).isEqualTo("video.com");
                asserts.assertThat(executedScenario.getLogsUrl()).isEqualTo("logs.com");
                asserts.assertThat(executedScenario.getHttpRequestsUrl()).isEqualTo("http-requests.com");
                asserts.assertThat(executedScenario.getJavaScriptErrorsUrl()).isEqualTo("java-script-errors.com");
                asserts.assertThat(executedScenario.getStartDateTime()).isEqualTo(theDate());

                // TODO We do not test getDiffReportUrl() neither getApiServer(): come up with a more generic way to display all embeddings in GUI

                asserts.assertThat(executedScenario.getCucumberReportUrl()).isEqualTo("http://job-url/cucumber-html-reports/report-feature_ara-test-features-feature2-feature.html");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getId()).isNull();
                    asserts.assertThat(error.getStep()).isEqualTo("A step number 4 that fails with error \"string parameter 4\"");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo(STEP_DEF_ERROR);
                    asserts.assertThat(error.getStepLine()).isEqualTo(16);
                    asserts.assertThat(error.getException()).isEqualTo("" +
                            "java.lang.RuntimeException: Error message string parameter 4\n" +
                            "\tat ara.test.TestGlue.a_step_number_that_fails_with_error(TestGlue.java:59)\n" +
                            "\tat ✽.And A step number 4 that fails with error \"string parameter 4\"(ara/test/features/feature2.feature:16)\n");
                }
            }

            // Errors are naturally-identified and sorted by, in this order: runId, featureFile, scenarioName, scenarioLine, stepLine
            // We made sure @Before errors have stepLine starting with -100000
            // Now, test @After errors have stepLine starting with 100000
            {
                final ExecutedScenario executedScenario = executedScenarios.get(6);
                asserts.assertThat(executedScenario.getName()).isEqualTo("Fail after it");
                {
                    Error error = get(executedScenario.getErrors(), 0);
                    asserts.assertThat(error.getStep()).isEqualTo("@After");
                    asserts.assertThat(error.getStepDefinition()).isEqualTo("TestGlue.failOnAfter()");
                    asserts.assertThat(error.getStepLine()).isIn(100000, 100001); // Hooks execution order is not guaranteed
                }
            }

            // In feature3.feature, we have two scenarios with duplicate name, steps and errors + one scenario outline with the exact same characteristics
            // We must keep them distinct
            // Thankfully, the only thing that distinguish them is the scenario line:
            // * the two first scenarios are on different lines, of course (they have both the same generated id),
            // * and the two scenario outline executions have the line of their example table-row!
            // (the outlines also have their id endings with ";;2", ";;3"... But it's not specified: implementation of the ID scheme can change)
            asserts.assertThat(executedScenarios.get(7).getLine()).isEqualTo(6);
            asserts.assertThat(executedScenarios.get(8).getLine()).isEqualTo(11);
            asserts.assertThat(executedScenarios.get(9).getLine()).isEqualTo(21);
            asserts.assertThat(executedScenarios.get(10).getLine()).isEqualTo(22);

            asserts.assertThat(executedScenarios.get(15).getContent()).isEqualTo("" +
                    "-100000:passed:123000000:@Before TestGlue.anotherBeforeHook()\n" +
                    "0:element:Background:\n" +
                    "6:passed:123000000:Given A step that works\n" +
                    "7:failed:123000000:And A step number 1 that fails with error \"bad-background\"\n" +
                    "0:element:Scenario:\n" +
                    "11:passed:123000000:Given A step that works\n" +
                    "100000:passed:123000000:@After TestGlue.after(Scenario)");
        }
    }

    private Date theDate() {
        return new Calendar.Builder()
                .setDate(2017, Calendar.DECEMBER, 31)
                .setTimeOfDay(23, 59, 59, 999)
                .build()
                .getTime();
    }

}
