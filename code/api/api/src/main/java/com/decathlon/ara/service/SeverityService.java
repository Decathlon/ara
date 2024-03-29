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
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Severity.
 */
@Service
@Transactional
public class SeverityService {

    private final SeverityRepository repository;

    private final GenericMapper mapper;

    public SeverityService(SeverityRepository repository, GenericMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return all severities ordered by position, and with the special ALL at the end
     */
    public List<SeverityDTO> getSeveritiesWithAll(long projectId) {
        final List<Severity> severities = repository.findAllByProjectIdOrderByPosition(projectId);
        severities.add(Severity.ALL);
        return mapper.mapCollection(severities, SeverityDTO.class);
    }

    /**
     * @param severities given this list of severities
     * @return the code of the default severity, or null if none of them is default
     */
    public String getDefaultSeverityCode(List<SeverityDTO> severities) {
        return severities.stream()
                .filter(SeverityDTO::isDefaultOnMissing)
                .findFirst()
                .map(SeverityDTO::getCode)
                // Launched with explicit severity tags, and the default one was excluded: scenarios with no severity would not run neither
                // WARNING: when running only non-blocking runs, no severity is mandatory, so we find no default in them
                .orElse(null);
    }

    /**
     * Create a new entity.
     *
     * @param projectId the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given code, name, short name or initials is already used by another entity
     */
    public SeverityDTO create(long projectId, SeverityDTO dtoToCreate) throws NotUniqueException {
        Severity existingEntityWithSameCode = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (existingEntityWithSameCode != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_CODE, Entities.SEVERITY, "code", existingEntityWithSameCode.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Severity entity = mapper.map(dtoToCreate, Severity.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), SeverityDTO.class);
    }

    /**
     * Create or update an entity.
     *
     * @param projectId the ID of the project in which to work
     * @param dtoToCreateOrUpdate the entity to create or update
     * @return the created or updated entity
     * @throws NotUniqueException when the given name, short name or initials is already used
     */
    public UpsertResultDTO<SeverityDTO> createOrUpdate(long projectId, SeverityDTO dtoToCreateOrUpdate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Severity dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Severity entity = mapper.map(dtoToCreateOrUpdate, Severity.class);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        final SeverityDTO dto = mapper.map(repository.save(entity), SeverityDTO.class);
        return new UpsertResultDTO<>(dto, operation);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return all severities ordered by position
     */
    @Transactional(readOnly = true)
    public List<SeverityDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByPosition(projectId), SeverityDTO.class);
    }

    /**
     * Delete an entity by code.
     *
     * @param projectId the ID of the project in which to work
     * @param code the code of the entity
     * @throws NotFoundException if the entity code does not exist
     */
    public void delete(long projectId, String code) throws BadRequestException {
        Severity entity = repository.findByProjectIdAndCode(projectId, code);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_CYCLE_DEFINITION, Entities.SEVERITY);
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, SeverityDTO dto) throws NotUniqueException {
        validateUniqueName(projectId, dto);
        validateUniqueShortName(projectId, dto);
        validateUniqueInitials(projectId, dto);
        validateUniquePosition(projectId, dto);
        validateOnlyOneDefault(projectId, dto);
    }

    private void validateUniqueName(long projectId, SeverityDTO dto) throws NotUniqueException {
        Severity databaseEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (databaseEntityWithSameName != null && !databaseEntityWithSameName.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_NAME, Entities.SEVERITY, "name", databaseEntityWithSameName.getCode());
        }
    }

    private void validateUniqueShortName(long projectId, SeverityDTO dto) throws NotUniqueException {
        Severity entityWithSameShortName = repository.findByProjectIdAndShortName(projectId, dto.getShortName());
        if (entityWithSameShortName != null && !entityWithSameShortName.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_SHORT_NAME, Entities.SEVERITY, "shortName", entityWithSameShortName.getCode());
        }
    }

    private void validateUniqueInitials(long projectId, SeverityDTO dto) throws NotUniqueException {
        Severity entityWithSameInitials = repository.findByProjectIdAndInitials(projectId, dto.getInitials());
        if (entityWithSameInitials != null && !entityWithSameInitials.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_INITIALS, Entities.SEVERITY, "initials", entityWithSameInitials.getCode());
        }
    }

    private void validateUniquePosition(long projectId, SeverityDTO dto) throws NotUniqueException {
        Severity entityDataBaseWithSamePosition = repository.findByProjectIdAndPosition(projectId, dto.getPosition().intValue());
        if (entityDataBaseWithSamePosition != null && !entityDataBaseWithSamePosition.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_POSITION, Entities.SEVERITY, "position", entityDataBaseWithSamePosition.getCode());
        }
    }

    private void validateOnlyOneDefault(long projectId, SeverityDTO dto) throws NotUniqueException {
        if (dto.isDefaultOnMissing()) {
            Severity entityDataBaseWithDefaultOnMissing = repository.findByProjectIdAndDefaultOnMissing(projectId, true);
            if (entityDataBaseWithDefaultOnMissing != null && !entityDataBaseWithDefaultOnMissing.getCode().equals(dto.getCode())) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_SEVERITY_DEFAULT_ON_MISSION, Entities.SEVERITY, "defaultOnMissing", entityDataBaseWithDefaultOnMissing.getCode());
            }
        }
    }

}
