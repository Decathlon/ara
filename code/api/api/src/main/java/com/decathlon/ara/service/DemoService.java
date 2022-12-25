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

package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.loader.*;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.decathlon.ara.loader.DemoLoaderConstants.DEMO_PROJECT_CODE;

/**
 * Service for managing a Demo project.
 */
@Service
@Transactional
public class DemoService {

    private static final Logger LOG = LoggerFactory.getLogger(DemoService.class);

    private final ProjectRepository projectRepository;

    private final ProjectService projectService;

    private final SettingService settingService;

    private final DemoExecutionLoader demoExecutionLoader;

    private final DemoFunctionalityLoader demoFunctionalityLoader;

    private final DemoProblemLoader demoProblemLoader;

    private final DemoScenarioLoader demoScenarioLoader;

    private final DemoSettingsLoader demoSettingsLoader;

    @Autowired
    public DemoService(ProjectRepository projectRepository, ProjectService projectService,
            SettingService settingService, DemoExecutionLoader demoExecutionLoader,
            DemoFunctionalityLoader demoFunctionalityLoader, DemoProblemLoader demoProblemLoader,
            DemoScenarioLoader demoScenarioLoader, DemoSettingsLoader demoSettingsLoader) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.settingService = settingService;
        this.demoExecutionLoader = demoExecutionLoader;
        this.demoFunctionalityLoader = demoFunctionalityLoader;
        this.demoProblemLoader = demoProblemLoader;
        this.demoScenarioLoader = demoScenarioLoader;
        this.demoSettingsLoader = demoSettingsLoader;
    }

    /**
     * If not existing yet, create the demo project to show users what ARA looks like.
     *
     * @return the created demo project
     * @throws BadRequestException if the demo project already exists
     */
    @Transactional
    public ProjectDTO create() throws BadRequestException {
        if (projectService.exists(DEMO_PROJECT_CODE)) {
            throw new BadRequestException(Messages.RULE_DEMO_PROJECT_ALREADY_EXISTS, Entities.PROJECT, "demo-exists");
        }

        // IMPORTANT: Use DTO constructors everywhere,
        // to make sure the project will not compile when new fields are not thought about for the demo project

        final ProjectDTO project = demoSettingsLoader.createProjectWithCommunicationsAndRootCauses();
        long projectId = project.getId();

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
        final Project project = projectRepository.findOneByCode(DEMO_PROJECT_CODE);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }

        final String executionBasePath = settingService.get(project.getId().longValue(),
                Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH);

        projectRepository.delete(project);

        if (executionBasePath.contains(Settings.PROJECT_VARIABLE)) {
            final String projectExecutionsFolder = executionBasePath
                    .replace(Settings.PROJECT_VARIABLE, DEMO_PROJECT_CODE)
                    .replace(Settings.BRANCH_VARIABLE, "")
                    .replace(Settings.CYCLE_VARIABLE, "");
            try {
                FileUtils.deleteDirectory(new File(projectExecutionsFolder));
            } catch (IOException e) {
                LOG.warn("DEMO|execution|Cannot delete the temporary directory {} for the demo project's executions", projectExecutionsFolder, e);
            }
        }
    }

}
