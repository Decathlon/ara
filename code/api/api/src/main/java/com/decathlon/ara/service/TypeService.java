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
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.RunRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceCodeDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Type.
 */
@Service
@Transactional
public class TypeService {

    private final TypeRepository repository;

    private final SourceRepository sourceRepository;

    private final GenericMapper mapper;

    private final ProblemPatternRepository problemPatternRepository;

    private final RunRepository runRepository;

    public TypeService(TypeRepository repository, SourceRepository sourceRepository, GenericMapper mapper,
            ProblemPatternRepository problemPatternRepository, RunRepository runRepository) {
        this.repository = repository;
        this.sourceRepository = sourceRepository;
        this.mapper = mapper;
        this.problemPatternRepository = problemPatternRepository;
        this.runRepository = runRepository;
    }

    private void mapSourceCode(Type entity, TypeWithSourceCodeDTO dto) {
        if (entity.getSource() != null) {
            dto.setSourceCode(entity.getSource().getCode());
        }
    }

    private void mapSourceCode(TypeWithSourceCodeDTO dto, Type entity) {
        if (dto.getSourceCode() != null) {
            Source source = new Source();
            source.setCode(dto.getSourceCode());
            entity.setSource(source);
        }
    }

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given code or name is already used by another entity
     * @throws NotFoundException  when the given type has a source with a code not existing on the project
     */
    public TypeWithSourceCodeDTO create(long projectId, TypeWithSourceCodeDTO dtoToCreate) throws BadRequestException {
        Type existingEntityWithSameCode = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (existingEntityWithSameCode != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_TYPE_CODE, Entities.TYPE,
                    "code", existingEntityWithSameCode.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Type entity = mapper.map(dtoToCreate, Type.class, this::mapSourceCode);
        entity.setProjectId(projectId);
        assignExistingSource(projectId, entity);
        return mapper.map(repository.save(entity), TypeWithSourceCodeDTO.class, this::mapSourceCode);
    }

    /**
     * Create or update an entity.
     *
     * @param projectId           the ID of the project in which to work
     * @param dtoToCreateOrUpdate the entity to create or update
     * @return the created or updated entity
     * @throws NotUniqueException when the given name is already used by another entity
     * @throws NotFoundException  when the given type has a source with a code not existing on the project
     */
    public UpsertResultDTO<TypeWithSourceCodeDTO> createOrUpdate(long projectId, TypeWithSourceCodeDTO dtoToCreateOrUpdate) throws BadRequestException {
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Type dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Type entity = mapper.map(dtoToCreateOrUpdate, Type.class, this::mapSourceCode);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        assignExistingSource(projectId, entity);
        final TypeWithSourceCodeDTO dto = mapper.map(repository.save(entity), TypeWithSourceCodeDTO.class, this::mapSourceCode);
        return new UpsertResultDTO<>(dto, operation);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities ordered by code
     */
    @Transactional(readOnly = true)
    public List<TypeWithSourceCodeDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByCode(projectId), TypeWithSourceCodeDTO.class, this::mapSourceCode);
    }

    /**
     * Delete an entity by code.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the entity
     * @throws NotFoundException   if the entity id does not exist
     * @throws BadRequestException if the type is used by some other tables
     */
    public void delete(long projectId, String code) throws BadRequestException {
        Type entity = repository.findByProjectIdAndCode(projectId, code);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TYPE, Entities.TYPE);
        }

        checkNotUsed(entity.getId().longValue());

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, TypeWithSourceCodeDTO dto) throws NotUniqueException {
        Type dataBaseEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (dataBaseEntityWithSameName != null && !dataBaseEntityWithSameName.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_TYPE_NAME, Entities.TYPE, "name", dataBaseEntityWithSameName.getCode());
        }
    }

    private void checkNotUsed(long typeId) throws BadRequestException {
        if (problemPatternRepository.existsByTypeId(typeId)) {
            throw new BadRequestException(Messages.RULE_TYPE_USED_BY_PROBLEM_PATTERN, Entities.TYPE, "used_by_problem_pattern");
        }

        if (runRepository.existsByTypeId(typeId)) {
            throw new BadRequestException(Messages.RULE_TYPE_USED_BY_RUN, Entities.TYPE, "used_by_run");
        }
    }

    /**
     * Assign the Source entity to this type, if the type' source's code is provided.
     *
     * @param projectId the ID of the project in which to work
     * @param type      the type on which we want to check source existence
     * @throws NotFoundException if the (optional) source is provided (not null) but the code does not exist in the project
     */
    private void assignExistingSource(long projectId, Type type) throws NotFoundException {
        if (type.getSource() != null) {
            // User provided a code, but we need to know if it is legit in the project context
            final Source source = sourceRepository.findByProjectIdAndCode(projectId, type.getSource().getCode());
            if (source == null) {
                throw new NotFoundException(Messages.NOT_FOUND_SOURCE, Entities.SOURCE);
            }
            type.setSource(source);
        }
    }

}
