package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.CountryDeploymentExecution;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.ExecutionTree;
import com.decathlon.ara.ci.bean.NrtExecution;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileSystemFetcher implements Fetcher {

    public static final String FILESYSTEM = "filesystem";

    static final String DIRECTORY_HINT =
            " (it may not exist, have wrong permissions, be a file, or triggered I/O error (likely if an NFS folder))";

    @NonNull
    protected final ProjectRepository projectRepository;

    @NonNull
    protected final SettingService settingService;

    @NonNull
    private final SettingProviderService settingProviderService;

    @NonNull
    private final JsonFactory jsonFactory;

    @NonNull
    private final ObjectMapper objectMapper;

    private static <T> Optional<T> handle(IOException e, String path)
            throws FetchException {
        if (e instanceof FileNotFoundException) {
            return Optional.empty();
        }
        throw new FetchException(e, path);
    }

    @Override
    public String getCode() {
        return FILESYSTEM;
    }

    @Override
    public String getName() {
        return "File-system indexer (from ARA server disk or mount point)";
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return settingProviderService.getJobIndexingFileSystemDefinitions();
    }

    @Override
    public ExecutionTree getTree(long projectId, Build build) throws FetchException {
        String localExecutionFolder = build.getLink();

        File[] countryJobDirectories = new File(localExecutionFolder).listFiles(File::isDirectory);

        if (countryJobDirectories == null) {
            throw new FetchException("Error while searching for country jobs in directory " + localExecutionFolder + DIRECTORY_HINT);
        }

        List<CountryDeploymentExecution> deployedCountries = new ArrayList<>();
        List<NrtExecution> nonRegressionTests = new ArrayList<>();

        for (File countryJobDirectory : countryJobDirectories) {
            String country = countryJobDirectory.getName();
            Build countryBuild = new Build().withLink(countryJobDirectory.getPath() + File.separator);
            completeBuildInformation(projectId, countryBuild);
            deployedCountries.add(new CountryDeploymentExecution()
                    .withBuild(countryBuild)
                    .withCountry(country));

            File[] typeJobDirectories = new File(countryBuild.getLink()).listFiles(File::isDirectory);

            if (typeJobDirectories == null) {
                throw new FetchException("Error while searching for type jobs in directory " + countryBuild.getLink() + DIRECTORY_HINT);
            }

            for (File typeJobDirectory : typeJobDirectories) {
                String type = typeJobDirectory.getName();
                Build typeBuild = new Build().withLink(typeJobDirectory.getPath() + File.separator);
                completeBuildInformation(projectId, typeBuild);
                nonRegressionTests.add(new NrtExecution()
                        .withCountry(country)
                        .withType(type)
                        .withBuild(typeBuild));
            }
        }

        return new ExecutionTree().withDeployedCountries(deployedCountries).withNonRegressionTests(nonRegressionTests);
    }

    @Override
    public Optional<CycleDef> getCycleDefinition(long projectId, Build build) throws FetchException {
        String path = build.getLink() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH);
        try (InputStream input = new FileInputStream(path)) {
            return Optional.ofNullable(objectMapper.readValue(input, CycleDef.class));
        } catch (IOException e) {
            return handle(e, path);
        }
    }

    @Override
    public void completeBuildInformation(long projectId, Build build) throws FetchException {
        String path = build.getLink() + settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH);
        try (InputStream input = new FileInputStream(path)) {
            Build buildInformation = objectMapper.readValue(input, Build.class);
            build.setUrl(buildInformation.getUrl());
            build.setTimestamp(buildInformation.getTimestamp());
            build.setDisplayName(buildInformation.getDisplayName());
            build.setDuration(buildInformation.getDuration());
            build.setEstimatedDuration(buildInformation.getEstimatedDuration());
            build.setResult(buildInformation.getResult());
            build.setBuilding(buildInformation.isBuilding());
            build.setRelease(buildInformation.getRelease());
            build.setVersion(buildInformation.getVersion());
            build.setVersionTimestamp(buildInformation.getVersionTimestamp());
            build.setComment(buildInformation.getComment());
        } catch (IOException e) {
            throw new FetchException(e, path);
        }
    }

    @Override
    public Optional<List<Feature>> getCucumberReport(long projectId, Run run) throws FetchException {
        String path = run.getJobLink() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_REPORT_PATH);
        try (InputStream input = new FileInputStream(path)) {
            return Optional.ofNullable(objectMapper
                    .readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, Feature.class)));
        } catch (IOException e) {
            return handle(e, path);
        }
    }

    @Override
    public Optional<List<String>> getCucumberStepDefinitions(long projectId, Run run) throws FetchException {
        String path = run.getJobLink() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_STEP_DEFINITIONS_PATH);
        try (InputStream input = new FileInputStream(path)) {
            return Optional.ofNullable(objectMapper
                    .readValue(input, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        } catch (IOException e) {
            return handle(e, path);
        }
    }

    @Override
    public List<String> getNewmanReportPaths(long projectId, Run run) throws FetchException {
        final Path reportsFolderPath = new File(run.getJobLink() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH)).toPath();
        try (final Stream<Path> walker = Files.walk(reportsFolderPath)) {
            return walker
                    .filter(Files::isRegularFile)
                    .map(path -> reportsFolderPath.toUri().relativize(path.toFile().toURI()).getPath())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FetchException("Failed to search for Newman reports in folder " + reportsFolderPath +
                    DIRECTORY_HINT, e);
        }
    }

    @Override
    public void streamNewmanResult(long projectId, Run run, String newmanReportPath, JsonParserConsumer consumer)
            throws FetchException {
        final Path reportsFolderPath = new File(run.getJobLink() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH)).toPath();
        final String url = reportsFolderPath + File.separator + newmanReportPath;
        try (InputStream input = new FileInputStream(url); JsonParser parser = jsonFactory.createParser(input)) {
            consumer.accept(parser);
        } catch (IOException e) {
            throw new FetchException(e, url);
        }
    }

    /**
     * If enabled in settings, delete the directory that served to successfully index the given now-finished execution.
     *
     * @param projectId the ID of the project in which to work
     * @param execution the DONE indexed execution
     * @throws FetchException if the directory cannot be deleted
     */
    @Override
    public void onDoneExecutionIndexingFinished(long projectId, Execution execution) throws FetchException {
        if (settingService.getBoolean(projectId, Settings.EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE)) {
            try {
                FileUtils.deleteDirectory(new File(execution.getJobLink()));
            } catch (IOException | IllegalArgumentException e) {
                throw new FetchException("Cannot delete directory " + execution.getJobLink(), e);
            }
        }
    }

}
