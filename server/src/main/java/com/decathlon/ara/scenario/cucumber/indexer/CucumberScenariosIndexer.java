package com.decathlon.ara.scenario.cucumber.indexer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cucumber.service.ExecutedScenarioExtractorService;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CucumberScenariosIndexer implements ScenariosIndexer {

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final ExecutedScenarioExtractorService executedScenarioExtractorService;

    /**
     * Get the Cucumber executed scenarios
     * @param cucumberFolder the Cucumber report folder
     * @param run the run
     * @return the Cucumber executed scenarios
     */
    @Override
    public List<ExecutedScenario> getExecutedScenarios(File cucumberFolder, Run run) {
        List<Feature> features = new ArrayList<>();
        List<String> stepDefinitions = new ArrayList<>();

        Optional<File> cucumberReportFile = getCucumberReportFileFromParentFolder(cucumberFolder);
        if (cucumberReportFile.isPresent()) {
            features = getCucumberFeaturesFromReport(cucumberReportFile.get());
        }

        Optional<File> stepDefinitionsFile = getCucumberStepDefinitionsFileFromParentFolder(cucumberFolder);
        if (stepDefinitionsFile.isPresent()) {
            stepDefinitions = getCucumberStepDefinitions(stepDefinitionsFile.get());
        }

        List<ExecutedScenario> executedScenarios = executedScenarioExtractorService.extractExecutedScenarios(
                features,
                stepDefinitions,
                run.getJobUrl()
        );

        return executedScenarios;
    }

    /**
     * Extract the Cucumber report file, if found
     * @param cucumberReportFolder the Cucumber report parent folder
     * @return the Cucumber report file, if found
     */
    private Optional<File> getCucumberReportFileFromParentFolder(File cucumberReportFolder) {
        final File[] allFolderContent = cucumberReportFolder.listFiles();
        final File[] filteredFolderContents = Arrays.stream(allFolderContent).filter(file -> file.isFile() && "report.json".equals(file.getName().toLowerCase())).toArray(File[]::new);
        if (filteredFolderContents.length == 1) {
            File cucumberReportFile = filteredFolderContents[0];
            return Optional.of(cucumberReportFile);
        }
        log.info("Found no report.json in {}", cucumberReportFolder.getPath());
        return Optional.empty();
    }

    /**
     * Get the features from the Cucumber report file
     * @param cucumberReport the Cucumber report file
     * @return the Cucumber features
     */
    private List<Feature> getCucumberFeaturesFromReport(File cucumberReport) {
        try (InputStream input = new FileInputStream(cucumberReport)) {
            return objectMapper.readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, Feature.class));
        } catch (IOException e) {
            log.info("Cannot download report.json in {}", cucumberReport.getPath(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get Cucumber step definitions file
     * @param cucumberReportFolder the folder to look at
     * @return the Cucumber step definitions file
     */
    private Optional<File> getCucumberStepDefinitionsFileFromParentFolder(File cucumberReportFolder) {
        final File[] allFolderContent = cucumberReportFolder.listFiles();
        final File[] filteredFolderContents = Arrays.stream(allFolderContent).filter(file -> file.isFile() && "stepDefinitions.json".equals(file.getName())).toArray(File[]::new);
        if (filteredFolderContents.length == 1) {
            File stepDefinitionsFile = filteredFolderContents[0];
            return Optional.of(stepDefinitionsFile);
        }
        log.info("Found no stepDefinitions.json in {}", cucumberReportFolder.getPath());
        return Optional.empty();
    }

    /**
     * Get Cucumber step definitions from the step definitions file
     * @param stepDefinitionsFile the file defining the Cucumber step definitions
     * @return all the extracted Cucumber step definitions
     */
    private List<String> getCucumberStepDefinitions(File stepDefinitionsFile) {
        try (InputStream input = new FileInputStream(stepDefinitionsFile)) {
            return objectMapper.readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            log.info("Cannot download stepDefinitions.json in {}", stepDefinitionsFile.getPath(), e);
            return new ArrayList<>();
        }
    }
}
