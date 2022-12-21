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
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.security.dto.user.UserAccountProfile;
import com.decathlon.ara.security.service.AuthorityService;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Service for managing Project.
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repository;

    private final RootCauseRepository rootCauseRepository;

    private final GenericMapper mapper;

    private final CommunicationService communicationService;

    private final AuthorityService authorityService;

    public ProjectService(
            ProjectRepository repository,
            RootCauseRepository rootCauseRepository,
            GenericMapper mapper,
            CommunicationService communicationService,
            AuthorityService authorityService
    ) {
        this.repository = repository;
        this.rootCauseRepository = rootCauseRepository;
        this.mapper = mapper;
        this.communicationService = communicationService;
        this.authorityService = authorityService;
    }

    /**
     * Create a new entity.
     *
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given code or name is already used by another entity
     */
    public ProjectDTO create(ProjectDTO dtoToCreate) throws NotUniqueException {
        validateBusinessRules(dtoToCreate);
        final Project entity = mapper.map(dtoToCreate, Project.class);
        communicationService.initializeProject(entity);
        final ProjectDTO createdProject = mapper.map(repository.save(entity), ProjectDTO.class);

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
    public ProjectDTO createFromCode(String projectCode) throws NotUniqueException {
        var projectName =  getProjectNameFromCode(projectCode);
        return create(new ProjectDTO(null, projectCode, projectName, false));
    }

    private static String getProjectNameFromCode(String projectCode) {
        UnaryOperator<String> capitalizeFirstLetter = string -> Character.toUpperCase(string.charAt(0)) + string.substring(1);
        var projectNameFromCode = Arrays.stream(projectCode.split("[^a-zA-Z0-9]+"))
                .map(capitalizeFirstLetter)
                .collect(Collectors.joining(" "));
        return String.format("%s (generated)", projectNameFromCode);
    }

    /**
     * Update an entity.
     *
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException  when the given entity ID is not present in database
     * @throws NotUniqueException when the given code or name is already used by another entity
     */
    public ProjectDTO update(ProjectDTO dtoToUpdate) throws BadRequestException {
        // Must update an existing entity
        Optional<Project> dataBaseEntity = repository.findById(dtoToUpdate.getId());
        if (!dataBaseEntity.isPresent()) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }

        validateBusinessRules(dtoToUpdate);

        final Project entity = mapper.map(dtoToUpdate, Project.class);
        entity.setCommunications(dataBaseEntity.get().getCommunications());
        return mapper.map(repository.save(entity), ProjectDTO.class);
    }

    /**
     * Get all the projects depending on the (logged in) user profile.
     *
     * @return the list of the projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        var profile = authorityService.getProfile();
        if (profile.isEmpty()) {
            return new ArrayList<>();
        }
        var superAdmin = UserAccountProfile.SUPER_ADMIN;
        var auditor = UserAccountProfile.AUDITOR;
        var userHasFullAccessToProjects = superAdmin.equals(profile.get()) || auditor.equals(profile.get());
        if (userHasFullAccessToProjects) {
            return mapper.mapCollection(repository.findAllByOrderByName(), ProjectDTO.class);
        }
        var scopedUser = UserAccountProfile.SCOPED_USER;
        var userHasLimitedAccessToProjects = scopedUser.equals(profile.get());
        if (userHasLimitedAccessToProjects) {
            var scopedProjectCodes = authorityService.getScopedProjectCodes();
            return mapper.mapCollection(repository.findByCodeInOrderByName(scopedProjectCodes), ProjectDTO.class);
        }

        return new ArrayList<>();
    }

    /**
     * Tell if a project exists
     * @param projectCode the project code
     * @return true iff the project exists
     */
    public boolean exists(String projectCode) {
        return repository.existsByCode(projectCode);
    }

    /**
     * Delete a project.
     * @param projectCode the project code
     * @throws ForbiddenException if this operation fails
     */
    @Transactional
    public void delete(String projectCode) throws ForbiddenException {
        repository.deleteByCode(projectCode);
        authorityService.refreshCurrentUserAccountAuthorities();
    }

    /**
     * Given a project code, returns its ID if it exists, or throw a {@link NotFoundException}.
     *
     * @param code the code of the mandatory project
     * @return the ID of the found project
     * @throws NotFoundException when the project code does not exist
     */
    public long toId(String code) throws NotFoundException {
        final Project project = repository.findOneByCode(code);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }
        return project.getId();
    }

    private void validateBusinessRules(ProjectDTO dto) throws NotUniqueException {
        validateUniqueCode(dto);
        validateUniqueName(dto);
        switchProjectAsDefault(dto);
    }

    private void validateUniqueCode(ProjectDTO dto) throws NotUniqueException {
        Project existingEntityWithSameCode = repository.findOneByCode(dto.getCode());
        if (existingEntityWithSameCode != null && !existingEntityWithSameCode.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_CODE, Entities.PROJECT, "code", existingEntityWithSameCode.getId());
        }
    }

    private void validateUniqueName(ProjectDTO dto) throws NotUniqueException {
        Project existingEntityWithSameName = repository.findOneByName(dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_NAME, Entities.PROJECT, "name", existingEntityWithSameName.getId());
        }
    }

    private void switchProjectAsDefault(ProjectDTO dto) {
        if (dto.isDefaultAtStartup()) {
            Project entityDataBaseWithDefaultAtStartup = repository.findByDefaultAtStartup(true);
            if (entityDataBaseWithDefaultAtStartup != null && !entityDataBaseWithDefaultAtStartup.getCode().equals(dto.getCode())) {
                entityDataBaseWithDefaultAtStartup.setDefaultAtStartup(false);
            }
        }
    }

}
