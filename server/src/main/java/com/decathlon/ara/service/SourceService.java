package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.QSource;
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
import com.decathlon.ara.service.mapper.SourceMapper;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Source.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SourceService {

    @NonNull
    private final SourceRepository repository;

    @NonNull
    private final SourceMapper mapper;

    @NonNull
    private final TypeRepository typeRepository;

    @NonNull
    private final ScenarioRepository scenarioRepository;

    /**
     * Create a new entity.
     *
     * @param projectId the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public SourceDTO create(long projectId, SourceDTO dtoToCreate) throws NotUniqueException {
        ObjectUtil.trimStringValues(dtoToCreate);

        Source dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (dataBaseEntity != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_CODE, Entities.SOURCE, QSource.source.code.getMetadata().getName(), dataBaseEntity.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Source source = mapper.toEntity(dtoToCreate);
        source.setProjectId(projectId);
        return mapper.toDto(repository.save(source));
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
        ObjectUtil.trimStringValues(dtoToCreateOrUpdate);
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Source dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Source entity = mapper.toEntity(dtoToCreateOrUpdate);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        final SourceDTO dto = mapper.toDto(repository.save(entity));
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
        return mapper.toDto(repository.findAllByProjectIdOrderByName(projectId));
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
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_NAME, Entities.SOURCE, QSource.source.name.getMetadata().getName(), entityWithSameName.getCode());
        }

        Source entityWithSameLetter = repository.findByProjectIdAndLetter(projectId, dto.getLetter().charAt(0));
        if (entityWithSameLetter != null && !entityWithSameLetter.getCode().equals(dto.getCode())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_SOURCE_LETTER, Entities.SOURCE, QSource.source.letter.getMetadata().getName(), entityWithSameLetter.getCode());
        }
    }
}
