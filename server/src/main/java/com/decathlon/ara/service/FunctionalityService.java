package com.decathlon.ara.service;

import com.decathlon.ara.domain.QFunctionality;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.functionality.FunctionalityWithChildrenDTO;
import com.decathlon.ara.service.dto.request.FunctionalityPosition;
import com.decathlon.ara.service.dto.request.MoveFunctionalityDTO;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.FunctionalityMapper;
import com.decathlon.ara.service.mapper.FunctionalityWithChildrenMapper;
import com.decathlon.ara.service.mapper.ScenarioMapper;
import com.decathlon.ara.service.support.TreePosition;
import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Functionality.
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FunctionalityService {

    @NonNull
    private final FunctionalityRepository repository;

    @NonNull
    private final CountryRepository countryRepository;

    @NonNull
    private final TeamRepository teamRepository;

    @NonNull
    private final TeamService teamService;

    @NonNull
    private final FunctionalityMapper mapper;

    @NonNull
    private final FunctionalityWithChildrenMapper mapperWithChildren;

    @NonNull
    private final ScenarioMapper scenarioMapper;

    private static boolean isFolder(FunctionalityDTO functionality) {
        return FunctionalityType.FOLDER.name().equals(functionality.getType());
    }

    /**
     * @param functionalities       the list of functionalities in which to search for searchedFunctionality
     * @param searchedFunctionality the functionality to search by ID in functionalities
     * @return the index of the searched functionality in the list, or -1 if not found or if searching for null
     */
    private static int indexOf(List<Functionality> functionalities, Functionality searchedFunctionality) {
        if (searchedFunctionality != null) {
            for (int i = 0; i < functionalities.size(); i++) {
                if (functionalities.get(i).getId().equals(searchedFunctionality.getId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get all the entities as a hierarchy.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of root entities with children
     */
    @Transactional(readOnly = true)
    public List<FunctionalityWithChildrenDTO> findAllAsTree(long projectId) {
        return buildTree(repository.findAllByProjectIdOrderByOrder(projectId), null);
    }

    /**
     * Update an existing functionality (or a folder).
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated functionality
     * @throws NotFoundException   when the functionality to update does not exist, or when the associated countries, teams or severity do not exist
     * @throws NotUniqueException  when the given name is already used by another functionality
     * @throws BadRequestException if the name is empty
     */
    public FunctionalityDTO update(long projectId, FunctionalityDTO dtoToUpdate) throws BadRequestException {
        ObjectUtil.trimStringValues(dtoToUpdate);

        // Must update an existing entity
        Functionality dataBaseEntity = repository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            String message = (isFolder(dtoToUpdate) ? Messages.NOT_FOUND_FUNCTIONALITY_FOLDER : Messages.NOT_FOUND_FUNCTIONALITY);
            throw new NotFoundException(message, Entities.FUNCTIONALITY);
        }

        // The updated entity must remain valid
        dtoToUpdate.setParentId(dataBaseEntity.getParentId());
        dtoToUpdate.setType(dataBaseEntity.getType().name());
        validateBusinessRules(projectId, dtoToUpdate);

        // We only update modifiable properties: don't change any data identifying the entity and its place or role in the tree
        Functionality entityToUpdate = mapper.toEntity(dtoToUpdate);
        entityToUpdate.setId(dataBaseEntity.getId());
        entityToUpdate.setProjectId(dataBaseEntity.getProjectId());
        entityToUpdate.setCreationDateTime(dataBaseEntity.getCreationDateTime());
        entityToUpdate.setUpdateDateTime(new Date());
        entityToUpdate.setParentId(dataBaseEntity.getParentId());
        entityToUpdate.setOrder(dataBaseEntity.getOrder());
        entityToUpdate.setType(dataBaseEntity.getType());
        entityToUpdate.setCoveredScenarios(dataBaseEntity.getCoveredScenarios());
        entityToUpdate.setCoveredCountryScenarios(dataBaseEntity.getCoveredCountryScenarios());
        entityToUpdate.setIgnoredScenarios(dataBaseEntity.getIgnoredScenarios());
        entityToUpdate.setIgnoredCountryScenarios(dataBaseEntity.getIgnoredCountryScenarios());
        return mapper.toDto(repository.save(entityToUpdate));
    }

    private void validateBusinessRules(long projectId, FunctionalityDTO functionality) throws BadRequestException {
        boolean isFolder = isFolder(functionality);

        // Type must be known
        if (!FunctionalityType.exists(functionality.getType())) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_UNKNOWN_TYPE, Entities.FUNCTIONALITY, "unknown_type");
        }

        // Name is mandatory
        if (StringUtils.isEmpty(functionality.getName())) {
            String message = (isFolder ? Messages.RULE_FUNCTIONALITY_FOLDER_MANDATORY_NAME : Messages.RULE_FUNCTIONALITY_MANDATORY_NAME);
            throw new BadRequestException(message, Entities.FUNCTIONALITY, "mandatory_name");
        }

        // Name is unique in a given folder
        Functionality existingEntityWithSameNameAndParent = repository.findByProjectIdAndNameAndParentId(projectId, functionality.getName(), functionality.getParentId());
        if (existingEntityWithSameNameAndParent != null && !existingEntityWithSameNameAndParent.getId().equals(functionality.getId())) {
            String message = (existingEntityWithSameNameAndParent.getType() == FunctionalityType.FOLDER ? Messages.NOT_UNIQUE_FUNCTIONALITY_FOLDER_NAME : Messages.NOT_UNIQUE_FUNCTIONALITY_NAME);
            throw new NotUniqueException(message, Entities.FUNCTIONALITY, QFunctionality.functionality.name.getMetadata().getName(), existingEntityWithSameNameAndParent.getId());
        }

        if (isFolder) {
            // Folders can only have a name
            validateFolder(functionality);
        } else {
            // Functionalities have mandatory fields
            validateFunctionality(functionality);
        }

        // Functionalities must only refer to existing entities
        validateWeakForeignKeys(projectId, functionality);
    }

    /**
     * Create a new entity.
     *
     * @param projectId the ID of the project in which to work
     * @param newDto    the entity to create, relative to another entity
     * @return the crated entity
     * @throws BadRequestException if something is wrong in the request
     */
    public FunctionalityDTO create(long projectId, NewFunctionalityDTO newDto) throws BadRequestException {
        ObjectUtil.trimStringValues(newDto.getFunctionality());

        // Compute new position and verify all technical rules related to the position of the node in the tree
        TreePosition treePosition = computeDestinationTreePosition(projectId, newDto.getReferenceId(), newDto.getRelativePosition(), null);

        // Define some properties (before toEntity for no NullPointerException)
        FunctionalityDTO dtoToCreate = newDto.getFunctionality();
        dtoToCreate.setId(null);
        dtoToCreate.setParentId(treePosition.getParentId());
        dtoToCreate.setOrder(treePosition.getOrder());

        // The created entity must be valid against business rules
        validateBusinessRules(projectId, dtoToCreate);

        final Functionality entity = mapper.toEntity(dtoToCreate);
        entity.setProjectId(projectId);
        entity.setCreationDateTime(new Date());
        entity.setUpdateDateTime(entity.getCreationDateTime());

        // New entities are not covered
        final boolean isFolder = entity.getType() == FunctionalityType.FOLDER;
        entity.setCoveredScenarios(isFolder ? null : Integer.valueOf(0));
        entity.setCoveredCountryScenarios(null);
        entity.setIgnoredScenarios(isFolder ? null : Integer.valueOf(0));
        entity.setIgnoredCountryScenarios(null);

        return mapper.toDto(repository.save(entity));
    }

    /**
     * Delete an entity by id, and its children, if any.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException if the entity id does not exist
     */
    public void delete(long projectId, long id) throws NotFoundException {
        Functionality entity = repository.findByProjectIdAndId(projectId, id);
        if (entity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_FUNCTIONALITY_OR_FOLDER, Entities.FUNCTIONALITY);
        }
        // Will cascade delete children
        repository.delete(entity);
    }

    /**
     * Move a functionality or folder to another place in the tree.
     *
     * @param projectId   the ID of the project in which to work
     * @param moveRequest the entity to move, relative to another entity
     * @return the updated entity
     * @throws BadRequestException if something is wrong in the request
     */
    public FunctionalityDTO move(long projectId, MoveFunctionalityDTO moveRequest) throws BadRequestException {
        Functionality source = repository.findByProjectIdAndId(projectId, moveRequest.getSourceId());
        if (source == null) {
            throw new NotFoundException(Messages.NOT_FOUND_FUNCTIONALITY_OR_FOLDER_TO_MOVE, Entities.FUNCTIONALITY);
        }

        // Compute new position and verify all technical rules related to the position of the node in the tree
        TreePosition treePosition = computeDestinationTreePosition(projectId, moveRequest.getReferenceId(), moveRequest.getRelativePosition(), source);
        source.setParentId(treePosition.getParentId());
        source.setOrder(treePosition.getOrder());

        return mapper.toDto(repository.save(source));
    }

    /**
     * Get scenarios covering the given functionality.
     *
     * @param projectId       the ID of the project in which to work
     * @param functionalityId the ID of the functionality to query
     * @return the list of scenarios covering the functionality
     * @throws BadRequestException if the entity id does not exist or refers to a folder
     */
    public List<ScenarioDTO> findScenarios(long projectId, long functionalityId) throws BadRequestException {
        // Must update an existing entity
        Functionality functionality = repository.findByProjectIdAndId(projectId, functionalityId);
        if (functionality == null) {
            throw new NotFoundException(Messages.NOT_FOUND_FUNCTIONALITY, Entities.FUNCTIONALITY);
        }
        if (functionality.getType() != FunctionalityType.FUNCTIONALITY) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_FOLDER_HAVE_NO_COVERAGE, Entities.FUNCTIONALITY, "folders_have_no_coverage");
        }
        return scenarioMapper.toDto(functionality.getScenarios());
    }

    private TreePosition computeDestinationTreePosition(long projectId, Long referenceId, FunctionalityPosition relativePosition, Functionality source) throws BadRequestException {
        FunctionalityPosition effectivePosition = (relativePosition == null ? FunctionalityPosition.LAST_CHILD : relativePosition);
        Functionality reference = findAndEnsureReference(projectId, referenceId, effectivePosition);

        // Parent may be null if inserting at root level; reference is mandatory if inserting above/below
        Long parentId;
        if (effectivePosition == FunctionalityPosition.LAST_CHILD) {
            parentId = referenceId;
        } else if (reference == null) {
            throw new NotGonnaHappenException("findAndEnsureReference should throw an exception instead of returning null for other positions than LAST_CHILD");
        } else {
            parentId = reference.getParentId();
        }

        // Verify functionalities will never get children
        ensureNotInsertingIntoFunctionality(projectId, effectivePosition, parentId);

        // If moving (not creating), check not moving into itself or one of its children
        if (reference != null && source != null && isDestinationInSource(projectId, reference, source)) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_MOVE_TO_ITSELF_OR_SUB_FOLDER, Entities.FUNCTIONALITY, "cannot_move_to_itself_or_sub_folder");
        }

        // Threat "insert LAST_CHILD" as "BELOW the last child of the parent in which to insert"
        List<Functionality> siblings = repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId);
        boolean insertBelow = (effectivePosition == FunctionalityPosition.BELOW);
        if (effectivePosition == FunctionalityPosition.LAST_CHILD) {
            reference = (siblings.isEmpty() ? null : siblings.get(siblings.size() - 1));
            insertBelow = true;
        }

        return new TreePosition(parentId, computeNewOrder(siblings, reference, insertBelow));
    }

    private void ensureNotInsertingIntoFunctionality(long projectId, FunctionalityPosition effectivePosition, Long parentId) throws BadRequestException {
        if (effectivePosition == FunctionalityPosition.LAST_CHILD && parentId != null) {
            Functionality parent = repository.findByProjectIdAndId(projectId, parentId.longValue());
            if (parent.getType() == FunctionalityType.FUNCTIONALITY) {
                throw new BadRequestException(Messages.RULE_FUNCTIONALITY_HAVE_NO_CHILDREN, Entities.FUNCTIONALITY, "functionalities_cannot_have_children");
            }
        }
    }

    private boolean isDestinationInSource(long projectId, Functionality destination, Functionality source) {
        if (destination.getId().equals(source.getId())) {
            return true;
        }

        FunctionalityWithChildrenDTO sourceInTree = find(findAllAsTree(projectId), source.getId());
        if (sourceInTree == null) {
            throw new NotGonnaHappenException("We found source & destination in database: if we query again and don't find them, the SQL transaction is tainted or we messed up the Hibernate cache!");
        }

        return find(sourceInTree.getChildren(), destination.getId()) != null;
    }

    private FunctionalityWithChildrenDTO find(List<FunctionalityWithChildrenDTO> nodes, Long id) {
        for (FunctionalityWithChildrenDTO node : nodes) {
            if (id.equals(node.getId())) {
                return node;
            }
            final FunctionalityWithChildrenDTO match = find(node.getChildren(), id);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    /**
     * @param referenceId    the ID of the reference to find in database (can be null)
     * @param targetPosition the position to insert/move another functionality, relative to that referenceId
     * @return null if referenceId is null and targetPosition is not LAST_CHILD, the found reference otherwise
     * @throws BadRequestException if requesting a null referenceId for a LAST_CHILD targetPosition or cannot find requested referenceId
     */
    private Functionality findAndEnsureReference(long projectId, Long referenceId, FunctionalityPosition targetPosition) throws BadRequestException {
        Functionality reference;
        if (referenceId == null) {
            if (targetPosition != FunctionalityPosition.LAST_CHILD) {
                throw new BadRequestException(Messages.RULE_FUNCTIONALITY_NO_REFERENCE, Entities.FUNCTIONALITY, "no_reference");
            }
            reference = null;
        } else {
            reference = repository.findByProjectIdAndId(projectId, referenceId.longValue());
            if (reference == null) {
                throw new NotFoundException(Messages.NOT_FOUND_FUNCTIONALITY_OR_FOLDER_REFERENCE, Entities.FUNCTIONALITY);
            }
        }
        return reference;
    }

    /**
     * @param siblings               the list of sibling functionalities (can be empty) in which to find referenceFunctionality (if not null)
     * @param referenceFunctionality the reference functionality to find in the list (can be null)
     * @param insertBelow            true to insert below, false to insert above
     * @return the new order to use to place the new functionality correctly (the mean of the found functionality order and the one above or below; if no above/below/siblings, the number is averaged with 1 and the maximum Long value minus 1)
     */
    private double computeNewOrder(List<Functionality> siblings, Functionality referenceFunctionality, boolean insertBelow) {
        // A note about the ordering algorithm:

        // "Too Long Don't Read":
        // The goal is to allow concurrent edition of the tree while eliminating most conflicts.
        // Each and every edit action must touch only a single row in database.
        // Moving a node at another position of the tree must also be a single-row update.
        // To move a node between two other nodes, each have greatly-separated "order" numbers and we compute the average of both to get the new "order" of the target node.
        // => Worst case scenario: a node with two children and repeatedly move on top of each other:
        // => We can swap the two nodes a maximum of 2099 times, which is quite large.

        // From Double.MAX_VALUE, when dividing by 2 repeatedly in Java and ...
        // from Number.MAX_VALUE, when dividing by 2 repeatedly in JavaScript ...
        // give the exact same result when dividing by two 954 times.
        // Then, they have different formatting but have the same value, rarely having a differently rounded last digit.
        // But the rounding errors do not impact the the meaning of the algorithm of dividing by 2
        // We get to 0 at the 2099th division by two.
        // You'd have to swap each second during 35 minutes non-stop before harming the algorithm.
        // In the improbable case that such case would appear (eg. by a batch doing heavy work each night),
        // we could come up with a reorder algorithm to spread all "order"s along the Double axis...
        // Then the limiting factor would become storage space, as it would need an astronomical amount of lines to put the algorithm to harm.

        double lowerOrder = 0;
        double upperOrder = Double.MAX_VALUE - 1;

        int referenceIndex = indexOf(siblings, referenceFunctionality);
        if (referenceIndex != -1) {
            if (insertBelow) {
                lowerOrder = siblings.get(referenceIndex).getOrder();
                if (referenceIndex + 1 < siblings.size()) {
                    upperOrder = siblings.get(referenceIndex + 1).getOrder();
                }
            } else {
                upperOrder = siblings.get(referenceIndex).getOrder();
                if (referenceIndex > 0) {
                    lowerOrder = siblings.get(referenceIndex - 1).getOrder();
                }
            }
        }

        // Compute the middle of lowerOrder and upperOrder
        // Do NOT factor the computation, as it will lead to computing Infinity: we are handling very big numbers
        return lowerOrder / 2 + upperOrder / 2;
    }

    private void validateFolder(FunctionalityDTO functionality) throws BadRequestException {
        boolean folderHasContent;

        folderHasContent = StringUtils.isNotEmpty(functionality.getCountryCodes());
        folderHasContent |= functionality.getTeamId() != null;
        folderHasContent |= StringUtils.isNotEmpty(functionality.getSeverity());
        folderHasContent |= StringUtils.isNotEmpty(functionality.getCreated());
        folderHasContent |= Boolean.TRUE.equals(functionality.getStarted());
        folderHasContent |= Boolean.TRUE.equals(functionality.getNotAutomatable());
        folderHasContent |= functionality.getCoveredScenarios() != null && functionality.getCoveredScenarios().intValue() != 0;
        folderHasContent |= functionality.getIgnoredScenarios() != null && functionality.getIgnoredScenarios().intValue() != 0;
        folderHasContent |= StringUtils.isNotEmpty(functionality.getCoveredCountryScenarios());
        folderHasContent |= StringUtils.isNotEmpty(functionality.getIgnoredCountryScenarios());
        folderHasContent |= StringUtils.isNotEmpty(functionality.getComment());

        if (folderHasContent) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_FOLDER_ONLY_NAME, Entities.FUNCTIONALITY, "folder_can_only_have_name");
        }
    }

    private void validateFunctionality(FunctionalityDTO functionality) throws BadRequestException {
        if (functionality.getTeamId() == null) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_MANDATORY_TEAM, Entities.FUNCTIONALITY, "mandatory_team_id");
        }

        if (StringUtils.isEmpty(functionality.getSeverity())) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_MANDATORY_SEVERITY, Entities.FUNCTIONALITY, "mandatory_severity");
        }

        if (StringUtils.isEmpty(functionality.getCountryCodes())) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_MANDATORY_COUNTRY_CODES, Entities.FUNCTIONALITY, "mandatory_country_codes");
        }

        if (functionality.getStarted() == Boolean.TRUE && functionality.getNotAutomatable() == Boolean.TRUE) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_EXCLUSIVE_STARTED_AND_NOT_AUTOMATABLE, Entities.FUNCTIONALITY, "exclusive_started_and_not_automatable");
        }
    }

    private void validateWeakForeignKeys(long projectId, FunctionalityDTO functionality) throws BadRequestException {
        if (StringUtils.isNotEmpty(functionality.getCountryCodes())) {
            final List<String> existingCountryCodes = countryRepository.findCodesByProjectId(projectId);
            for (String countryCode : functionality.getCountryCodes().split(Functionality.COUNTRY_CODES_SEPARATOR)) {
                if (!existingCountryCodes.contains(countryCode)) {
                    throw new NotFoundException(Messages.NOT_FOUND_COUNTRY, Entities.COUNTRY);
                }
            }
        }

        if (functionality.getTeamId() != null) {
            TeamDTO team = teamService.findOne(projectId, functionality.getTeamId().longValue());
            if (!team.isAssignableToFunctionalities()) {
                throw new BadRequestException(Messages.RULE_TEAM_NOT_ASSIGNABLE_TO_FUNCTIONALITIES, Entities.TEAM, "not_assignable_team");
            }
        }

        if (StringUtils.isNotEmpty(functionality.getSeverity()) && !FunctionalitySeverity.exists(functionality.getSeverity())) {
            throw new BadRequestException(Messages.RULE_FUNCTIONALITY_SEVERITY_WRONG, Entities.FUNCTIONALITY, "wrong_severity");
        }
    }

    private List<FunctionalityWithChildrenDTO> buildTree(List<Functionality> flatFunctionalities, Long parentId) {
        List<FunctionalityWithChildrenDTO> nodes = new ArrayList<>();
        for (Functionality functionality : flatFunctionalities) {
            if (Objects.equals(functionality.getParentId(), parentId)) {
                FunctionalityWithChildrenDTO dto = mapperWithChildren.toDto(functionality);
                dto.setChildren(buildTree(flatFunctionalities, functionality.getId()));
                nodes.add(dto); // flatFunctionalities is already sorted by "order" column
            }
        }
        return nodes;
    }

}
