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
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing CycleDefinition.
 */
@Service
@Transactional
public class CycleDefinitionService {

    private final CycleDefinitionRepository repository;

    private final GenericMapper mapper;

    private final ExecutionRepository executionRepository;

    public CycleDefinitionService(CycleDefinitionRepository repository, GenericMapper mapper,
            ExecutionRepository executionRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.executionRepository = executionRepository;
    }

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public CycleDefinitionDTO create(long projectId, CycleDefinitionDTO dtoToCreate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreate);
        final CycleDefinition entity = mapper.map(dtoToCreate, CycleDefinition.class);
        entity.setProjectId(projectId);
        final CycleDefinition savedEntity = repository.save(entity);
        updateBranchPositions(projectId, savedEntity.getBranch(), savedEntity.getBranchPosition());
        return mapper.map(savedEntity, CycleDefinitionDTO.class);
    }

    /**
     * Update an entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException  when the given entity ID is not present in database
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public CycleDefinitionDTO update(long projectId, CycleDefinitionDTO dtoToUpdate) throws BadRequestException {
        // Must update an existing entity
        CycleDefinition dataBaseEntity = repository.findAllByProjectIdAndId(projectId, dtoToUpdate.getId());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_CYCLE_DEFINITION, Entities.CYCLE_DEFINITION);
        }

        validateBusinessRules(projectId, dtoToUpdate);

        final CycleDefinition entity = mapper.map(dtoToUpdate, CycleDefinition.class);
        entity.setProjectId(projectId);
        final CycleDefinition savedEntity = repository.save(entity);
        updateBranchPositions(projectId, savedEntity.getBranch(), savedEntity.getBranchPosition());
        return mapper.map(savedEntity, CycleDefinitionDTO.class);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<CycleDefinitionDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByBranchPositionAscBranchAscNameAsc(projectId), CycleDefinitionDTO.class);
    }

    /**
     * Delete an entity by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException   if the entity id does not exist
     * @throws BadRequestException if the cycle definition or is used by an execution
     */
    public void delete(long projectId, long id) throws BadRequestException {
        CycleDefinition entity = repository.findAllByProjectIdAndId(projectId, id);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_CYCLE_DEFINITION, Entities.CYCLE_DEFINITION);
        }

        if (executionRepository.existsByCycleDefinitionId(entity.getId())) {
            throw new BadRequestException(Messages.RULE_CYCLE_DEFINITION_USED_BY_EXECUTION, Entities.CYCLE_DEFINITION, "used_by_execution");
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, CycleDefinitionDTO dto) throws NotUniqueException {
        Optional<CycleDefinition> existingEntityWithSameBranchAndName = repository.findByProjectIdAndBranchAndName(projectId, dto.getBranch(), dto.getName());
        if (existingEntityWithSameBranchAndName.isPresent() && !existingEntityWithSameBranchAndName.get().getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_CYCLE_DEFINITION_NAME_BRANCH, Entities.CYCLE_DEFINITION, "branch", existingEntityWithSameBranchAndName.get().getId());
        }
    }

    private void updateBranchPositions(long projectId, String branch, int branchPosition) {
        final List<CycleDefinition> cycleDefinitions = repository.findAllByProjectIdAndBranch(projectId, branch);
        for (CycleDefinition cycleDefinition : cycleDefinitions) {
            cycleDefinition.setBranchPosition(branchPosition);
        }
        repository.saveAll(cycleDefinitions);
    }

}
