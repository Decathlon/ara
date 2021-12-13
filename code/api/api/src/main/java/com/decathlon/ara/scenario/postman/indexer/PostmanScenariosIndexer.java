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

package com.decathlon.ara.scenario.postman.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.postman.model.NewmanParsingResult;
import com.decathlon.ara.scenario.postman.service.PostmanService;
import com.decathlon.ara.scenario.postman.settings.PostmanSettings;
import com.decathlon.ara.service.FileProcessorService;
import com.decathlon.ara.service.TechnologySettingService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

@Component
public class PostmanScenariosIndexer implements ScenariosIndexer {

    private static final Logger LOG = LoggerFactory.getLogger(PostmanScenariosIndexer.class);

    private final PostmanService postmanService;

    private final JsonFactory jsonFactory;

    private final TechnologySettingService technologySettingService;

    private final FileProcessorService fileProcessorService;

    public PostmanScenariosIndexer(PostmanService postmanService, JsonFactory jsonFactory,
            TechnologySettingService technologySettingService, FileProcessorService fileProcessorService) {
        this.postmanService = postmanService;
        this.jsonFactory = jsonFactory;
        this.technologySettingService = technologySettingService;
        this.fileProcessorService = fileProcessorService;
    }

    /**
     * Get the Postman executed scenarios
     * @param postmanFolder the folder containing all the Postman related files
     * @param run the run
     * @param projectId the project id
     * @return the Postman executed scenarios
     */
    @Override
    public List<ExecutedScenario> getExecutedScenarios(File postmanFolder, Run run, Long projectId) {
        List<List<ExecutedScenario>> allExecutedScenarios = new ArrayList<>();
        String postmanReportFolderName = technologySettingService.getSettingValue(projectId, PostmanSettings.REPORTS_PATH).orElse("");
        List<File> postmanReports = getNewmanReportFiles(postmanFolder, postmanReportFolderName);
        String resultFileName = technologySettingService.getSettingValue(projectId, PostmanSettings.RESULT_FILE_NAME).orElse("").toLowerCase();
        Boolean containsResult = postmanReports.stream()
                .anyMatch(file -> resultFileName.equals(file.getName().toLowerCase()));
        if (containsResult) {
            List<File> postmanReportsWithoutResultFile = postmanReports.stream()
                    .filter(file -> !resultFileName.equals(file.getName().toLowerCase()))
                    .collect(Collectors.toList());

            AtomicInteger requestPosition = new AtomicInteger(0);

            for (File postmanReportFile : postmanReportsWithoutResultFile) {
                final NewmanParsingResult newmanParsingResult = new NewmanParsingResult();
                try {
                    JsonParserConsumer consumer = jsonParser -> postmanService.parse(jsonParser, newmanParsingResult);
                    try (InputStream input = new FileInputStream(postmanReportFile); JsonParser parser = jsonFactory.createParser(input)) {
                        consumer.accept(parser);
                    } catch (IOException e) {
                        LOG.error("Error while handling the postman report file {}", postmanReportFile.getPath(), e);
                        return new ArrayList<>();
                    }
                    List<ExecutedScenario> currentFileExecutedScenarios = postmanService.postProcess(run, newmanParsingResult, postmanReportFile.getName(), requestPosition);
                    allExecutedScenarios.add(currentFileExecutedScenarios);
                } finally {
                    postmanService.deleteTempFiles(newmanParsingResult);
                }
            }
        }
        return allExecutedScenarios.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Extract the Newman report files, i.e. files in the reports folder
     * @param newmanFolder the newman folder
     * @param newmanReportFolderName the name (or relative path) of the folder containing the reports
     * @return all the report files
     */
    private List<File> getNewmanReportFiles(File newmanFolder, String newmanReportFolderName) {
        List<File> newmanReportFiles = new ArrayList<>();
        final Optional<File> newmanReportFolder = fileProcessorService.getMatchingDirectory(newmanFolder, newmanReportFolderName);
        if (newmanReportFolder.isPresent()) {
            final File reportFolder = newmanReportFolder.get();
            final File[] allReportFolderContent = reportFolder.listFiles();
            newmanReportFiles = Arrays.asList(Arrays.stream(allReportFolderContent).filter(File::isFile).toArray(File[]::new));
        }
        return newmanReportFiles;
    }
}
