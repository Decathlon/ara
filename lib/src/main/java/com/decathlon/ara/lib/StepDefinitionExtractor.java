package com.decathlon.ara.lib;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minidev.json.JSONArray;

/**
 * This class can be copy/pasted into a project wanting to report Cucumber executions into ARA, in order to generate the
 * {@code stepDefinitions.json} file needed for more relevant
 *
 * @see #extract(String, String)
 */
public final class StepDefinitionExtractor {

    /**
     * Using the Java glue in {@code gluePackage} package from this project, write a {@code stepDefinitions.json} file
     * in the {@code featuresPath} folder.
     * <p>
     * This file is a JSON array containing all step-definitions (regular expression strings) available in the current
     * project.
     *
     * @param reportDirectory the directory in which to create the {@code stepDefinitions.json} file
     *                        (eg. {@code System.getProperty("user.dir")})
     * @param gluePackage     the package in which to scan for Cucumber step-definition annotations ({@code @Given},
     *                        {@code @When}, {@code @Then}, and many more, depending on the Cucumber language)
     *                        (eg. "com.company.project.tests.cucumber.glue")
     * @throws IOException when writing the {@code stepDefinitions.json} file into {@code reportDirectory} failed
     */
    public static void extract(String reportDirectory, final String gluePackage)
            throws IOException {
        // Create the Cucumber runtime
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final RuntimeOptions runtimeOptions = new RuntimeOptions(Arrays.asList("--glue", gluePackage));
        final ResourceLoader resourceLoader = new MultiLoader(classLoader);
        final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        final Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);

        // Ask Cucumber to extract all step definitions
        final List<String> stepDefinitions = new ArrayList<>();
        final StepDefinitionReporter reporter = stepDefinition -> stepDefinitions.add(stepDefinition.getPattern());
        runtime.getGlue().reportStepDefinitions(reporter);

        // Create the JSON array of all step definitions
        final JSONArray array = new JSONArray();
        array.addAll(stepDefinitions);
        final String json = array.toJSONString();

        // Write stepDefinitions.json into the requested directory
        Files.createDirectories(Paths.get(reportDirectory));
        final File file = new File(reportDirectory, "stepDefinitions.json");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

}
