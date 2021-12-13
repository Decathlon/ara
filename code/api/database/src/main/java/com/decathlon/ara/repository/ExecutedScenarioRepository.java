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

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.repository.util.SpecificationUtil;

/**
 * Spring Data JPA repository for the ExecutedScenario entity.
 */
@Repository
public interface ExecutedScenarioRepository extends JpaRepository<ExecutedScenario, Long>, JpaSpecificationExecutor<ExecutedScenario> {

    @Query("SELECT DISTINCT executedScenario.featureName " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.featureName")
    List<String> findDistinctFeatureNameByProjectId(long projectId);

    @Query("SELECT DISTINCT executedScenario.featureFile " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.featureFile")
    List<String> findDistinctFeatureFileByProjectId(long projectId);

    @Query("SELECT DISTINCT executedScenario.name " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.name")
    List<String> findDistinctNameByProjectId(long projectId);

    @Query("SELECT es " +
            "FROM ExecutedScenario es " +
            "WHERE es.run.execution.cycleDefinition.projectId = ?1 " +
            "AND es.id = ?2 ")
    ExecutedScenario findOne(long projectId, long executedScenarioId);

    /**
     * @param runIds the IDs of the Runs where to find ExecutedScenarios
     * @return all executed-scenario of the runs, with minimal information (id, runId, name, severity) and count of errors and problem-patterns
     */
    // NO projectId: runIds is already restrained to the correct project
    @Query("""
            select new com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin(executedScenario.id,
              executedScenario.run.id,
              executedScenario.severity,
              executedScenario.name,
              sum(CASE WHEN error.id IS NOT NULL AND
                (problem.id IS NULL OR
                  (problem.status = 'CLOSED' AND
                    problem.closingDateTime < executedScenario.run.execution.testDateTime))
                 THEN 1
                 ELSE 0
                 END),
              sum(CASE WHEN problem.id IS NOT NULL AND
                  (problem.status = 'OPEN' OR
                    (problem.status = 'CLOSED' AND
                      (problem.closingDateTime IS NULL OR problem.closingDateTime >= executedScenario.run.execution.testDateTime)))
                 THEN 1
                 ELSE 0
                 END))
            from ExecutedScenario executedScenario
            left join executedScenario.errors error
            left join error.problemOccurrences problemOccurrence
            left join problemOccurrence.problemPattern problemPattern
            left join problemPattern.problem problem
            where executedScenario.run.id in (:runIds)
            group by executedScenario.id
            """)
    List<ExecutedScenarioWithErrorAndProblemJoin> findAllErrorAndProblemCounts(@Param("runIds") Set<Long> runIds);

    default List<ExecutedScenario> findHistory(long projectId, String cucumberId, String branch, String cycleName, String countryCode, String runTypeCode, Optional<Period> duration) {
        return findAll(SpecificationUtil.toExecutedScenarioSpecification(projectId, cucumberId, branch, cycleName, countryCode, runTypeCode, duration));
    }

}
