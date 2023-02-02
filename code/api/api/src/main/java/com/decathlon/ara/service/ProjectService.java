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
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.security.service.UserSessionService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ProjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Service for managing Project.
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final RootCauseRepository rootCauseRepository;

    private final ProjectMapper projectMapper;

    private final CommunicationService communicationService;

    private final UserSessionService userSessionService;

    public ProjectService(
            ProjectRepository projectRepository,
            RootCauseRepository rootCauseRepository,
            ProjectMapper projectMapper,
            CommunicationService communicationService,
            UserSessionService userSessionService
    ) {
        this.projectRepository = projectRepository;
        this.rootCauseRepository = rootCauseRepository;
        this.projectMapper = projectMapper;
        this.communicationService = communicationService;
        this.userSessionService = userSessionService;
    }

    /**
     * Create a new project.
     *
     * @param projectToCreate the project to save
     * @return the persisted project
     * @throws NotUniqueException when the given code or name is already used by another project
     */
    public ProjectDTO create(@NonNull ProjectDTO projectToCreate, @NonNull User creationUser) throws NotUniqueException {
        validateBusinessRules(projectToCreate);
        final Project projectToSave = projectMapper.getProjectFromProjectDTO(projectToCreate);
        projectToSave.setCreationUser(creationUser);
        communicationService.initializeProject(projectToSave);
        final ProjectDTO createdProject = projectMapper.getProjectDTOFromProject(projectRepository.save(projectToSave));

        final long projectId = createdProject.getId();
        rootCauseRepository.saveAll(Arrays.asList(
                new RootCause(projectId, "Fragile test"),
                new RootCause(projectId, "Network issue"),
                new RootCause(projectId, "Regression"),
                new RootCause(projectId, "Test to update")));

        return createdProject;
    }

    /**
     * Create a new project from a code.
     * The project name is generated from the code given.
     * @param projectCode the project code
     * @return the created {@link ProjectDTO}
     * @throws NotUniqueException thrown when project code already exists
     */
    public ProjectDTO createFromCode(@NonNull String projectCode, @NonNull User creationUser) throws NotUniqueException {
        var projectName =  getProjectNameFromCode(projectCode);
        var projectToCreate = new ProjectDTO(projectCode, projectName);
        return create(projectToCreate, creationUser);
    }

    private static String getProjectNameFromCode(String projectCode) {
        UnaryOperator<String> capitalizeFirstLetter = string -> Character.toUpperCase(string.charAt(0)) + string.substring(1);
        var projectNameFromCode = Arrays.stream(projectCode.split("[^a-zA-Z0-9]+"))
                .map(capitalizeFirstLetter)
                .collect(Collectors.joining(" "));
        return String.format("%s (generated)", projectNameFromCode);
    }

    /**
     * Update a project.
     *
     * @param projectToUpdate the project to update
     * @return the updated project, if the project exists
     * @throws NotFoundException  when the given project does not exist
     * @throws NotUniqueException when the given code or name is already used by another project
     */
    public ProjectDTO update(@NonNull ProjectDTO projectToUpdate, @NonNull User updateUser) throws BadRequestException {
        var newProjectName = projectToUpdate.getName();
        if (StringUtils.isBlank(newProjectName)) {
            throw new BadRequestException("The project name cannot be left blank!", Entities.PROJECT, "project_name_blank");
        }

        var persistedProject = projectRepository.findByCode(projectToUpdate.getCode()).orElseThrow(() -> new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT));

        if (!newProjectName.equals(persistedProject.getName())) {
            var projectNameAlreadyExists = projectRepository.existsByName(newProjectName);
            if (projectNameAlreadyExists) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_NAME, Entities.PROJECT, "name", persistedProject.getId());
            }
        }

        persistedProject.setName(newProjectName);
        persistedProject.setDescription(projectToUpdate.getDescription());
        persistedProject.setUpdateDate(ZonedDateTime.now());
        persistedProject.setUpdateUser(updateUser);
        return projectMapper.getProjectDTOFromProject(projectRepository.save(persistedProject));
    }

    /**
     * Tell if a project exists
     * @param projectCode the project code
     * @return true iff the project exists
     */
    public boolean exists(String projectCode) {
        return projectRepository.existsByCode(projectCode);
    }

    /**
     * Delete a project.
     * @param projectCode the project code
     * @throws ForbiddenException if this operation fails
     */
    @Transactional
    public void delete(String projectCode) throws ForbiddenException {
        projectRepository.deleteByCode(projectCode);
        userSessionService.refreshCurrentUserAuthorities();
    }

    /**
     * Given a project code, returns its ID if it exists, or throw a {@link NotFoundException}.
     *
     * @param code the code of the mandatory project
     * @return the ID of the found project
     * @throws NotFoundException when the project code does not exist
     */
    public long toId(String code) throws NotFoundException {
        final Project project = projectRepository.findOneByCode(code);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }
        return project.getId();
    }

    private void validateBusinessRules(ProjectDTO dto) throws NotUniqueException {
        validateUniqueCode(dto);
        validateUniqueName(dto);
    }

    private void validateUniqueCode(ProjectDTO dto) throws NotUniqueException {
        Project existingProjectWithSameCode = projectRepository.findOneByCode(dto.getCode());
        if (existingProjectWithSameCode != null && !existingProjectWithSameCode.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_CODE, Entities.PROJECT, "code", existingProjectWithSameCode.getId());
        }
    }

    private void validateUniqueName(ProjectDTO dto) throws NotUniqueException {
        Project existingProjectWithSameName = projectRepository.findOneByName(dto.getName());
        if (existingProjectWithSameName != null && !existingProjectWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_NAME, Entities.PROJECT, "name", existingProjectWithSameName.getId());
        }
    }

}
