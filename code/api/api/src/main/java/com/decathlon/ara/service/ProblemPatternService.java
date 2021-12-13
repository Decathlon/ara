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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemOccurrence;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DeletePatternDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.GenericMapper;

/**
 * Service for managing ProblemPattern.
 */
@Service
@Transactional
public class ProblemPatternService {

    private final ProblemDenormalizationService problemDenormalizationService;

    private final ProblemPatternRepository problemPatternRepository;

    private final ProblemRepository problemRepository;

    private final ErrorService errorService;

    private final CountryRepository countryRepository;

    private final TypeRepository typeRepository;

    private final GenericMapper mapper;

    private final JpaCacheManager jpaCacheManager;

    private final TransactionAppenderUtil transactionService;

    @Autowired
    public ProblemPatternService(ProblemDenormalizationService problemDenormalizationService,
            ProblemPatternRepository problemPatternRepository, ProblemRepository problemRepository,
            @Lazy ErrorService errorService, CountryRepository countryRepository, TypeRepository typeRepository,
            GenericMapper mapper,
            JpaCacheManager jpaCacheManager, TransactionAppenderUtil transactionService) {
        this.problemDenormalizationService = problemDenormalizationService;
        this.problemPatternRepository = problemPatternRepository;
        this.problemRepository = problemRepository;
        this.errorService = errorService;
        this.countryRepository = countryRepository;
        this.typeRepository = typeRepository;
        this.mapper = mapper;
        this.jpaCacheManager = jpaCacheManager;
        this.transactionService = transactionService;
    }

    /**
     * Get one problem pattern by id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the entity
     * @throws NotFoundException if the entity id does not exist
     */
    @Transactional(readOnly = true)
    public ProblemPatternDTO findOne(long projectId, long id) throws NotFoundException {
        ProblemPattern problemPattern = problemPatternRepository.findByProjectIdAndId(projectId, id);
        if (problemPattern == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PROBLEM_PATTERN, Entities.PROBLEM_PATTERN);
        }
        return mapper.map(problemPattern, ProblemPatternDTO.class);
    }

    /**
     * Delete the "id" problem pattern.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @return the source problem if the source problem has been removed (because it now has no pattern)
     * @throws NotFoundException if the entity id does not exist
     */
    public DeletePatternDTO delete(long projectId, long id) throws NotFoundException {
        ProblemPattern pattern = problemPatternRepository.findByProjectIdAndId(projectId, id);
        if (pattern == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PATTERN, Entities.PROBLEM_PATTERN);
        }

        evictErrorProblemPatternsCacheFor(pattern);

        // Detach the pattern from its problem
        Problem sourceProblem = pattern.getProblem();
        sourceProblem.removePattern(pattern);
        sourceProblem = problemRepository.save(sourceProblem);

        // Delete the pattern
        problemPatternRepository.delete(pattern);

        // Delete the source problem if it has no pattern anymore
        DeletePatternDTO response = new DeletePatternDTO();
        if (sourceProblem.getPatterns().isEmpty()) {
            problemRepository.delete(sourceProblem);
            response.setDeletedProblem(mapper.map(sourceProblem, ProblemDTO.class));
        } else {
            problemDenormalizationService.updateFirstAndLastSeenDateTimes(Collections.singleton(sourceProblem));
        }

        return response;
    }

    /**
     * Get all errors associated to the given problem pattern id.
     *
     * @param projectId the ID of the project in which to work
     * @param id        the id of the entity
     * @param pageable  the meta-data of the requested page
     * @return a page of the errors associated to the problem pattern
     * @throws NotFoundException if the entity id does not exist
     */
    @Transactional(readOnly = true)
    public Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> getProblemPatternErrors(long projectId, long id, Pageable pageable) throws NotFoundException {
        ProblemPattern problemPattern = problemPatternRepository.findByProjectIdAndId(projectId, id);
        if (problemPattern == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PATTERN, Entities.PROBLEM_PATTERN);
        }
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(Direction.ASC, "id"));
        for (Order order : pageable.getSort()) {
            orders.add(order);
        }

        return errorService.getErrors(Collections.singletonList(problemPattern), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders)));
    }

    /**
     * Update an existing problem pattern (and re-assign errors).
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate the entity to update
     * @return the updated problem pattern, or empty if the problem pattern was not found in database
     * @throws NotFoundException  if the entity id does not exist
     * @throws NotUniqueException if the same patterns already exist for the same problem
     */
    public ProblemPatternDTO update(long projectId, ProblemPatternDTO dtoToUpdate) throws BadRequestException {
        ProblemPattern problemPattern = problemPatternRepository.findByProjectIdAndId(projectId, dtoToUpdate.getId().longValue());
        if (problemPattern == null) {
            throw new NotFoundException(Messages.NOT_FOUND_PATTERN, Entities.PROBLEM_PATTERN);
        }

        // We update the pattern, but don't change the problem it's associated to, and we remove all assigned errors from the old pattern
        ProblemPattern entityToUpdate = mapper.map(dtoToUpdate, ProblemPattern.class);
        assignExistingEntities(projectId, entityToUpdate);
        entityToUpdate.setProblem(problemPattern.getProblem()); // BEFORE the next Pattern.equals(...)

        // Don't allow two same patterns for the same problem (but allow to update a pattern without modifying it)
        for (ProblemPattern existingPattern : problemPattern.getProblem().getPatterns()) {
            // ProblemPattern.equals(...) uses problemId & all aggregation rule criteria
            // Here, both pattern.problemId are equal, so we check criteria equality inside the same problem
            if (existingPattern.equals(entityToUpdate) && !existingPattern.getId().equals(entityToUpdate.getId())) {
                throw new NotUniqueException(Messages.NOT_UNIQUE_PATTERN_IN_PROBLEM, Entities.PROBLEM_PATTERN, "patterns", existingPattern.getId());
            }
        }

        // Evict errors' cache of the OLD pattern
        evictErrorProblemPatternsCacheFor(problemPattern);

        // Update the pattern: this will remove all occurrences, as the DTO has no error
        problemPattern = problemPatternRepository.save(entityToUpdate);

        // Reassign errors to the new pattern, and update the first and last seen occurrences
        errorService.assignPatternToErrors(projectId, problemPattern); // Also evict errors' cache of the NEW pattern
        problemDenormalizationService.updateFirstAndLastSeenDateTimes(Collections.singleton(problemPattern.getProblem()));

        return mapper.map(problemPattern, ProblemPatternDTO.class);
    }

    /**
     * Schedule an evict-cache action for after commit. The evict action will clear problemPatterns cache of the errors
     * CURRENTLY assigned to the given problem pattern (at the time of the method call).
     *
     * @param problemPattern the problem pattern whose currently-assigned-errors' problemPatterns should be
     *                       cache-evicted after transaction commit
     */
    private void evictErrorProblemPatternsCacheFor(ProblemPattern problemPattern) {
        Set<Long> errorIds = problemPattern.getProblemOccurrences()
                .stream()
                .map(ProblemOccurrence::getError)
                .map(Error::getId)
                .collect(Collectors.toSet());
        transactionService.doAfterCommit(() -> jpaCacheManager.evictCollections(Error.PROBLEM_OCCURRENCES_COLLECTION_CACHE, errorIds));
    }

    void assignExistingEntities(long projectId, ProblemPattern problemPattern) throws NotFoundException {
        assignExistingCountry(projectId, problemPattern);
        assignExistingType(projectId, problemPattern);
    }

    /**
     * Assign the Country entity to this problemPattern, if the problemPattern' country's code is provided.
     *
     * @param projectId      the ID of the project in which to work
     * @param problemPattern the rule on which we want to check country existence
     * @throws NotFoundException if the (optional) country is provided (not null) but the code does not exist in the project
     */
    private void assignExistingCountry(long projectId, ProblemPattern problemPattern) throws NotFoundException {
        if (problemPattern.getCountry() != null) {
            // User provided a code, but we need to know if it is legit in the project context
            final Country country = countryRepository.findByProjectIdAndCode(projectId, problemPattern.getCountry().getCode());
            if (country == null) {
                throw new NotFoundException(Messages.NOT_FOUND_COUNTRY, Entities.COUNTRY);
            }
            problemPattern.setCountry(country);
        }
    }

    /**
     * Assign the Type entity to this problemPattern, if the problemPattern' type's code is provided.
     *
     * @param projectId      the ID of the project in which to work
     * @param problemPattern the rule on which we want to check type existence
     * @throws NotFoundException if the (optional) type is provided (not null) but the code does not exist in the project
     */
    private void assignExistingType(long projectId, ProblemPattern problemPattern) throws NotFoundException {
        if (problemPattern.getType() != null) {
            // User provided a code, but we need to know if it is legit in the project context
            final Type type = typeRepository.findByProjectIdAndCode(projectId, problemPattern.getType().getCode());
            if (type == null) {
                throw new NotFoundException(Messages.NOT_FOUND_TYPE, Entities.TYPE);
            }
            problemPattern.setType(type);
        }
    }

}
