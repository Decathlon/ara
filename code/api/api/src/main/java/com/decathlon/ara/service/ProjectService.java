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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.cache.CacheService;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Project.
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repository;

    private final RootCauseRepository rootCauseRepository;

    private final UserRepository userRepository;

    private final ProjectUserMemberRepository projectUserMemberRepository;

    private final ProjectGroupMemberRepository projectGroupMemberRepository;

    private final GenericMapper mapper;

    private final CommunicationService communicationService;

    private final UserPreferenceService userPreferenceService;

    private final CacheService cacheService;

    @Autowired
    public ProjectService(ProjectRepository repository, RootCauseRepository rootCauseRepository, UserRepository userRepository, ProjectUserMemberRepository projectUserMemberRepository, ProjectGroupMemberRepository projectGroupMemberRepository, GenericMapper mapper, CommunicationService communicationService, UserPreferenceService userPreferenceService, CacheService cacheService) {
        this.repository = repository;
        this.rootCauseRepository = rootCauseRepository;
        this.userRepository = userRepository;
        this.projectUserMemberRepository = projectUserMemberRepository;
        this.projectGroupMemberRepository = projectGroupMemberRepository;
        this.mapper = mapper;
        this.communicationService = communicationService;
        this.userPreferenceService = userPreferenceService;
        this.cacheService = cacheService;
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
        Project createdProject = repository.save(entity);
        final ProjectDTO createdProjectDTO = mapper.map(createdProject, ProjectDTO.class);

        final long projectId = createdProjectDTO.getId().longValue();
        rootCauseRepository.saveAll(Arrays.asList(
                new RootCause(projectId, "Fragile test"),
                new RootCause(projectId, "Network issue"),
                new RootCause(projectId, "Regression"),
                new RootCause(projectId, "Test to update")));

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        ProjectUserMember projectUserMember = new ProjectUserMember(entity, userRepository.findByMemberName(userName));
        projectUserMember.setRole(MemberRole.ADMIN);
        projectUserMemberRepository.save(projectUserMember);
        updateDefaultProject(dtoToCreate);
        cacheService.evictCaches(createdProject, userName);

        return createdProjectDTO;
    }

    /**
     * Update an entity.
     *
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException  when the given entity ID is not present in database
     * @throws NotUniqueException when the given code or name is already used by another entity
     */
    public ProjectDTO update(String projectCode, ProjectDTO dtoToUpdate) throws BadRequestException {
        // Must update an existing entity
        Project dataBaseEntity = repository.findOneByCode(projectCode);
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }
        dtoToUpdate.setId(dataBaseEntity.getId());

        validateBusinessRules(dtoToUpdate);

        final Project entity = mapper.map(dtoToUpdate, Project.class);
        entity.setCommunications(dataBaseEntity.getCommunications());
        ProjectDTO updatedProject = mapper.map(repository.save(entity), ProjectDTO.class);
        if (updateDefaultProject(dtoToUpdate)) {
            cacheService.evictsUserProjectsCache(SecurityContextHolder.getContext().getAuthentication().getName());
        }
        return updatedProject;
    }

    /**
     * Get all the entities.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        return mapper.mapCollection(repository.findAllByOrderByName(), ProjectDTO.class);
    }

    /**
     * Get all the entities.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "security.user.projects", key = "#userId")
    public List<ProjectDTO> findAll(String userId) {
        Set<Project> projects = new TreeSet<>(Comparator.comparing(Project::getName));
        projects.addAll(projectUserMemberRepository.findAllProjectByUserName(userId));
        projects.addAll(projectGroupMemberRepository.findAllProjectByUserName(userId));
        String defaultProjectCode = userPreferenceService.getValue(UserPreferenceService.DEFAULT_PROJECT);
        List<ProjectDTO> projectDTOList = mapper.mapCollection(projects, ProjectDTO.class);
        if (defaultProjectCode != null) {
            Optional<ProjectDTO> findFirst = projectDTOList.stream().filter(project -> defaultProjectCode.equals(project.getCode())).findFirst();
            if (findFirst.isPresent()) {
                findFirst.get().setDefaultAtStartup(true);
            }
        }
        return projectDTOList;
    }

    /**
     * Get one project by code.
     *
     * @param code the code of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(String code) {
        return Optional.ofNullable(repository.findOneByCode(code)).map(project -> mapper.map(project, ProjectDTO.class));
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
        return project.getId().longValue();

    }

    private void validateBusinessRules(ProjectDTO dto) throws NotUniqueException {
        validateUniqueCode(dto);
        validateUniqueName(dto);
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

    private boolean updateDefaultProject(ProjectDTO dto) {
        if (dto.isDefaultAtStartup()) {
            userPreferenceService.setValue(UserPreferenceService.DEFAULT_PROJECT, dto.getCode());
            return true;
        } else if (dto.getCode().equals(userPreferenceService.getValue(UserPreferenceService.DEFAULT_PROJECT))) {
            userPreferenceService.setValue(UserPreferenceService.DEFAULT_PROJECT, null);
            return true;
        }
        return false;
    }

    public void delete(String code) throws NotFoundException {
        final Project project = repository.findOneByCode(code);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }
        projectGroupMemberRepository.deleteByProjectCode(code);
        projectUserMemberRepository.deleteByProjectCode(code);
        repository.delete(project);
        cacheService.evictCaches(project);
    }

}
