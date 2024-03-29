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

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Team.
 */
@Service
@Transactional
public class TeamService {

    private final TeamRepository repository;

    private final GenericMapper mapper;

    private final FunctionalityRepository functionalityRepository;

    private final ProblemRepository problemRepository;

    public TeamService(TeamRepository repository, GenericMapper mapper, FunctionalityRepository functionalityRepository,
            ProblemRepository problemRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.functionalityRepository = functionalityRepository;
        this.problemRepository = problemRepository;
    }

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public TeamDTO create(long projectId, TeamDTO dtoToCreate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreate);
        final Team entity = mapper.map(dtoToCreate, Team.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), TeamDTO.class);
    }

    /**
     * Update an entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException   when the given entity ID is not present in database
     * @throws NotUniqueException  when the given name is already used by another entity
     * @throws BadRequestException when trying to forbid assignation to an entity type still having assignations
     */
    public TeamDTO update(long projectId, TeamDTO dtoToUpdate) throws BadRequestException {
        // Must update an existing entity
        Team dataBaseEntity = repository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }

        validateBusinessRules(projectId, dtoToUpdate);

        if (!dtoToUpdate.isAssignableToFunctionalities() && functionalityRepository.existsByProjectIdAndTeamId(projectId, dataBaseEntity.getId().longValue())) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_ASSIGNED_FUNCTIONALITIES, Entities.TEAM, "has_assigned_functionalities");
        }

        if (!dtoToUpdate.isAssignableToProblems() && problemRepository.existsByProjectIdAndBlamedTeam(projectId, dataBaseEntity)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_ASSIGNED_PROBLEMS, Entities.TEAM, "has_assigned_problems");
        }

        final Team entity = mapper.map(dtoToUpdate, Team.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), TeamDTO.class);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<TeamDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByName(projectId), TeamDTO.class);
    }

    /**
     * Get one entity by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the entity
     * @throws NotFoundException when the entity does not exist on the project
     */
    @Transactional(readOnly = true)
    public TeamDTO findOne(long projectId, long id) throws NotFoundException {
        final Team team = repository.findByProjectIdAndId(projectId, id);
        if (team == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }
        return mapper.map(team, TeamDTO.class);
    }

    /**
     * Delete an entity by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException   if the entity id does not exist
     * @throws BadRequestException if the team has associated functionalities or problems
     */
    public void delete(long projectId, long id) throws BadRequestException {
        Team entity = repository.findByProjectIdAndId(projectId, id);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }

        if (functionalityRepository.existsByProjectIdAndTeamId(projectId, id)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_FUNCTIONALITIES, Entities.TEAM, "has_functionalities");
        }

        if (problemRepository.existsByProjectIdAndBlamedTeam(projectId, entity)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_PROBLEMS, Entities.TEAM, "has_problems");
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, TeamDTO dto) throws NotUniqueException {
        Team existingEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_TEAM_NAME, Entities.TEAM, "name", existingEntityWithSameName.getId());
        }
    }

}
