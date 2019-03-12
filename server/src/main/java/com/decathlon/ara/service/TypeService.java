package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.QType;
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
import com.decathlon.ara.service.mapper.TypeWithSourceCodeMapper;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Type.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TypeService {

    @NonNull
    private final TypeRepository repository;

    @NonNull
    private final SourceRepository sourceRepository;

    @NonNull
    private final TypeWithSourceCodeMapper mapper;

    @NonNull
    private final ProblemPatternRepository problemPatternRepository;

    @NonNull
    private final RunRepository runRepository;

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
        ObjectUtil.trimStringValues(dtoToCreate);

        Type existingEntityWithSameCode = repository.findByProjectIdAndCode(projectId, dtoToCreate.getCode());
        if (existingEntityWithSameCode != null) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_TYPE_CODE, Entities.TYPE,
                    QType.type.code.getMetadata().getName(), existingEntityWithSameCode.getCode());
        }

        validateBusinessRules(projectId, dtoToCreate);

        final Type entity = mapper.toEntity(dtoToCreate);
        entity.setProjectId(projectId);
        assignExistingSource(projectId, entity);
        return mapper.toDto(repository.save(entity));
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
        ObjectUtil.trimStringValues(dtoToCreateOrUpdate);
        validateBusinessRules(projectId, dtoToCreateOrUpdate);

        Type dataBaseEntity = repository.findByProjectIdAndCode(projectId, dtoToCreateOrUpdate.getCode());
        final Upsert operation = (dataBaseEntity == null ? Upsert.INSERT : Upsert.UPDATE);

        final Type entity = mapper.toEntity(dtoToCreateOrUpdate);
        entity.setId(dataBaseEntity == null ? null : dataBaseEntity.getId());
        entity.setProjectId(projectId);
        assignExistingSource(projectId, entity);
        final TypeWithSourceCodeDTO dto = mapper.toDto(repository.save(entity));
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
        return mapper.toDto(repository.findAllByProjectIdOrderByCode(projectId));
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
            throw new NotUniqueException(Messages.NOT_UNIQUE_TYPE_NAME, Entities.TYPE, QType.type.name.getMetadata().getName(), dataBaseEntityWithSameName.getCode());
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
