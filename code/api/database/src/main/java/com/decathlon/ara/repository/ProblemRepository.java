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

package com.decathlon.ara.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.filter.ProblemFilter;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import com.decathlon.ara.repository.util.SpecificationUtil;

/**
 * Spring Data JPA repository for the Problem entity.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem> {

    List<Problem> findByProjectId(long projectId);

    List<Problem> findByProjectIdAndDefectExistenceIsNotAndDefectIdIsNotNull(long projectId, DefectExistence defectExistence);

    Problem findByProjectIdAndId(long projectId, long id);

    Problem findByProjectIdAndName(long projectId, String name);

    Problem findByProjectIdAndDefectId(long projectId, String defectId);

    boolean existsByProjectIdAndBlamedTeam(long projectId, Team blamedTeam);

    List<Problem> findAllByProjectIdAndDefectIdIsNotNullAndDefectIdNotNullAndDefectIdNot(long projectId, String defectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return all problems with an assigned defect
     */
    default List<Problem> findAllByProjectIdAndDefectIdIsNotEmpty(long projectId) {
        return findAllByProjectIdAndDefectIdIsNotNullAndDefectIdNotNullAndDefectIdNot(projectId, "").stream().filter(problem -> !problem.getDefectId().isBlank()).toList();
    }

    /**
     * For a list of problems, return the problem IDs with the date and time of their first and last
     * execution.testDateTime error occurrences. Problems without occurrence are not returned in the resulting list.
     *
     * @param problems a list of problems to search  for first and last execution date-times occurrences
     * @return a list of aggregates containing problem ID and first and last occurrence date and times
     */
    @Query("""
            select new com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence(problem.id, min(problemOccurrence.error.executedScenario.run.execution.testDateTime), max(problemOccurrence.error.executedScenario.run.execution.testDateTime))
            from Problem problem
            join problem.patterns problemPattern
            join problemPattern.problemOccurrences problemOccurrence
            where problem in (:problems)
            group by problem.id
            """)
    List<FirstAndLastProblemOccurrence> findFirstAndLastProblemOccurrences(Collection<Problem> problems);

    @Query("""
            select problem.id,
            count(distinct problemPattern),
            count(distinct problemOccurrence.error),
            count(distinct problemOccurrence.error.executedScenario.name),
            min(problemOccurrence.error.executedScenario.name),
            count(distinct problemOccurrence.error.executedScenario.run.execution.branch),
            min(problemOccurrence.error.executedScenario.run.execution.branch),
            count(distinct problemOccurrence.error.executedScenario.run.execution.release),
            min(problemOccurrence.error.executedScenario.run.execution.release),
            count(distinct problemOccurrence.error.executedScenario.run.execution.version),
            min(problemOccurrence.error.executedScenario.run.execution.version),
            count(distinct problemOccurrence.error.executedScenario.run.country),
            min(problemOccurrence.error.executedScenario.run.country.code),
            count(distinct problemOccurrence.error.executedScenario.run.type),
            min(problemOccurrence.error.executedScenario.run.type.code),
            count(distinct problemOccurrence.error.executedScenario.run.platform),
            min(problemOccurrence.error.executedScenario.run.platform)
            from Problem problem
            join problem.patterns problemPattern
            join problemPattern.problemOccurrences problemOccurrence
            where problem.id in (:problemIds)
            group by problem.id
            """)
    List<Object[]> findProblemAggregatesNotFormatted(@Param("problemIds") List<Long> problemIds);

    /**
     * GET all problems matching the given filter.
     *
     * @param filter   the search terms (including the projectId in which to restrain the search)
     * @param pageable the pagination information
     * @return a page of matching problems
     */
    // NO projectId: filter.problemId will be used to restrain to the correct project
    default Page<Problem> findMatchingProblems(ProblemFilter filter, Pageable pageable) {
        return findAll(SpecificationUtil.toProblemSpecification(filter), pageable);
    }

    @Query("""
            select distinct problem.id, occurrence.error.executedScenario.run.execution.id
            from Problem problem
            join problem.patterns pattern
            join pattern.problemOccurrences occurrence
            where occurrence.error.executedScenario.run.execution.id in (:executionIds)
            and problem.id in (:problemIds)
            """)
    List<Object[]> findProblemIdsToExecutionIdsAssociationsNotFormated(@Param("problemIds") List<Long> problemIds, @Param("executionIds") List<Long> executionIds);

    /**
     * @param problemIds   a list of IDs of problems
     * @param executionIds a list of IDs of executions
     * @return for each problem's ID, the list of execution IDs
     */
    // NO projectId: problemIds and executionIds are already restrained to the correct project
    default Map<Long, List<Long>> findProblemIdsToExecutionIdsAssociations(List<Long> problemIds, List<Long> executionIds) {
        List<Object[]> problemExecutionsNotFormated = findProblemIdsToExecutionIdsAssociationsNotFormated(problemIds, executionIds);
        Map<Long, List<Long>> problemExecutionAssoc = new HashMap<>();
        for (Object[] problemExecution : problemExecutionsNotFormated) {
            Long problemId = (Long) problemExecution[0];
            Long executionId = (Long) problemExecution[1];
            List<Long> executionList = problemExecutionAssoc.computeIfAbsent(problemId, key -> new ArrayList<>());
            executionList.add(executionId);
        }
        return problemExecutionAssoc;
    }

}
