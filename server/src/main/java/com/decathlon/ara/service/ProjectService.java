package com.decathlon.ara.service;

import com.decathlon.ara.domain.QProject;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.mapper.ProjectMapper;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Project.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectService {

    @NonNull
    private final ProjectRepository repository;

    @NonNull
    private final RootCauseRepository rootCauseRepository;

    @NonNull
    private final ProjectMapper mapper;

    @NonNull
    private final CommunicationService communicationService;

    /**
     * Create a new entity.
     *
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given code or name is already used by another entity
     */
    public ProjectDTO create(ProjectDTO dtoToCreate) throws NotUniqueException {
        ObjectUtil.trimStringValues(dtoToCreate);
        validateBusinessRules(dtoToCreate);
        final Project entity = mapper.toEntity(dtoToCreate);
        communicationService.initializeProject(entity);
        final ProjectDTO createdProject = mapper.toDto(repository.save(entity));

        final long projectId = createdProject.getId().longValue();
        rootCauseRepository.saveAll(Arrays.asList(
                new RootCause().withProjectId(projectId).withName("Fragile test"),
                new RootCause().withProjectId(projectId).withName("Network issue"),
                new RootCause().withProjectId(projectId).withName("Regression"),
                new RootCause().withProjectId(projectId).withName("Test to update")));

        return createdProject;
    }

    /**
     * Update an entity.
     *
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException  when the given entity ID is not present in database
     * @throws NotUniqueException when the given code or name is already used by another entity
     */
    public ProjectDTO update(ProjectDTO dtoToUpdate) throws BadRequestException {
        ObjectUtil.trimStringValues(dtoToUpdate);

        // Must update an existing entity
        Optional<Project> dataBaseEntity = repository.findById(dtoToUpdate.getId());
        if (!dataBaseEntity.isPresent()) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }

        validateBusinessRules(dtoToUpdate);

        final Project entity = mapper.toEntity(dtoToUpdate);
        entity.setCommunications(dataBaseEntity.get().getCommunications());
        return mapper.toDto(repository.save(entity));
    }

    /**
     * Get all the entities.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        return mapper.toDto(repository.findAllByOrderByName());
    }

    /**
     * Get one project by code.
     *
     * @param code the code of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(String code) {
        return Optional.ofNullable(repository.findOneByCode(code)).map(mapper::toDto);
    }

    /**
     * Given a project code, returns its ID if it exists, or throw a {@link NotFoundException}.
     *
     * @param code the code of the mandatory project
     * @return the ID of the found project
     * @throws NotFoundException when the project code does not exist
     */
    public long toId(String code) throws NotFoundException {
        final Project project = repository.findOneByCode(code);
        if (project == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROJECT, Entities.PROJECT);
        }
        return project.getId().longValue();

    }

    private void validateBusinessRules(ProjectDTO dto) throws NotUniqueException {
        validateUniqueCode(dto);
        validateUniqueName(dto);
        validateOnlyOneDefault(dto);
    }

    private void validateUniqueCode(ProjectDTO dto) throws NotUniqueException {
        Project existingEntityWithSameCode = repository.findOneByCode(dto.getCode());
        if (existingEntityWithSameCode != null && !existingEntityWithSameCode.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_CODE, Entities.PROJECT, QProject.project.code.getMetadata().getName(), existingEntityWithSameCode.getId());
        }
    }

    private void validateUniqueName(ProjectDTO dto) throws NotUniqueException {
        Project existingEntityWithSameName = repository.findOneByName(dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_NAME, Entities.PROJECT, QProject.project.name.getMetadata().getName(), existingEntityWithSameName.getId());
        }
    }

    private void validateOnlyOneDefault(ProjectDTO dto) throws NotUniqueException {
        if (dto.isDefaultAtStartup()) {
            Project entityDataBaseWithDefaultAtStartup = repository.findByDefaultAtStartup(true);
            if (entityDataBaseWithDefaultAtStartup != null && !entityDataBaseWithDefaultAtStartup.getCode().equals(dto.getCode())) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_PROJECT_DEFAULT_AT_STARTUP, Entities.PROJECT, QProject.project.defaultAtStartup.getMetadata().getName(), entityDataBaseWithDefaultAtStartup.getCode());
            }
        }
    }

}
