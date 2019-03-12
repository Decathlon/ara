package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.loader.DemoExecutionLoader;
import com.decathlon.ara.loader.DemoFunctionalityLoader;
import com.decathlon.ara.loader.DemoProblemLoader;
import com.decathlon.ara.loader.DemoScenarioLoader;
import com.decathlon.ara.loader.DemoSettingsLoader;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;

/**
 * Service for managing a Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoService {

    @NonNull
    private final ProjectRepository projectRepository;

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final SettingService settingService;

    @NonNull
    private final DemoExecutionLoader demoExecutionLoader;

    @NonNull
    private final DemoFunctionalityLoader demoFunctionalityLoader;

    @NonNull
    private final DemoProblemLoader demoProblemLoader;

    @NonNull
    private final DemoScenarioLoader demoScenarioLoader;

    @NonNull
    private final DemoSettingsLoader demoSettingsLoader;

    /**
     * If not existing yet, create the demo project to show users what ARA looks like.
     *
     * @return the created demo project
     * @throws BadRequestException if the demo project already exists
     */
    @Transactional
    public ProjectDTO create() throws BadRequestException {
        if (projectService.findOne(PROJECT_CODE_DEMO).isPresent()) {
            throw new BadRequestException(Messages.RULE_DEMO_PROJECT_ALREADY_EXISTS, Entities.PROJECT, "demo-exists");
        }

        // IMPORTANT: Use DTO constructors everywhere,
        // to make sure the project will not compile when new fields are not thought about for the demo project

        final ProjectDTO project = demoSettingsLoader.createProjectWithCommunicationsAndRootCauses();
        long projectId = project.getId().longValue();

        demoSettingsLoader.updateSettings(projectId);

        demoSettingsLoader.setCommunications(projectId);

        demoSettingsLoader.createSources(projectId);
        demoSettingsLoader.createTypes(projectId);
        demoSettingsLoader.createCountries(projectId);
        demoSettingsLoader.createSeverities(projectId);

        List<TeamDTO> teams = demoSettingsLoader.createTeams(projectId);
        demoProblemLoader.createProblems(projectId, teams);

        final Map<String, Long> functionalityIds = demoFunctionalityLoader.createFunctionalityTree(projectId, teams);
        demoScenarioLoader.createScenarios(projectId, functionalityIds);
        final List<CycleDefinitionDTO> cycleDefinitions = demoSettingsLoader.createCycleDefinitions(projectId);
        demoExecutionLoader.importExecution(projectId, functionalityIds, cycleDefinitions);

        return project;
    }

    /**
     * Delete the demo project from database and indexing directory.
     *
     * @throws NotFoundException if there is no demo project
     */
    public void delete() throws NotFoundException {
        final Project project = projectRepository.findOneByCode(PROJECT_CODE_DEMO);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }

        final String executionBasePath = settingService.get(project.getId().longValue(),
                Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH);

        projectRepository.delete(project);

        if (executionBasePath.contains(Fetcher.PROJECT_VARIABLE)) {
            final String projectExecutionsFolder = executionBasePath
                    .replace(Fetcher.PROJECT_VARIABLE, PROJECT_CODE_DEMO)
                    .replace(Fetcher.BRANCH_VARIABLE, "")
                    .replace(Fetcher.CYCLE_VARIABLE, "");
            try {
                FileUtils.deleteDirectory(new File(projectExecutionsFolder));
            } catch (IOException e) {
                log.error("Cannot delete the temporary directory for the demo project's executions: " +
                        projectExecutionsFolder, e);
            }
        }
    }

}
