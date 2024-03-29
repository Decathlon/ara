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
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing RootCause.
 */
@Service
@Transactional
public class RootCauseService {

    private final RootCauseRepository repository;

    private final GenericMapper mapper;

    public RootCauseService(RootCauseRepository repository, GenericMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public RootCauseDTO create(long projectId, RootCauseDTO dtoToCreate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreate);
        final RootCause entity = mapper.map(dtoToCreate, RootCause.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), RootCauseDTO.class);
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
    public RootCauseDTO update(long projectId, RootCauseDTO dtoToUpdate) throws BadRequestException {
        // Must update an existing entity
        RootCause dataBaseEntity = repository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ROOT_CAUSE, Entities.ROOT_CAUSE);
        }

        validateBusinessRules(projectId, dtoToUpdate);

        final RootCause entity = mapper.map(dtoToUpdate, RootCause.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), RootCauseDTO.class);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RootCauseDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByName(projectId), RootCauseDTO.class);
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
    public RootCauseDTO findOne(long projectId, long id) throws NotFoundException {
        final RootCause rootCause = repository.findByProjectIdAndId(projectId, id);
        if (rootCause == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ROOT_CAUSE, Entities.ROOT_CAUSE);
        }
        return mapper.map(rootCause, RootCauseDTO.class);
    }

    /**
     * Delete an entity by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException if the entity id does not exist
     */
    public void delete(long projectId, long id) throws NotFoundException {
        RootCause entity = repository.findByProjectIdAndId(projectId, id);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ROOT_CAUSE, Entities.ROOT_CAUSE);
        }

        // The database will do an ON DELETE SET NULL on this entity
        // But we first need to set null the children entities that are already in second-level cache to keep the cache up to date
        // We do not invalidate the cache manually: another thread could repopulate it between us evicting it and committing transaction
        for (Problem problem : entity.getProblems()) {
            problem.setRootCause(null);
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, RootCauseDTO dto) throws NotUniqueException {
        RootCause existingEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_ROOT_CAUSE_NAME, Entities.ROOT_CAUSE, "name", existingEntityWithSameName.getId());
        }
    }

}
