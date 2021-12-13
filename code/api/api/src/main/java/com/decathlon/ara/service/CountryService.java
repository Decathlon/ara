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
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.repository.CountryDeploymentRepository;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.RunRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.support.Upsert;
import com.decathlon.ara.service.dto.support.UpsertResultDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing Country.
 */
@Service
@Transactional
public class CountryService {

    private final CountryRepository repository;

    private final CountryDeploymentRepository countryDeploymentRepository;

    private final RunRepository runRepository;

    private final ProblemPatternRepository problemPatternRepository;

    private final FunctionalityRepository functionalityRepository;

    private final ScenarioRepository scenarioRepository;

    private final GenericMapper mapper;

    public CountryService(CountryRepository repository, CountryDeploymentRepository countryDeploymentRepository,
            RunRepository runRepository, ProblemPatternRepository problemPatternRepository,
            FunctionalityRepository functionalityRepository, ScenarioRepository scenarioRepository,
            GenericMapper mapper) {
        this.repository = repository;
        this.countryDeploymentRepository = countryDeploymentRepository;
        this.runRepository = runRepository;
        this.problemPatternRepository = problemPatternRepository;
        this.functionalityRepository = functionalityRepository;
        this.scenarioRepository = scenarioRepository;
        this.mapper = mapper;
    }

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given code or name is already used by another entity
     * @throws NotFoundException  when the given project does not exist
     */
    public CountryDTO create(long projectId, CountryDTO dtoToCreate) throws BadRequestException {
        Country existingEntityWithSameCode = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (existingEntityWithSameCode != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_COUNTRY_CODE, Entities.COUNTRY,
                    "code", existingEntityWithSameCode.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Country entity = mapper.map(dtoToCreate, Country.class);
        entity.setProjectId(projectId);
        return mapper.map(repository.save(entity), CountryDTO.class);
    }

    /**
     * Create or update an entity.
     *
     * @param projectId           the ID of the project in which to work
     * @param dtoToCreateOrUpdate the entity to create or update
     * @return the created or updated entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public UpsertResultDTO<CountryDTO> createOrUpdate(long projectId, CountryDTO dtoToCreateOrUpdate) throws NotUniqueException {
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Country dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Country entity = mapper.map(dtoToCreateOrUpdate, Country.class);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        final CountryDTO dto = mapper.map(repository.save(entity), CountryDTO.class);
        return new UpsertResultDTO<>(dto, operation);
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities ordered by code
     */
    @Transactional(readOnly = true)
    public List<CountryDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByCode(projectId), CountryDTO.class);
    }

    /**
     * Delete an entity by code.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the entity
     * @throws NotFoundException   if the entity id does not exist
     * @throws BadRequestException if the country is used by some other tables
     */
    public void delete(long projectId, String code) throws BadRequestException {
        Country entity = repository.findByProjectIdAndCode(projectId, code);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_COUNTRY, Entities.COUNTRY);
        }

        checkNotUsed(entity);

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, CountryDTO dto) throws NotUniqueException {
        Country existingEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_COUNTRY_NAME, Entities.COUNTRY, "name", existingEntityWithSameName.getCode());
        }
    }

    private void checkNotUsed(Country country) throws BadRequestException {
        final long countryId = country.getId().longValue();

        if (countryDeploymentRepository.existsByCountryId(countryId)) {
            throw new BadRequestException(Messages.RULE_COUNTRY_USED_BY_COUNTRY_DEPLOYMENT, Entities.COUNTRY, "used_by_country_deployment");
        }

        if (runRepository.existsByCountryId(countryId)) {
            throw new BadRequestException(Messages.RULE_COUNTRY_USED_BY_RUN, Entities.COUNTRY, "used_by_run");
        }

        if (problemPatternRepository.existsByCountryId(countryId)) {
            throw new BadRequestException(Messages.RULE_COUNTRY_USED_BY_PROBLEM_PATTERN, Entities.COUNTRY, "used_by_problem_pattern");
        }

        if (functionalityRepository.existsByProjectIdAndCountryCode(country.getProjectId(), country.getCode())) {
            throw new BadRequestException(Messages.RULE_COUNTRY_USED_BY_FUNCTIONALITY, Entities.COUNTRY, "used_by_functionality");
        }

        if (scenarioRepository.existsByProjectIdAndCountryCode(country.getProjectId(), country.getCode())) {
            throw new BadRequestException(Messages.RULE_COUNTRY_USED_BY_SCENARIO, Entities.COUNTRY, "used_by_scenario");
        }
    }

}
