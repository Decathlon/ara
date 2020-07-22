/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.cypress.indexer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.indexer.CucumberScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.service.ExecutedScenarioExtractorService;
import com.decathlon.ara.scenario.cypress.settings.CypressSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CypressScenariosIndexer implements ScenariosIndexer {

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final TechnologySettingService technologySettingService;

    @NonNull
    private final FileProcessorService fileProcessorService;

    @NonNull
    private final CucumberScenariosIndexer cucumberScenariosIndexer;

    @NonNull
    private final ExecutedScenarioExtractorService executedScenarioExtractorService;

    @Override
    public List<ExecutedScenario> getExecutedScenarios(File parentFolder, Run run, Long projectId) {
        List<ExecutedScenario> executedScenarios = new ArrayList<>();

        String cucumberReportsFolderPath = technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_REPORTS_FOLDER_PATHS).orElse("");
        Optional<File> cucumberFolder = fileProcessorService.getMatchingDirectory(parentFolder, cucumberReportsFolderPath);

        if (!cucumberFolder.isPresent()) {
            return executedScenarios;
        }

        String cucumberSuffixValue = technologySettingService.getSettingValue(projectId, CypressSettings.CUCUMBER_FILE_NAME_SUFFIX_VALUE).orElse("");
        String cucumberReportFileNameSuffix = String.format(".%s.json", cucumberSuffixValue);
        File[] cucumberFolderContent = cucumberFolder.get().listFiles();
        List<File> cucumberReportFiles = Arrays.stream(cucumberFolderContent)
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(cucumberReportFileNameSuffix))
                .collect(Collectors.toList());

        if (cucumberReportFiles.isEmpty()) {
            return executedScenarios;
        }

        String stepDefinitionsFolderPath = technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FOLDER_PATH).orElse("");
        Optional<File> stepDefinitionFolder = fileProcessorService.getMatchingDirectory(parentFolder, stepDefinitionsFolderPath);
        final List<File> stepDefinitionFiles = stepDefinitionFolder
                .map(File::listFiles)
                .map(files -> {
                    String stepDefinitionsSuffixValue = technologySettingService.getSettingValue(projectId, CypressSettings.STEP_DEFINITIONS_FILE_NAME_SUFFIX_VALUE).orElse("");
                    String stepDefinitionFileNameSuffix = String.format(".%s.json", stepDefinitionsSuffixValue);
                    return Arrays.stream(files)
                            .filter(File::isFile)
                            .filter(file -> file.getName().endsWith(stepDefinitionFileNameSuffix))
                            .collect(Collectors.toList());
                })
                .orElse(new ArrayList<>());

        executedScenarios = cucumberReportFiles.stream()
                .map(file -> Pair.of(file, getStepDefinitionsFile(file, stepDefinitionFiles)))
                .map(pair ->
                        Pair.of(
                                cucumberScenariosIndexer.getCucumberFeaturesFromReport(pair.getFirst()),
                                pair.getSecond().isPresent() ? cucumberScenariosIndexer.getCucumberStepDefinitions(pair.getSecond().get()) : new ArrayList<String>()
                        )
                )
                .map(pair -> executedScenarioExtractorService.extractExecutedScenarios(pair.getFirst(), pair.getSecond(), run.getJobUrl()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return executedScenarios;
    }

    /**
     * Get the matching step definitions file from a Cucumber report, if found
     * @param cucumberReportFile the Cucumber report file
     * @param stepDefinitionsFiles all the step definitions files
     * @return a step definitions file, if any
     */
    private Optional<File> getStepDefinitionsFile(File cucumberReportFile, List<File> stepDefinitionsFiles) {
        Optional<File> matchingStepDefinition = stepDefinitionsFiles.stream()
                .filter(stepDefinitionsFile -> filesHaveTheSamePrefix(cucumberReportFile, stepDefinitionsFile))
                .findFirst();
        return matchingStepDefinition;
    }

    /**
     * Check whether 2 files share the same prefix
     * @param file1 the first file
     * @param file2 the second file
     * @return true iff, the files have different names but share the same prefix
     */
    private Boolean filesHaveTheSamePrefix(File file1, File file2) {
        if (file1.getName().equals(file2.getName())) {
            log.info("The 2 compared files share the same name ({})!", file1.getName());
            return false;
        }

        String prefix1 = getPrefixFromFile(file1);
        String prefix2 = getPrefixFromFile(file2);
        return prefix1.equals(prefix2);
    }

    /**
     * Get the prefix from a file name. E.g. for a file named 'prefix.type.ext', the prefix is the string 'prefix'
     * @param file the file to get the prefix from
     * @return the prefix
     */
    private String getPrefixFromFile(File file){
        String name = file.getName();
        String[] splitName = name.split("\\.");
        String prefix = splitName[0];
        return prefix;
    }
}
