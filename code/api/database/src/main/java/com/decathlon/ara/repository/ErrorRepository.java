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

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.repository.util.SpecificationUtil;

/**
 * Spring Data JPA repository for the Error entity.
 */
@Repository
public interface ErrorRepository extends JpaRepository<Error, Long>, JpaSpecificationExecutor<Error> {

    // NO projectId: patterns is already restrained to the correct project
    Page<Error> findDistinctByProblemOccurrencesProblemPatternIn(List<ProblemPattern> patterns, Pageable pageable);

    @Query("SELECT error " +
            "FROM Error error " +
            "WHERE error.executedScenario.run.execution.cycleDefinition.projectId = ?1 AND error.id = ?2")
    Error findByProjectIdAndId(long projectId, long id);

    @Query("SELECT DISTINCT error.step " +
            "FROM Error error " +
            "WHERE error.executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY error.step")
    List<String> findDistinctStepByProjectId(long projectId);

    @Query("SELECT DISTINCT error.stepDefinition " +
            "FROM Error error " +
            "WHERE error.executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY error.stepDefinition")
    List<String> findDistinctStepDefinitionByProjectId(long projectId);

    default Page<Error> findByProjectIdAndProblemPattern(@Param("projectId") long projectId, @Param("pattern") ProblemPattern problemPattern, Pageable pageable) {
        return findAll(SpecificationUtil.toErrorSpecification(projectId, problemPattern, null), pageable);
    }

    default List<Error> findByProjectIdAndProblemPatternAndErrorIds(@Param("projectId") long projectId, @Param("pattern") ProblemPattern problemPattern, @Param("errorIds") List<Long> errorIds) {
        return findAll(SpecificationUtil.toErrorSpecification(projectId, problemPattern, errorIds));
    }

    @Query("""
            select problemOccurrence.error, problem from Problem problem
            join problem.patterns pattern
            join pattern.problemOccurrences problemOccurrence
            where problemOccurrence.error.id in (:errorIds)
            """)
    List<Object[]> getErrorsProblemsNotFormated(@Param("errorIds") List<Long> errorIds);

    default Map<Error, List<Problem>> getErrorsProblems(List<Error> errors) {
        List<Object[]> errorProblemsNotFormated = getErrorsProblemsNotFormated(errors.stream().map(Error::getId).toList());
        Map<Error, List<Problem>> errorProblems = new HashMap<>();
        for (Object[] errorProblem : errorProblemsNotFormated) {
            Error error = (Error) errorProblem[0];
            Problem problem = (Problem) errorProblem[1];
            List<Problem> problemList = errorProblems.computeIfAbsent(error, key -> new ArrayList<>());
            problemList.add(problem);
        }
        return errorProblems;
    }

}
