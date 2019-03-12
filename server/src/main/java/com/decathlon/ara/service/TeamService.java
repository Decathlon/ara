package com.decathlon.ara.service;

import com.decathlon.ara.domain.QTeam;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.TeamMapper;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Team.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    @NonNull
    private final TeamRepository repository;

    @NonNull
    private final TeamMapper mapper;

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final ProblemRepository problemRepository;

    /**
     * Create a new entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToCreate the entity to save
     * @return the persisted entity
     * @throws NotUniqueException when the given name is already used by another entity
     */
    public TeamDTO create(long projectId, TeamDTO dtoToCreate) throws NotUniqueException {
        ObjectUtil.trimStringValues(dtoToCreate);
        validateBusinessRules(projectId, dtoToCreate);
        final Team entity = mapper.toEntity(dtoToCreate);
        entity.setProjectId(projectId);
        return mapper.toDto(repository.save(entity));
    }

    /**
     * Update an entity.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated entity, if the entity was present in database
     * @throws NotFoundException   when the given entity ID is not present in database
     * @throws NotUniqueException  when the given name is already used by another entity
     * @throws BadRequestException when trying to forbid assignation to an entity type still having assignations
     */
    public TeamDTO update(long projectId, TeamDTO dtoToUpdate) throws BadRequestException {
        ObjectUtil.trimStringValues(dtoToUpdate);

        // Must update an existing entity
        Team dataBaseEntity = repository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }

        validateBusinessRules(projectId, dtoToUpdate);

        if (!dtoToUpdate.isAssignableToFunctionalities() && functionalityRepository.existsByProjectIdAndTeamId(projectId, dataBaseEntity.getId().longValue())) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_ASSIGNED_FUNCTIONALITIES, Entities.TEAM, "has_assigned_functionalities");
        }

        if (!dtoToUpdate.isAssignableToProblems() && problemRepository.existsByProjectIdAndBlamedTeam(projectId, dataBaseEntity)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_ASSIGNED_PROBLEMS, Entities.TEAM, "has_assigned_problems");
        }

        final Team entity = mapper.toEntity(dtoToUpdate);
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
    public List<TeamDTO> findAll(long projectId) {
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
    public TeamDTO findOne(long projectId, long id) throws NotFoundException {
        final Team team = repository.findByProjectIdAndId(projectId, id);
        if (team == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }
        return mapper.toDto(team);
    }

    /**
     * Delete an entity by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException   if the entity id does not exist
     * @throws BadRequestException if the team has associated functionalities or problems
     */
    public void delete(long projectId, long id) throws BadRequestException {
        Team entity = repository.findByProjectIdAndId(projectId, id);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_TEAM, Entities.TEAM);
        }

        if (functionalityRepository.existsByProjectIdAndTeamId(projectId, id)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_FUNCTIONALITIES, Entities.TEAM, "has_functionalities");
        }

        if (problemRepository.existsByProjectIdAndBlamedTeam(projectId, entity)) {
            throw new BadRequestException(Messages.RULE_TEAM_HAS_PROBLEMS, Entities.TEAM, "has_problems");
        }

        repository.delete(entity);
    }

    private void validateBusinessRules(long projectId, TeamDTO dto) throws NotUniqueException {
        Team existingEntityWithSameName = repository.findByProjectIdAndName(projectId, dto.getName());
        if (existingEntityWithSameName != null && !existingEntityWithSameName.getId().equals(dto.getId())) {
            throw new NotUniqueException(Messages.NOT_UNIQUE_TEAM_NAME, Entities.TEAM, QTeam.team.name.getMetadata().getName(), existingEntityWithSameName.getId());
        }
    }

}
