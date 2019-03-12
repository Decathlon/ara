package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO (SNI) : TO be deleted after validation of FileSystemFetcher
@Service
@Transactional
public class OldFileSystemFetcher extends FileSystemFetcher implements PullFetcher {

    public OldFileSystemFetcher(ProjectRepository projectRepository, SettingService settingService, SettingProviderService settingProviderService, JsonFactory jsonFactory, ObjectMapper objectMapper) {
        super(projectRepository, settingService, settingProviderService, jsonFactory, objectMapper);
    }

    @Override
    public String getCode() {
        return "old-filesystem";
    }

    @Override
    public String getName() {
        return "Old file system (to be removed soon)";
    }

    /**
     * List sub-folders contained in the folder "{executionBasePath}/{branchName}/{cycleName}": all sub-folders are
     * considered execution folders and being a timestamp
     *
     * @param branchName the branch name (eg. "master", "develop"...)
     * @param cycleName  the cycle name (eg. "day", "night"...)
     * @return an history of the latest job executions (latest first)
     * @throws FetchException when the searched folder does not exist, is not readable, is a file, or an I/O error
     *                        occurred
     */
    @Override
    public List<Build> getJobHistory(long projectId, String branchName, String cycleName) throws FetchException {
        final String projectCode = projectRepository.findById(projectId)
                .map(Project::getCode)
                .orElse(PROJECT_VARIABLE);

        String path = settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH)
                .replace(PROJECT_VARIABLE, projectCode)
                .replace(BRANCH_VARIABLE, branchName)
                .replace(CYCLE_VARIABLE, cycleName);
        File[] directories = new File(path).listFiles(File::isDirectory);

        if (directories == null) {
            throw new FetchException("Error while searching for executions in directory " + path + DIRECTORY_HINT);
        }

        try {
            return Arrays.stream(directories)
                    .filter(directory -> !directory.getName().contains("incoming"))
                    .map(buildDirectory -> new Build()
                            .withLink(buildDirectory.getAbsolutePath() + File.separator)
                            .withTimestamp(Long.parseLong(buildDirectory.getName())))
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new FetchException("One sub-directory of " + path + " is not a timestamp: " + e.getMessage(), e);
        }
    }

}
