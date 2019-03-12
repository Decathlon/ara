package ara;

import ara.demo.HooksGlue;
import com.decathlon.ara.lib.StepDefinitionExtractor;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

/**
 * Execute .feature files to produce a report.json (execution result of non-@ignore scenarios),
 * a stepDefinitions.json (list of all step definitions in the provided glue) and
 * a dry-report.json (to report all scenarios, including the ignored ones).
 */
public final class ReportGenerator {

    public static void main(String[] args) throws IOException {
        String outputDirectory = args[0];

        createTestData(outputDirectory);
        createDemoData(outputDirectory);
    }

    private static void createTestData(String outputDirectory) throws IOException {
        String reportDirectory = outputDirectory + "/reports/tests";
        String gluePackage = "ara.test";

        File reportFile = generateReportJson(reportDirectory, gluePackage, "report.json");
        fixDurations(reportFile);
        generateDryReportJson(reportDirectory, gluePackage);
        StepDefinitionExtractor.extract(reportDirectory, gluePackage);
    }

    private static void createDemoData(String outputDirectory) throws IOException {
        String reportDirectory = outputDirectory + "/reports/demo";
        String gluePackage = "ara.demo";

        for (int i = 0; i < 3; i++) {
            HooksGlue.failingLevel = i;
            generateReportJson(reportDirectory, gluePackage, "report-" + i + ".json");
        }
        generateDryReportJson(reportDirectory, gluePackage);
        StepDefinitionExtractor.extract(reportDirectory, gluePackage);
    }

    /**
     * Run Cucumber features and generate report.json with all non-@ignore scenarios.
     *
     * @param reportDirectory the directory in which to create the {@code reportFileName} file
     *                        (eg. {@code System.getProperty("user.dir")})
     * @param gluePackage     the package in which to scan for Cucumber step-definition annotations ({@code @Given},
     *                        {@code @When}, {@code @Then}, and many more, depending on the Cucumber language)
     *                        (eg. "com.company.project.tests.cucumber.glue")
     * @param reportFileName  the name of the report file to create in {@code reportDirectory} (eg. "report.json")
     * @return the full path of the created .json report file
     * @throws IOException if Cucumber fails to load anything
     */
    private static File generateReportJson(String reportDirectory, String gluePackage, String reportFileName)
            throws IOException {
        final String reportFile = reportDirectory + "/" + reportFileName;
        runCucumber(
                "--strict",
                "--glue", gluePackage,
                "--plugin", "json:" + reportFile,
                "--tags", "~@ignore",
                toFeaturesPath(gluePackage));
        return new File(reportFile);
    }

    /**
     * We will use durations in tests: make sure they do not vary from execution to execution, by fixing them to an
     * arbitrary and known value.
     *
     * @param reportFile the file to replace with fix durations
     * @throws IOException in case of an I/O error
     */
    private static void fixDurations(File reportFile) throws IOException {
        String reportJson = FileUtils.readFileToString(reportFile, StandardCharsets.UTF_8);
        reportJson = reportJson.replaceAll("\"duration\": [0-9]+,", "\"duration\": 123000000,"); // 123 ms
        FileUtils.write(reportFile, reportJson, StandardCharsets.UTF_8);
    }

    /**
     * Dry-run Cucumber features and generate raw-report.json with all scenarios, to get a JSON with all scenarios.
     *
     * @param reportDirectory the directory in which to create the JSON {@code dry-report.json} file
     * @param gluePackage     the package in which to scan for Cucumber step-definition annotations ({@code @Given},
     *                        {@code @When}, {@code @Then}, and many more, depending on the Cucumber language)
     *                        (eg. "com.company.project.tests.cucumber.glue")
     * @throws IOException if Cucumber fails to load anything
     */
    private static void generateDryReportJson(String reportDirectory, String gluePackage)
            throws IOException {
        runCucumber(
                "--dry-run",
                "--glue", gluePackage,
                "--plugin", "json:" + reportDirectory + "/dry-report.json",
                toFeaturesPath(gluePackage));
    }

    /**
     * Return a classpath Cucumber feature path for a given Cucumber glue package.
     *
     * @param gluePackage eg. "some.sub.package"
     * @return eg. "classpath:org/some/sub/package/features"
     */
    private static String toFeaturesPath(String gluePackage) {
        return "classpath:" + gluePackage.replace('.', '/') + "/features/";
    }

    private static void runCucumber(String... argv) throws IOException {
        silentOutputs(() -> {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final RuntimeOptions runtimeOptions = new RuntimeOptions(Arrays.asList(argv));
            final ResourceLoader resourceLoader = new MultiLoader(classLoader);
            final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
            final Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
            runtime.run();
        });
    }

    private static void silentOutputs(RunnableThrowingIOException runnable)
            throws IOException {
        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("disableSilentOutputs"))) {
            runnable.run();
            return;
        }

        PrintStream realOut = System.out;
        PrintStream realErr = System.err;
        try {
            // Cucumber will print all exceptions in the console
            // The exceptions are expected: do not pollute the build output with them
            final PrintStream nullPrintStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    // No operation
                }
            });
            System.setOut(nullPrintStream);
            System.setErr(nullPrintStream);

            runnable.run();
        } finally {
            System.setOut(realOut);
            System.setErr(realErr);
        }
    }

    @FunctionalInterface
    private interface RunnableThrowingIOException {

        void run() throws IOException;

    }

}
