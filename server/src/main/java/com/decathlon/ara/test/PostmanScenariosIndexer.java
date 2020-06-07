package com.decathlon.ara.test;

import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.postman.model.NewmanParsingResult;
import com.decathlon.ara.postman.service.PostmanService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PostmanScenariosIndexer implements ScenariosIndexer {

    @NonNull
    private final PostmanService postmanService;

    @NonNull
    private final JsonFactory jsonFactory;

    /**
     * Get the Postman executed scenarios
     * @param postmanFolder the folder containing all the Postman related files
     * @param run the run
     * @return the Postman executed scenarios
     */
    @Override
    public List<ExecutedScenario> getExecutedScenarios(File postmanFolder, Run run) {
        List<List<ExecutedScenario>> allExecutedScenarios = new ArrayList<>();
        List<File> postmanReports = getNewmanReportFiles(postmanFolder);
        Boolean containsResult = postmanReports.stream()
                .anyMatch(file -> "result.txt".equals(file.getName().toLowerCase()));
        if (containsResult) {
            List<File> postmanReportsWithoutResultFile = postmanReports.stream()
                    .filter(file -> !"result.txt".equals(file.getName().toLowerCase()))
                    .collect(Collectors.toList());

            AtomicInteger requestPosition = new AtomicInteger(0);

            for (File postmanReportFile : postmanReportsWithoutResultFile) {
                final NewmanParsingResult newmanParsingResult = new NewmanParsingResult();
                try {
                    JsonParserConsumer consumer = jsonParser -> postmanService.parse(jsonParser, newmanParsingResult);
                    try (InputStream input = new FileInputStream(postmanReportFile); JsonParser parser = jsonFactory.createParser(input)) {
                        consumer.accept(parser);
                    } catch (IOException e) {
                        log.error("Error while handling the postman report file {}", postmanReportFile.getPath(), e);
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
     * @return all the report files
     */
    private List<File> getNewmanReportFiles(File newmanFolder) {
        List<File> newmanReportFiles = new ArrayList<>();
        final File[] allNewmanFolderContent = newmanFolder.listFiles();
        final File[] filteredNewmanFolderContents = Arrays.stream(allNewmanFolderContent).filter(file -> file.isDirectory() && "reports".equals(file.getName().toLowerCase())).toArray(File[]::new);
        if (filteredNewmanFolderContents.length == 1) {
            final File reportFolder = filteredNewmanFolderContents[0];
            final File[] allReportFolderContent = reportFolder.listFiles();
            newmanReportFiles = Arrays.asList(Arrays.stream(allReportFolderContent).filter(File::isFile).toArray(File[]::new));
        }
        return newmanReportFiles;
    }
}
