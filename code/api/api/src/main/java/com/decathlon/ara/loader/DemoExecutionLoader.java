/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
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

package com.decathlon.ara.loader;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.PlatformRule;
import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.service.ExecutionService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.decathlon.ara.loader.DemoLoaderConstants.*;

/**
 * Service for loading executions into the Demo project.
 */
@Service
@Transactional
public class DemoExecutionLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DemoExecutionLoader.class);

    private static final Pattern BUILD_INFORMATION_TIMESTAMP_PATTERN = Pattern.compile("\"timestamp\":\\s[0-9]{13}");
    private static final String BUILD_INFORMATION_TIMESTAMP_REPLACEMENT = "\"timestamp\": %1s";

    private static final Pattern BUILD_INFORMATION_VERSION_TIMESTAMP_PATTERN = Pattern.compile("\"versionTimestamp\":\\s[0-9]{13}");
    private static final String BUILD_INFORMATION_VERSION_TIMESTAMP_REPLACEMENT = "\"versionTimestamp\": %1s";

    private static final Pattern POSTMAN_ECHO_HOST_PATTERN = Pattern.compile("postman-echo.com");

    private static final BiPredicate<Path, BasicFileAttributes> JSON_FILE_FILTER = (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().endsWith(".json");

    private final SettingService settingService;

    private final DemoLoaderService demoLoaderService;

    private final ExecutionService executionService;

    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final CycleDefinitionRepository cycleDefinitionRepository;

    private final AtomicInteger nextJobId = new AtomicInteger(42);

    public DemoExecutionLoader(SettingService settingService, DemoLoaderService demoLoaderService,
            ExecutionService executionService, EntityManager entityManager, ObjectMapper objectMapper,
            CycleDefinitionRepository cycleDefinitionRepository) {
        this.settingService = settingService;
        this.demoLoaderService = demoLoaderService;
        this.executionService = executionService;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.cycleDefinitionRepository = cycleDefinitionRepository;
    }

    /**
     * Import one demo execution for a given project, customized a little bit to appear to have run a few seconds or
     * days ago, and to use existing functionality IDs of the project.
     *
     * @param projectId        the ID of the project in which to work
     * @param functionalityIds a map of letters (eg. "A" for the placeholder "{{F-A}}") as keys, and functionality IDs
     *                         as values
     * @param cycleDefinitions all cycles of the project: indexing directories will be created for each of them
     */
    public void importExecution(long projectId, Map<String, Long> functionalityIds, List<CycleDefinitionDTO> cycleDefinitions) {
        final LocalDate now = LocalDate.now();
        final LocalTime nightTime = LocalTime.of(3, 0);
        final LocalTime dayTime = LocalTime.of(8, 0);
        long pastDay = LocalDateTime.of(now.minusDays(2), dayTime).toEpochSecond(ZoneOffset.UTC) * 1000;
        long yesterdayDay = LocalDateTime.of(now.minusDays(1), dayTime).toEpochSecond(ZoneOffset.UTC) * 1000;
        long todayDay = LocalDateTime.of(now, dayTime).toEpochSecond(ZoneOffset.UTC) * 1000;
        long pastNight = LocalDateTime.of(now.minusDays(2), nightTime).toEpochSecond(ZoneOffset.UTC) * 1000;
        long yesterdayNight = LocalDateTime.of(now.minusDays(1), nightTime).toEpochSecond(ZoneOffset.UTC) * 1000;
        long todayNight = LocalDateTime.of(now, nightTime).toEpochSecond(ZoneOffset.UTC) * 1000;

        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_DAY, 0, pastDay);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_DAY, 1, yesterdayDay);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_DAY, 2, todayDay);

        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_NIGHT, 0, pastNight);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_NIGHT, 1, yesterdayNight);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_DEVELOP, CYCLE_NIGHT, 2, todayNight);

        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_MASTER, CYCLE_DAY, 0, pastDay);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_MASTER, CYCLE_DAY, 0, yesterdayDay);
        importExecution(projectId, functionalityIds, cycleDefinitions,
                BRANCH_MASTER, CYCLE_DAY, 0, todayDay);
    }

    private void importExecution(long projectId,
                                 Map<String, Long> functionalityIds,
                                 List<CycleDefinitionDTO> cycleDefinitions,
                                 String branch,
                                 String cycle,
                                 int failingLevel,
                                 long timestamp) {
        File tempDirectory = null;
        try {
            // Create the execution in a temporary directory
            tempDirectory = Files.createTempDirectory("ara_demo_executions_work_").toFile();

            long versionTimestamp = timestamp - 5 * 60 * 1000;

            File executionDirectory = new File(tempDirectory, String.valueOf(timestamp));
            Files.createDirectories(executionDirectory.toPath());

            // Simple (one country, one Web type) or complete execution
            boolean complete = CYCLE_NIGHT.equals(cycle) || BRANCH_MASTER.equals(branch);
            List<String> countries = (complete ? Arrays.asList("fr", "us") : Collections.singletonList("fr"));
            final String testTypes = "api,firefox-desktop" + (complete ? ",firefox-mobile" : "");
            createExecutionJob(executionDirectory, branch, cycle, timestamp, versionTimestamp, countries, testTypes);

            for (String country : countries) {
                createDeploymentJob(new File(executionDirectory, country), country, timestamp);
                createNewmanReports(
                        createRunJob(new File(executionDirectory, country + "/api"), timestamp),
                        failingLevel);
                createCucumberReports(
                        createRunJob(new File(executionDirectory, country + "/firefox-desktop"), timestamp),
                        failingLevel);
                if (complete) {
                    createCucumberReports(
                            createRunJob(new File(executionDirectory, country + "/firefox-mobile"), timestamp),
                            failingLevel);
                }
            }

            final String testTimestamp = String.valueOf(System.currentTimeMillis());

            // Replace timestamp in all buildInformation.json too
            // (warning: the file name can vary by project => replace in all .json files)
            replaceInFiles(executionDirectory, JSON_FILE_FILTER,
                    content -> BUILD_INFORMATION_TIMESTAMP_PATTERN.matcher(content)
                            .replaceAll(String.format(BUILD_INFORMATION_TIMESTAMP_REPLACEMENT, testTimestamp)));
            replaceInFiles(executionDirectory, JSON_FILE_FILTER,
                    content -> BUILD_INFORMATION_VERSION_TIMESTAMP_PATTERN.matcher(content)
                            .replaceAll(String.format(BUILD_INFORMATION_VERSION_TIMESTAMP_REPLACEMENT, Long.valueOf(versionTimestamp))));

            // Replace all functionality IDs
            replaceInFiles(executionDirectory, JSON_FILE_FILTER,
                    content -> demoLoaderService.replaceFunctionalityIdPlaceholders(functionalityIds, content));

            // When running Newman, we queried Postman-Echo: replace it with our supposed URL
            replaceInFiles(executionDirectory, JSON_FILE_FILTER,
                    content -> POSTMAN_ECHO_HOST_PATTERN.matcher(content)
                            .replaceAll("integration.our-lovely-store.com"));

            final String executionBasePath = settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH);

            // Create directories for all cycles so there will be no exception in logs if the indexer does not find them
            for (CycleDefinitionDTO cycleDefinition : cycleDefinitions) {
                final File cycleDirectory = new File(executionBasePath
                        .replace(Settings.PROJECT_VARIABLE, PROJECT_CODE_DEMO)
                        .replace(Settings.BRANCH_VARIABLE, cycleDefinition.getBranch())
                        .replace(Settings.CYCLE_VARIABLE, cycleDefinition.getName()));
                Files.createDirectories(cycleDirectory.toPath());
            }

            // The execution is ready to be indexed by ExecutionDiscovererService
            this.entityManager.flush();
            CycleDefinition cycleDefinition = cycleDefinitionRepository.findByProjectIdAndBranchAndName(projectId, branch, cycle)
                    .orElseThrow(() -> new IllegalArgumentException("The branch or cycle for this project doesn't exists."));
            this.executionService.processSpecificDirectory(cycleDefinition, executionDirectory);
            // ^ Deletion of executionDirectory is made by this call
        } catch (IOException e) {
            LOG.error("DEMO|Cannot import executions: " + e.getMessage(), e);
        } finally {
            // executionDirectory has been deleted, but not its containing temporary directory yet
            FileUtils.deleteQuietly(tempDirectory);
        }
    }

    private void createExecutionJob(File executionDirectory,
                                    String branch,
                                    String cycle,
                                    long timestamp,
                                    long versionTimestamp,
                                    List<String> countries,
                                    String testTypes) throws IOException {
        final List<PlatformRule> platformRules = new ArrayList<>();
        for (String country : countries) {
            platformRules.add(new PlatformRule(true, country, "all", testTypes, "all", true));
        }

        Map<String, QualityThreshold> qualityThresholds = new HashedMap<>();
        qualityThresholds.put("sanity-check", new QualityThreshold(100, 100));
        qualityThresholds.put("high", new QualityThreshold(95, 98));
        qualityThresholds.put("medium", new QualityThreshold(90, 95));

        CycleDef cycleDefinition = new CycleDef(true, Collections.singletonMap("integ", platformRules), qualityThresholds);

        writeObjectToFile(cycleDefinition, new File(executionDirectory, "cycleDefinition.json"));

        Build buildInformation = new Build("https://build.company.com/demo/" + branch + "/" + cycle + "/" + nextJobId.getAndIncrement() + "/", Result.SUCCESS, timestamp, BRANCH_MASTER.equals(branch) ? "v2" : "v3", randomGitCommitId(), Long.valueOf(versionTimestamp));
        writeObjectToFile(buildInformation, new File(executionDirectory, "buildInformation.json"));
    }

    private void createDeploymentJob(File deploymentDirectory, String country, long timestamp) throws IOException {
        Build buildInformation = new Build("https://build.company.com/demo/deploy/" + country + "/" + nextJobId.getAndIncrement() + "/", Result.SUCCESS, timestamp);
        writeObjectToFile(buildInformation, new File(deploymentDirectory, "buildInformation.json"));
    }

    private File createRunJob(File runDirectory, long timestamp) throws IOException {
        Build buildInformation = new Build("https://build.company.com/demo/test/" + nextJobId.getAndIncrement() + "/", Result.SUCCESS, timestamp);
        writeObjectToFile(buildInformation, new File(runDirectory, "buildInformation.json"));
        return runDirectory;
    }

    private void createCucumberReports(File runDirectory, int failingLevel) throws IOException {
        copyResourceToFile(
                "reports/demo/report-" + failingLevel + ".json",
                new File(runDirectory, "report.json"));
        copyResourceToFile(
                "reports/demo/stepDefinitions.json",
                new File(runDirectory, "stepDefinitions.json"));
    }

    private void createNewmanReports(File runDirectory, int failingLevel) throws IOException {
        final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        String parentFolder = "newman-reports-" + Math.min(failingLevel, 1) + "/";
        final String newmanReportsLocation = "classpath:demo/" + parentFolder;
        resourceResolver.getResource(newmanReportsLocation);
        Resource[] jsonResources = resourceResolver.getResources(newmanReportsLocation + "**/*.json");
        for (Resource resource : jsonResources) {
            final String relativePath = resource.getFilename();
            copyStreamToFile(resource.getInputStream(), new File(runDirectory, "reports/" + relativePath));
        }

        FileUtils.writeStringToFile(new File(runDirectory, "reports/result.txt"), "SUCCESS", StandardCharsets.UTF_8);
    }

    private void copyResourceToFile(String resourceName, File destinationFile) throws IOException {
        copyStreamToFile(getClass().getClassLoader().getResourceAsStream(resourceName), destinationFile);
    }

    private void copyStreamToFile(InputStream stream, File destinationFile) throws IOException {
        Files.createDirectories(destinationFile.getParentFile().toPath());
        try (InputStream input = stream;
             FileOutputStream output = new FileOutputStream(destinationFile)) {
            IOUtils.copy(input, output);
        }
    }

    private String randomGitCommitId() {
        Random random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }
        char[] id = new char[40];
        for (int i = 0; i < id.length; i++) {
            id[i] = Integer.toHexString(random.nextInt(16)).charAt(0);
        }
        return new String(id);
    }

    /**
     * Write an object to a JSON file.
     *
     * @param object the object to serialize
     * @param file   the file to write
     * @throws IOException in case of an I/O error
     */
    private void writeObjectToFile(Object object, File file) throws IOException {
        final String json;

        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new NotGonnaHappenException("JSON serializing should not have failed when serializing to a String", e);
        }

        FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
    }

    /**
     * Recursively find files matching a filter in a starting directory, read all files, apply a replace function to
     * them, and save the results back to disk.
     *
     * @param startDirectory  the root directory that will be searched for files matching the filter and replace them
     * @param fileFilter      matching files will be replaced using the replace function
     * @param replaceFunction a mapping function to apply to the file content
     * @throws IOException if an I/O error is thrown when accessing the start directory (each file will fail in silence)
     */
    private void replaceInFiles(File startDirectory,
                                BiPredicate<Path, BasicFileAttributes> fileFilter,
                                Function<String, String> replaceFunction) throws IOException {
        try (final Stream<Path> paths = Files.find(startDirectory.toPath(), 999, fileFilter)) {
            paths.forEach(jsonFilePath -> replaceInFile(jsonFilePath, replaceFunction));
        }
    }

    /**
     * Read a file, apply a replace function to it, and save the result back to disk.<br>
     * Exceptions are logged: the method will never fail.
     *
     * @param filePath        a UTF-8 file to read and replace
     * @param replaceFunction a mapping function to apply to the file content
     */
    private void replaceInFile(Path filePath, Function<String, String> replaceFunction) {
        try {
            final File file = filePath.toFile();
            final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            final String replacedContent = replaceFunction.apply(content);
            FileUtils.writeStringToFile(file, replacedContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Cannot rethrow the exception, as the method is called in a lambda, and it's better to have the execution
            // imported, even without the right functionalities, instead of nothing
            LOG.error("DEMO|Cannot replace functionality ID placeholders in file " + filePath, e);
        }
    }

}
