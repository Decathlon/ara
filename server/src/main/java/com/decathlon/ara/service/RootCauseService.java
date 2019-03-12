package com.decathlon.ara.service;

import com.decathlon.ara.domain.QRootCause;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.RootCauseMapper;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing RootCause.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RootCauseService {

    @NonNull
    private final RootCauseRepository repository;

    @NonNull
    private final RootCauseMapper mapper;

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public RootCauseDTO create(long projectId, RootCauseDTO dtoToCreate) throws NotUniqueException {
        ObjectUtil.trimStringValues(dtoToCreate);
        validateBusinessRules(projectId, dtoToCreate);
        final RootCause entity = mapper.toEntity(dtoToCreate);
        entity.setProjectId(projectId);
        return mapper.toDto(repository.save(entity));
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
        ObjectUtil.trimStringValues(dtoToUpdate);

        // Must update an existing entity
        RootCause dataBaseEntity = repository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ROOT_CAUSE, Entities.ROOT_CAUSE);
        }

        validateBusinessRules(projectId, dtoToUpdate);

        final RootCause entity = mapper.toEntity(dtoToUpdate);
        entity.setProjectId(projectId);
        return mapper.toDto(repository.save(entity));
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RootCauseDTO> findAll(long projectId) {
        return mapper.toDto(repository.findAllByProjectIdOrderByName(projectId));
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
        return mapper.toDto(rootCause);
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
            throw new NotUniqueException(Messages.NOT_UNIQUE_ROOT_CAUSE_NAME, Entities.ROOT_CAUSE, QRootCause.rootCause.name.getMetadata().getName(), existingEntityWithSameName.getId());
        }
    }

}
