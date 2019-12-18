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

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.repository.custom.ErrorRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Error entity.
 */
@Repository
public interface ErrorRepository extends JpaRepository<Error, Long>, JpaSpecificationExecutor<Error>, ErrorRepositoryCustom,
        QuerydslPredicateExecutor<Error> {

    // NO projectId: patterns is already restrained to the correct project
    Page<Error> findDistinctByProblemPatternsInOrderById(List<ProblemPattern> patterns, Pageable pageable);

    /**
     * Find errors problems paginated and ordered by their date time (descending)
     * @param patterns the problem patterns, should not be null
     * @param pageable the pagination details, must not be null
     * @return the errors. If none found, an empty page is returned
     */
    Page<Error> findDistinctByProblemPatternsInOrderByExecutedScenarioRunExecutionTestDateTimeDesc(List<ProblemPattern> patterns, Pageable pageable);

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

}
