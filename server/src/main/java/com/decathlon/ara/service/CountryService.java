package com.decathlon.ara.service;

import com.decathlon.ara.domain.QCountry;
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
import com.decathlon.ara.service.mapper.CountryMapper;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Country.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CountryService {

    @NonNull
    private final CountryRepository repository;

    @NonNull
    private final CountryDeploymentRepository countryDeploymentRepository;

    @NonNull
    private final RunRepository runRepository;

    @NonNull
    private final ProblemPatternRepository problemPatternRepository;

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final ScenarioRepository scenarioRepository;

    @NonNull
    private final CountryMapper mapper;

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
        ObjectUtil.trimStringValues(dtoToCreate);

        Country existingEntityWithSameCode = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (existingEntityWithSameCode != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_COUNTRY_CODE, Entities.COUNTRY,
                    QCountry.country.code.getMetadata().getName(), existingEntityWithSameCode.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Country entity = mapper.toEntity(dtoToCreate);
        entity.setProjectId(projectId);
        return mapper.toDto(repository.save(entity));
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
        ObjectUtil.trimStringValues(dtoToCreateOrUpdate);
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Country dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Country entity = mapper.toEntity(dtoToCreateOrUpdate);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        final CountryDTO dto = mapper.toDto(repository.save(entity));
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
        return mapper.toDto(repository.findAllByProjectIdOrderByCode(projectId));
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
            throw new NotUniqueException(Messages.NOT_UNIQUE_COUNTRY_NAME, Entities.COUNTRY, QCountry.country.name.getMetadata().getName(), existingEntityWithSameName.getCode());
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
