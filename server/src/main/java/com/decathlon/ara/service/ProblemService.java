package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.QProblem;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.projection.ProblemAggregate;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.ci.service.DateService;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemAggregateDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problem.ProblemFilterDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithAggregateDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsAndAggregateTDO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.PickUpPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.stability.CycleStabilityDTO;
import com.decathlon.ara.service.dto.stability.ExecutionStabilityDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadGatewayException;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.ErrorWithExecutedScenarioAndRunAndExecutionMapper;
import com.decathlon.ara.service.mapper.ProblemAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemFilterMapper;
import com.decathlon.ara.service.mapper.ProblemMapper;
import com.decathlon.ara.service.mapper.ProblemPatternMapper;
import com.decathlon.ara.service.mapper.ProblemWithAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemWithPatternsAndAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemWithPatternsMapper;
import com.decathlon.ara.service.mapper.RootCauseMapper;
import com.decathlon.ara.service.mapper.TeamMapper;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Problem.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemService {

    private static final String STABILITY_NOT_RUN = "-";
    private static final String STABILITY_ERROR = "E";
    private static final String STABILITY_OK = "O";

    @NonNull
    private final ProblemRepository problemRepository;

    @NonNull
    private final ErrorRepository errorRepository;

    @NonNull
    private final ProblemPatternRepository problemPatternRepository;

    @NonNull
    private final ExecutionRepository executionRepository;

    @NonNull
    private final CycleDefinitionRepository cycleDefinitionRepository;

    @NonNull
    private final RootCauseRepository rootCauseRepository;

    @NonNull
    private final ProblemPatternService problemPatternService;

    @NonNull
    private final ProblemDenormalizationService problemDenormalizationService;

    @NonNull
    private final RootCauseService rootCauseService;

    @NonNull
    private final TeamService teamService;

    @NonNull
    private final DateService dateService;

    @NonNull
    private final DefectService defectService;

    @NonNull
    private final ProblemMapper problemMapper;

    @NonNull
    private final ProblemFilterMapper problemFilterMapper;

    @NonNull
    private final ProblemWithAggregateMapper problemWithAggregateMapper;

    @NonNull
    private final ProblemWithPatternsMapper problemWithPatternsMapper;

    @NonNull
    private final ProblemPatternMapper problemPatternMapper;

    @NonNull
    private final ErrorWithExecutedScenarioAndRunAndExecutionMapper errorWithExecutedScenarioAndRunAndExecutionMapper;

    @NonNull
    private final ProblemAggregateMapper problemAggregateMapper;

    @NonNull
    private final TeamMapper teamMapper;

    @NonNull
    private final RootCauseMapper rootCauseMapper;

    @NonNull
    private final ProblemWithPatternsAndAggregateMapper problemWithPatternsAndAggregateMapper;

    @NonNull
    private final JpaCacheManager jpaCacheManager;

    @NonNull
    private final TransactionService transactionService;

    private static void validateClosedProblemHasRootCause(ProblemDTO problemDto) throws BadRequestException {
        if (problemDto.getStatus() == ProblemStatus.CLOSED && (problemDto.getRootCause() == null ||
                problemDto.getRootCause().getId() == null ||
                problemDto.getRootCause().getId().longValue() <= 0)) {
            throw new BadRequestException(Messages.RULE_PROBLEM_CLOSED_MUST_HAVE_ROOT_CAUSE, Entities.PROBLEM,
                    "root_cause_mandatory_for_closed_problems");
        }
    }

    private static List<ExecutionStabilityDTO> computeExecutionStability(int lastExecutionCount,
                                                                         List<Execution> lastExecutions,
                                                                         List<Long> failedExecutionIds) {
        ExecutionStabilityDTO[] executionStabilityDTOS = new ExecutionStabilityDTO[lastExecutionCount];
        for (int i = 0; i < lastExecutionCount; i++) {
            int pos = lastExecutionCount - 1 - i;
            if (i >= lastExecutions.size()) {
                executionStabilityDTOS[pos] = new ExecutionStabilityDTO()
                        .withStatus(STABILITY_NOT_RUN);
            } else {
                String status;
                if (failedExecutionIds != null && failedExecutionIds.contains(lastExecutions.get(i).getId())) {
                    status = STABILITY_ERROR;
                } else {
                    status = STABILITY_OK;
                }
                executionStabilityDTOS[pos] = new ExecutionStabilityDTO()
                        .withExecutionId(lastExecutions.get(i).getId())
                        .withStatus(status)
                        .withTestDate(lastExecutions.get(i).getTestDateTime());
            }
        }
        return Arrays.asList(executionStabilityDTOS);
    }

    /**
     * Create a new problem.
     *
     * @param projectId  the ID of the project in which to work
     * @param problemDto the entity to save
     * @return the persisted entity
     * @throws BadRequestException when the given name or defect ID is already used by another entity, or when the team
     *                             does not exist or is not assignable to a problem
     */
    public ProblemWithPatternsDTO create(long projectId, ProblemWithPatternsDTO problemDto) throws BadRequestException {
        problemDto.setCreationDateTime(new Date()); // Will not change anymore
        problemDto.setDefectExistence(null); // Will be computed if has a defect ID, but ignore the requested value if no defect
        problemDto.setStatus(ProblemStatus.OPEN); // Open at creation time: ignore the requested status
        problemDto.setClosingDateTime(null); // Open at creation time: ignore the requested closingDateTime

        validateBusinessRules(
                projectId,
                problemDto,
                "Consider appending the aggregation criteria to this existing problem.",
                null);

        final Problem entity = problemWithPatternsMapper.toEntity(problemDto);
        entity.setProjectId(projectId);
        if (entity.getPatterns() != null) {
            for (ProblemPattern pattern : entity.getPatterns()) {
                problemPatternService.assignExistingEntities(projectId, pattern);
            }
        }
        Problem problem = problemRepository.save(entity);

        if (problem.getPatterns() != null) {
            for (ProblemPattern pattern : problem.getPatterns()) {
                errorRepository.assignPatternToErrors(projectId, pattern);
            }
        }

        problemDenormalizationService.updateFirstAndLastSeenDateTimes(Collections.singleton(problem));

        return problemWithPatternsMapper.toDto(problem);
    }

    /**
     * Update the properties of a problem, without affecting its children patterns.
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated problem
     * @throws NotFoundException   when the problem to update does not exist
     * @throws NotUniqueException  when the given name or defect ID is already used by another entity
     * @throws BadRequestException if removing the root cause of a closed problem
     */
    @Transactional
    public ProblemDTO updateProperties(long projectId, ProblemDTO dtoToUpdate) throws BadRequestException {
        Problem dataBaseEntity = problemRepository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (dataBaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        String oldDefectId = dataBaseEntity.getDefectId();

        // If defectId has not changed, validateBusinessRules()/handleDefectIdChange() will not update these fields
        dtoToUpdate.setDefectExistence(dataBaseEntity.getDefectExistence());
        dtoToUpdate.setStatus(dataBaseEntity.getStatus());
        dtoToUpdate.setClosingDateTime(dataBaseEntity.getClosingDateTime());

        // AT THE VERY LAST, for no useless HTTP request to the defect provider URL
        validateBusinessRules(
                projectId,
                dtoToUpdate,
                "Consider moving the aggregation criteria to this existing problem.",
                oldDefectId);

        // Now that all business rules are met, explicitly modify only allowed fields
        // (status update must be done via open/close methods, don't change patterns, creation timestamp is anchored...)
        dataBaseEntity.setName(dtoToUpdate.getName());
        dataBaseEntity.setComment(dtoToUpdate.getComment());
        dataBaseEntity.setBlamedTeam(teamMapper.toEntity(dtoToUpdate.getBlamedTeam()));
        dataBaseEntity.setDefectId(dtoToUpdate.getDefectId());
        dataBaseEntity.setRootCause(rootCauseMapper.toEntity(dtoToUpdate.getRootCause()));

        // validateBusinessRules()/handleDefectIdChange() could have changed these fields: save them too
        dataBaseEntity.setDefectExistence(dtoToUpdate.getDefectExistence());
        dataBaseEntity.setStatus(dtoToUpdate.getStatus());
        dataBaseEntity.setClosingDateTime(dtoToUpdate.getClosingDateTime());

        return problemMapper.toDto(problemRepository.save(dataBaseEntity));
    }

    void handleDefectIdChange(long projectId, ProblemDTO problemDto, String oldDefectId) throws BadRequestException {
        final Optional<DefectAdapter> maybeDefectAdapter = defectService.getAdapter(projectId);
        if (!maybeDefectAdapter.isPresent()) {
            return;
        }
        DefectAdapter defectAdapter = maybeDefectAdapter.get();

        final String newDefectId = problemDto.getDefectId();
        if (!Objects.equals(newDefectId, oldDefectId)) {
            if (StringUtils.isEmpty(newDefectId)) {
                // Removed defect assignation
                problemDto.setDefectId(null); // "" => null
                problemDto.setDefectExistence(null); // be sure to remove if it was UNKNOWN (EXISTS has no sense neither)
                // Here, we keep the status and closingDateTime unchanged
            } else {
                // Add defect assignation: is format valid? Fast check
                if (!defectAdapter.isValidId(projectId, newDefectId)) {
                    final String hint = defectAdapter.getIdFormatHint(projectId);
                    final String message = String.format(Messages.RULE_PROBLEM_WITH_WRONG_DEFECT_ID_FORMAT, hint);
                    throw new BadRequestException(message, Entities.PROBLEM, "wrong_defect_id_format");
                }

                // Check existence and current status
                final List<Defect> statuses;
                try {
                    statuses = defectAdapter.getStatuses(projectId, Collections.singletonList(newDefectId));
                } catch (FetchException e) {
                    // Also catch RuntimeException to not impact calling code in case of a faulty DefectAdapter in a custom ARA
                    log.error("Cannot check existence of new defect assignation to problem (defect {})", newDefectId, e);
                    problemDto.setDefectExistence(DefectExistence.UNKNOWN); // For re-indexing when connexion to bug-tracker is back
                    problemDto.setStatus(ProblemStatus.OPEN); // In case defect was closed and removed: it should be acted on
                    problemDto.setClosingDateTime(null); // Not CLOSED anymore (if it was)
                    return;
                }
                if (statuses.isEmpty()) {
                    throw new NotFoundException(Messages.NOT_FOUND_DEFECT, "defect");
                } else {
                    problemDto.setDefectExistence(DefectExistence.EXISTS);
                    problemDto.setStatus(statuses.get(0).getStatus());
                    problemDto.setClosingDateTime(statuses.get(0).getCloseDateTime());
                }
            }
        }
    }

    private void validateBusinessRules(long projectId, ProblemDTO problemDto, String hint, String oldDefectId) throws BadRequestException {
        ObjectUtil.trimStringValues(problemDto);
        validateUniqueName(projectId, problemDto, hint);
        validateUniqueDefectId(projectId, problemDto, hint);
        validateBlamedTeamExistsAndIsAssignable(projectId, problemDto);
        validateRootCauseExists(projectId, problemDto);
        validateClosedProblemHasRootCause(problemDto);
        handleDefectIdChange(projectId, problemDto, oldDefectId); // AT THE VERY LAST, for no useless HTTP request
    }

    private void validateUniqueName(long projectId, ProblemDTO problemDto, String hint) throws NotUniqueException {
        Problem existingProblemWithSameName = problemRepository.findByProjectIdAndName(projectId, problemDto.getName());
        if (existingProblemWithSameName != null && !existingProblemWithSameName.getId().equals(problemDto.getId())) {
            throw new NotUniqueException(
                    Messages.NOT_UNIQUE_PROBLEM_NAME + " " + hint,
                    Entities.PROBLEM,
                    QProblem.problem.name.getMetadata().getName(),
                    existingProblemWithSameName.getId());
        }
    }

    private void validateUniqueDefectId(long projectId, ProblemDTO problemDto, String hint) throws NotUniqueException {
        if (StringUtils.isNotEmpty(problemDto.getDefectId())) {
            Problem existingProblemWithSameDefectId = problemRepository.findByProjectIdAndDefectId(projectId, problemDto.getDefectId());
            if (existingProblemWithSameDefectId != null && !existingProblemWithSameDefectId.getId().equals(problemDto.getId())) {
                throw new NotUniqueException(
                        Messages.NOT_UNIQUE_PROBLEM_DEFECT_ID + " " + hint,
                        Entities.PROBLEM,
                        QProblem.problem.defectId.getMetadata().getName(),
                        existingProblemWithSameDefectId.getId());
            }
        }
    }

    private void validateBlamedTeamExistsAndIsAssignable(long projectId, ProblemDTO problemDto) throws BadRequestException {
        if (problemDto.getBlamedTeam() != null) {
            final long teamId = problemDto.getBlamedTeam().getId().longValue();
            TeamDTO team = teamService.findOne(projectId, teamId);
            if (!team.isAssignableToProblems()) {
                throw new BadRequestException(Messages.RULE_TEAM_NOT_ASSIGNABLE_TO_PROBLEMS, Entities.TEAM,
                        "not_assignable_team");
            } else {
                // We only care about the ID given by the client: replace the other properties with real ones
                problemDto.setBlamedTeam(team);
            }
        }
    }

    private void validateRootCauseExists(long projectId, ProblemDTO problemDto) throws BadRequestException {
        if (problemDto.getRootCause() != null) {
            final long rootCauseId = problemDto.getRootCause().getId().longValue();
            RootCauseDTO rootCause = rootCauseService.findOne(projectId, rootCauseId);
            // We only care about the ID given by the client: replace the other properties with real ones
            problemDto.setRootCause(rootCause);
        }
    }

    /**
     * Get one problem by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the entity
     * @throws NotFoundException if the problem cannot be found
     */
    @Transactional(readOnly = true)
    public ProblemDTO findOne(long projectId, long id) throws NotFoundException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, id);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }
        return problemMapper.toDto(problem);
    }

    /**
     * Get one problem by id, with its patterns.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the entity
     * @throws NotFoundException if the problem cannot be found
     */
    @Transactional(readOnly = true)
    public ProblemWithPatternsAndAggregateTDO findOneWithPatterns(long projectId, long id) throws NotFoundException {
        // Find problems
        Problem problem = problemRepository.findByProjectIdAndId(projectId, id);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }
        ProblemWithPatternsAndAggregateTDO problemDTO = problemWithPatternsAndAggregateMapper.toDto(problem);

        // Compute and assign aggregate to the problem
        List<Long> problemIds = Collections.singletonList(problemDTO.getId());
        Map<Long, ProblemAggregate> aggregates = problemRepository.findProblemAggregates(projectId, problemIds);
        ProblemAggregate aggregate = aggregates.get(problemDTO.getId());
        problemDTO.setAggregate(aggregate == null ? new ProblemAggregateDTO() : problemAggregateMapper.toDto(aggregate));

        // Compute and assign stability of each problem into their existing aggregate
        assignProblemStabilities(projectId, Collections.singletonList(problemDTO));

        return problemDTO;
    }

    /**
     * Delete the problem by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @throws NotFoundException if the entity id does not exist
     */
    public void delete(long projectId, long id) throws NotFoundException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, id);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        evictErrorProblemPatternsCacheFor(problem);

        problemRepository.delete(problem);
    }

    /**
     * Schedule an evict-cache action for after commit. The evict action will clear problemPatterns cache of the errors
     * CURRENTLY assigned to the given problem (at the time of the method call).
     *
     * @param problem the problem whose currently-assigned-errors' problemPatterns should be cache-evicted after
     *                transaction commit
     */
    private void evictErrorProblemPatternsCacheFor(Problem problem) {
        final Set<Long> errorIds = problem.getPatterns()
                .stream()
                .flatMap(p -> p.getErrors().stream())
                .map(Error::getId)
                .collect(Collectors.toSet());

        transactionService.doAfterCommit(() ->
                jpaCacheManager.evictCollections(Error.PROBLEM_PATTERNS_COLLECTION_CACHE, errorIds));
    }

    /**
     * GET all problems matching the given filter.
     *
     * @param projectId the ID of the project in which to work
     * @param filter    the search terms
     * @param pageable  the pagination information
     * @return a page of matching problems
     */
    @Transactional(readOnly = true)
    public Page<ProblemWithAggregateDTO> findMatchingProblems(long projectId, ProblemFilterDTO filter, Pageable pageable) {
        // Find problems
        Page<ProblemWithAggregateDTO> page = problemRepository
                .findMatchingProblems(problemFilterMapper.toEntity(filter).withProjectId(projectId), pageable)
                .map(problemWithAggregateMapper::toDto);

        // Compute and assign aggregates of each problem
        List<Long> problemIds = page.getContent().stream().map(ProblemDTO::getId).collect(Collectors.toList());
        Map<Long, ProblemAggregate> aggregates = problemRepository.findProblemAggregates(projectId, problemIds);
        for (ProblemWithAggregateDTO problem : page.getContent()) {
            ProblemAggregate aggregate = aggregates.get(problem.getId());
            problem.setAggregate(aggregate == null ? new ProblemAggregateDTO() : problemAggregateMapper.toDto(aggregate));
        }

        // Compute and assign stability of each problem into their existing aggregate
        assignProblemStabilities(projectId, page.getContent());

        return page;
    }

    /**
     * Get all errors associated to the given problem id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @param pageable  the meta-data of the requested page
     * @return a page of the errors associated to the problem
     * @throws NotFoundException if the entity id does not exist
     */
    @Transactional(readOnly = true)
    public Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> getProblemErrors(long projectId, long id, Pageable pageable) throws NotFoundException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, id);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        Page<Error> errors = errorRepository.findDistinctByProblemPatternsInOrderById(problem.getPatterns(), pageable);
        return errors.map(errorWithExecutedScenarioAndRunAndExecutionMapper::toDto);
    }

    /**
     * Append a new pattern to the given problem.
     *
     * @param projectId     the ID of the project in which to work
     * @param problemId     the problem where to append the pattern to
     * @param newPatternDTO the new pattern to append to the existing problem
     * @return the persisted entity
     * @throws BadRequestException when destination problem does not exist, or when a pattern with same criterion already
     *                             exists for this problem
     */
    public ProblemPatternDTO appendPattern(long projectId, long problemId, ProblemPatternDTO newPatternDTO) throws BadRequestException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, problemId);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }
        ProblemPattern newPattern = problemPatternMapper.toEntity(newPatternDTO);
        newPattern.setProblem(problem); // BEFORE the next Pattern.equals(...)
        problemPatternService.assignExistingEntities(projectId, newPattern);

        for (ProblemPattern existingPattern : problem.getPatterns()) {
            // ProblemPattern.equals(...) uses problemId & all aggregation rule criteria
            // Here, both pattern.problemId are equal, so we check criteria equality inside the same problem
            if (existingPattern.equals(newPattern)) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_PATTERN_IN_PROBLEM, Entities.PROBLEM_PATTERN,
                        QProblem.problem.patterns.getMetadata().getName(), existingPattern.getId());
            }
        }

        problem.addPattern(newPattern);
        newPattern = problemPatternRepository.save(newPattern);

        errorRepository.assignPatternToErrors(projectId, newPattern);

        problemDenormalizationService.updateFirstAndLastSeenDateTimes(Collections.singleton(problem));

        return problemPatternMapper.toDto(newPattern);
    }

    /**
     * Move a (source) pattern from its own problem to another (destination) problem.
     * If the source problem becomes without pattern, it is removed.
     *
     * @param projectId            the ID of the project in which to work
     * @param destinationProblemId the problem where to move the pattern to
     * @param sourcePatternId      the pattern to be moved
     * @return the destination problem, and the source problem if the source problem has been removed (because it now has
     * no pattern)
     * @throws BadRequestException when source pattern or destination problem does not exist, or when source and
     *                             destination problems are the same
     */
    public PickUpPatternDTO pickUpPattern(long projectId, long destinationProblemId, long sourcePatternId) throws BadRequestException {
        Problem destinationProblem = problemRepository.findByProjectIdAndId(projectId, destinationProblemId);
        if (destinationProblem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM_DESTINATION, Entities.PROBLEM);
        }

        ProblemPattern sourcePattern = problemPatternRepository.findByProjectIdAndId(projectId, sourcePatternId);
        if (sourcePattern == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PATTERN_TO_MOVE, Entities.PROBLEM_PATTERN);
        }

        Problem sourceProblem = sourcePattern.getProblem();
        if (destinationProblemId == sourceProblem.getId().longValue()) {
            throw new BadRequestException(Messages.RULE_PATTERN_MOVE_NO_OP, Entities.PROBLEM, "source_is_destination");
        }

        for (ProblemPattern existingPattern : destinationProblem.getPatterns()) {
            // Problem.equals() compare the uniqueness of the entity:
            // we want to compare the uniqueness of criteria INSIDE a given problem, hence the withProblemId()
            if (existingPattern.withProblemId(sourceProblem.getId()).equals(sourcePattern)) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_PATTERN_IN_PROBLEM, Entities.PROBLEM_PATTERN,
                        QProblem.problem.patterns.getMetadata().getName(), existingPattern.getId());
            }
        }

        // Do the real work
        sourceProblem.removePattern(sourcePattern);
        destinationProblem.addPattern(sourcePattern);
        List<Problem> updatedProblems = problemRepository.saveAll(Arrays.asList(sourceProblem, destinationProblem));
        sourceProblem = updatedProblems.get(0);
        destinationProblem = updatedProblems.get(1);

        problemDenormalizationService.updateFirstAndLastSeenDateTimes(updatedProblems);

        // Build the response the result of the work
        PickUpPatternDTO response = new PickUpPatternDTO();
        response.setDestinationProblem(problemMapper.toDto(destinationProblem));

        // Remove the source problem if it has no pattern anymore
        if (sourceProblem.getPatterns().isEmpty()) {
            problemRepository.delete(sourceProblem);
            response.setDeletedProblem(problemMapper.toDto(sourceProblem));
        }

        return response;
    }

    /**
     * Close a problem while assigning it a root cause.
     *
     * @param projectId   the ID of the project in which to work
     * @param problemId   the ID of the problem to close
     * @param rootCauseId the ID of the root-cause to set before closing the problem
     * @return the updated problem
     * @throws NotFoundException   if either problem or root cause cannot be found
     * @throws BadRequestException if the problem has a defect ID (no manual status change in this case)
     */
    public ProblemDTO close(long projectId, long problemId, long rootCauseId) throws BadRequestException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, problemId);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        if (StringUtils.isNotEmpty(problem.getDefectId()) && defectService.getAdapter(projectId).isPresent()) {
            throw new BadRequestException(Messages.RULE_PROBLEM_WITH_DEFECT_CANNOT_CHANGE_STATUS_MANUALLY,
                    Entities.PROBLEM, "problem_status_managed_by_defect");
        }

        RootCause rootCause = rootCauseRepository.findByProjectIdAndId(projectId, rootCauseId);
        if (rootCause == null) {
            throw new NotFoundException(Messages.NOT_FOUND_ROOT_CAUSE, Entities.ROOT_CAUSE);
        }

        // Change status and root-cause, but keep other properties and patterns
        problem.setStatus(ProblemStatus.CLOSED);
        problem.setClosingDateTime(dateService.now());
        problem.setRootCause(rootCause);
        problem.setPatterns(problem.getPatterns());
        return problemMapper.toDto(problemRepository.save(problem));
    }

    /**
     * Reopen a closed problem. Does nothing if the problem is already open.
     *
     * @param projectId the ID of the project in which to work
     * @param problemId the ID of the problem to reopen
     * @return the updated problem
     * @throws NotFoundException   if the problem cannot be found
     * @throws BadRequestException if the problem has a defect ID (no manual status change in this case)
     */
    public ProblemDTO reopen(long projectId, long problemId) throws BadRequestException {
        Problem problem = problemRepository.findByProjectIdAndId(projectId, problemId);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        if (StringUtils.isNotEmpty(problem.getDefectId()) && defectService.getAdapter(projectId).isPresent()) {
            throw new BadRequestException(Messages.RULE_PROBLEM_WITH_DEFECT_CANNOT_CHANGE_STATUS_MANUALLY,
                    Entities.PROBLEM, "problem_status_managed_by_defect");
        }

        // Change status, but keep other properties and patterns
        problem.setStatus(ProblemStatus.OPEN);
        problem.setClosingDateTime(null);
        return problemMapper.toDto(problemRepository.save(problem));
    }

    /**
     * Query the defect status from the external defect tracking system, update the problem status and return the
     * updated problem.<br>
     * Does nothing if no defect ID is assigned.<br>
     * If the system can be contacted, the problem's defectExistence gets changed to EXISTS or NONEXISTENT.<br>
     * Otherwise, BadGatewayException is thrown with a message indicating the system cannot be contacted.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the problem to refresh its defect
     * @return the updated problem
     * @throws NotFoundException   if the problem cannot be found
     * @throws BadGatewayException if a problem occurred while joining the defect tracking system
     */
    public ProblemDTO refreshDefectStatus(long projectId, long id) throws BadRequestException {
        final Optional<DefectAdapter> maybeDefectAdapter = defectService.getAdapter(projectId);
        if (!maybeDefectAdapter.isPresent()) {
            throw new BadRequestException(Messages.RULE_PROBLEM_DEFECT_REFRESH_WITHOUT_DEFECT_SYSTEM, Entities.PROBLEM, "no_defect_tracking_system");
        }
        final DefectAdapter defectAdapter = maybeDefectAdapter.get();

        Problem problem = problemRepository.findByProjectIdAndId(projectId, id);
        if (problem == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM, Entities.PROBLEM);
        }

        if (StringUtils.isEmpty(problem.getDefectId())) {
            return problemMapper.toDto(problem);
        }

        try {
            // Change status, but keep other properties and patterns
            final List<Defect> statuses = defectAdapter.getStatuses(projectId, Collections.singletonList(problem.getDefectId()));
            if (statuses.isEmpty()) {
                problem.setDefectExistence(DefectExistence.NONEXISTENT);
                problem.setStatus(ProblemStatus.OPEN); // In case defect was closed and removed: it should be acted on
                problem.setClosingDateTime(null); // Not CLOSED anymore (if it was)
            } else {
                problem.setDefectExistence(DefectExistence.EXISTS);
                problem.setStatus(statuses.get(0).getStatus());
                problem.setClosingDateTime(statuses.get(0).getCloseDateTime());
            }
            return problemMapper.toDto(problemRepository.save(problem));
        } catch (FetchException e) {
            // Also catch RuntimeException to not impact calling code in case of a faulty DefectAdapter in a custom ARA
            log.error("Cannot refresh defect status of problem (defect {})", problem.getDefectId(), e);
            final String message = String.format(Messages.PROCESS_ERROR_WHILE_CONTACTING_DEFECT_TRACKING_SYSTEM,
                    defectAdapter.getName());
            throw new BadGatewayException(message, Entities.PROBLEM);
        }
    }

    /**
     * Recompute the firstSeenDateTime and lastSeenDateTime of all Problems. This should never be necessary, unless an
     * external event modified data in database without using the ARA APIs.
     *
     * @param projectId the ID of the project in which to work
     */
    public void recomputeFirstAndLastSeenDateTimes(long projectId) {
        final List<Problem> problems = problemRepository.findByProjectId(projectId);
        for (int i = 0; i < problems.size(); i++) {
            log.info("Recomputing problem {}/{} ", Integer.valueOf(i + 1), Integer.valueOf(problems.size()));
            problemDenormalizationService.updateFirstAndLastSeenDateTimes(Collections.singleton(problems.get(i)));
        }
    }

    private void assignProblemStabilities(long projectId, List<ProblemWithAggregateDTO> problems) {
        int lastExecutionCount = 10;

        // Given the problem IDs
        List<Long> problemIds = problems.stream().map(ProblemDTO::getId).collect(Collectors.toList());

        for (CycleDefinition cycleDefinition : cycleDefinitionRepository.findAllByProjectIdOrderByBranchPositionAscBranchAscNameAsc(projectId)) {
            List<Execution> lastExecutions = executionRepository
                    .findTop10ByProjectIdAndBranchAndNameOrderByTestDateTimeDesc(
                            cycleDefinition.getProjectId(), cycleDefinition.getBranch(), cycleDefinition.getName());
            List<Long> lastExecutionIds = lastExecutions.stream().map(Execution::getId).collect(Collectors.toList());
            Map<Long, List<Long>> problemIdsToExecutionIds = problemRepository
                    .findProblemIdsToExecutionIdsAssociations(problemIds, lastExecutionIds);

            for (ProblemWithAggregateDTO problem : problems) {
                List<Long> failedExecutionIds = problemIdsToExecutionIds.get(problem.getId());
                CycleStabilityDTO stability = computeStability(cycleDefinition, lastExecutionCount, lastExecutions,
                        failedExecutionIds);
                problem.getAggregate().getCycleStabilities().add(stability);
            }
        }
    }

    /**
     * @param lastExecutionCount the size of the stability bar: the number of latest executions to include in the bar
     * @param lastExecutions     the latest execution IDs (may be fewer that lastExecutionCount, but never more), from the
     *                           newest execution to the oldest
     * @param failedExecutionIds the IDs of the failed executions to pinpoint on the stability bar
     * @return a string of each character being one of the STABILITY_* constants, from the oldest execution to the newest
     */
    private CycleStabilityDTO computeStability(CycleDefinition cycleDefinition, int lastExecutionCount,
                                               List<Execution> lastExecutions, List<Long> failedExecutionIds) {
        return new CycleStabilityDTO()
                .withBranchName(cycleDefinition.getBranch())
                .withCycleName(cycleDefinition.getName())
                .withExecutionStabilities(computeExecutionStability(lastExecutionCount, lastExecutions, failedExecutionIds));
    }

}
