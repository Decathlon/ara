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
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Source.
 */
@Service
@Transactional
public class SourceService {

    private final SourceRepository repository;

    private final GenericMapper mapper;

    private final TypeRepository typeRepository;

    private final ScenarioRepository scenarioRepository;

    public SourceService(SourceRepository repository, GenericMapper mapper, TypeRepository typeRepository,
            ScenarioRepository scenarioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.typeRepository = typeRepository;
        this.scenarioRepository = scenarioRepository;
    }

    /**
     * Create a new entity.
     *
     * @param projectId the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public SourceDTO create(long projectId, SourceDTO dtoToCreate) throws NotUniqueException {
        Source dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (dataBaseEntity != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_CODE, Entities.SOURCE, "code", dataBaseEntity.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Source source = mapper.map(dtoToCreate, Source.class);
        source.setProjectId(projectId);
        return mapper.map(repository.save(source), SourceDTO.class);
    }

    /**
     * Create or update an entity.
     *
     * @param projectId the ID of the project in which to work
     * @param dtoToCreateOrUpdate the entity to create or update
     * @return the created or updated entity
     * @throws NotUniqueException when the given code is already used
     */
    public UpsertResultDTO<SourceDTO> createOrUpdate(long projectId, SourceDTO dtoToCreateOrUpdate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Source dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Source entity = mapper.map(dtoToCreateOrUpdate, Source.class);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        final SourceDTO dto = mapper.map(repository.save(entity), SourceDTO.class);
        return new UpsertResultDTO<>(dto, operation);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities ordered by name
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByName(projectId), SourceDTO.class);
    }

    /**
     * Delete an entity by code.
     *
     * @param projectId the ID of the project in which to work
     * @param code the code of the entity
     * @throws BadRequestException if the entity id does not exist
     */
    public void delete(long projectId, String code) throws BadRequestException {
        Source entity = repository.findByProjectIdAndCode(projectId, code);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_SOURCE, Entities.SOURCE);
        }

        if (scenarioRepository.existsBySourceId(entity.getId())) {
            throw new BadRequestException(Messages.RULE_SOURCE_USED_BY_SCENARIO, Entities.SOURCE, "used_by_scenario");
        }
        if (typeRepository.existsByProjectIdAndSourceId(projectId, entity.getId().longValue())) {
            throw new BadRequestException(Messages.RULE_SOURCE_USED_BY_TYPE, Entities.SOURCE, "used_by_type");
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, SourceDTO dto) throws NotUniqueException {
        Source entityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (entityWithSameName != null && !entityWithSameName.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_NAME, Entities.SOURCE, "name", entityWithSameName.getCode());
        }

        Source entityWithSameLetter = repository.findByProjectIdAndLetter(projectId, dto.getLetter().charAt(0));
        if (entityWithSameLetter != null && !entityWithSameLetter.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_LETTER, Entities.SOURCE, "letter", entityWithSameLetter.getCode());
        }
    }
}
