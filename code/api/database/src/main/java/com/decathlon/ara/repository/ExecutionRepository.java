/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.enumeration.JobStatus;

/**
 * Spring Data JPA repository for the Execution entity.
 */
@Repository
@Transactional(readOnly = true)
public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    @Query("SELECT execution " +
            "FROM Execution execution " +
            "WHERE execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY execution.testDateTime DESC")
    Page<Execution> findAllByProjectIdOrderByTestDateTimeDesc(long projectId, Pageable pageable);

    @Query("SELECT DISTINCT execution.release " +
            "FROM Execution execution " +
            "WHERE execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY execution.release")
    List<String> findDistinctReleaseByProjectId(long projectId);

    @Query("SELECT execution.jobUrl FROM Execution execution WHERE execution.status = ?1 AND execution.jobUrl IN ?2")
    List<String> findJobUrls(JobStatus jobStatus, Collection<String> jobUrls);

    @Query("SELECT execution.jobLink FROM Execution execution WHERE execution.status = ?1 AND execution.jobLink IN ?2")
    List<String> findJobLinks(JobStatus jobStatus, Collection<String> jobLinks);

    @Query("SELECT execution " +
            "FROM Execution execution " +
            "WHERE execution.cycleDefinition.projectId = ?1 AND execution.id = ?2")
    Execution findByProjectIdAndId(long projectId, long id);

    @Query("SELECT execution " +
            "FROM Execution execution " +
            "WHERE execution.cycleDefinition.projectId = ?1 AND execution.jobUrl = ?2")
    Execution findByProjectIdAndJobUrl(long projectId, String jobUrl);

    @Query("SELECT execution " +
            "FROM Execution execution " +
            "WHERE execution.cycleDefinition.projectId = ?1 AND " +
            "      ((execution.jobUrl IS NOT NULL AND execution.jobUrl = ?2) OR " +
            "       (execution.jobLink IS NOT NULL AND execution.jobLink = ?3))")
    Optional<Execution> findByProjectIdAndJobUrlOrJobLink(Long projectId, String jobUrl, String jobLink);

    Optional<Execution> findByCycleDefinitionProjectIdAndJobLinkAndJobLinkNotNull(Long projectId, String jobLink);

    boolean existsByCycleDefinitionId(Long id);

    List<Execution> findByCycleDefinitionProjectIdAndTestDateTimeBefore(long projectId, Date startDate);

    List<Execution> findTop10ByCycleDefinitionProjectIdAndCycleDefinitionBranchAndCycleDefinitionNameOrderByTestDateTimeDesc(long projectId, String branch, String name);

    @Query("""
            select execution from Execution execution
            where execution.cycleDefinition.projectId = :projectId
              and execution.status = 'DONE'
              and execution.acceptance <> 'DISCARDED'
              and execution.blockingValidation = true
              and execution.qualityStatus in ('PASSED', 'WARNING')
              and (execution.cycleDefinition.branch, execution.testDateTime) in (select lastExecution.cycleDefinition.branch, max(lastExecution.testDateTime) from Execution lastExecution
                where execution.cycleDefinition.projectId = :projectId
                  and lastExecution.status = 'DONE'
                  and lastExecution.acceptance <> 'DISCARDED'
                  and lastExecution.blockingValidation = true
                  and lastExecution.qualityStatus in ('PASSED', 'WARNING')
                group by lastExecution.cycleDefinition.branch)
              order by execution.branch
            """)
    List<Execution> getLatestEligibleVersionsByProjectId(@Param("projectId") long projectId);

    @Query("""
            select execution from Execution execution
            where (execution.cycleDefinition.id, execution.testDateTime) in (select execution.cycleDefinition.id, max(execution.testDateTime) from Execution execution, Execution currentExecution
              where currentExecution.cycleDefinition.id = execution.cycleDefinition.id
              and currentExecution.testDateTime > execution.testDateTime
              and currentExecution.id in (:executionIds)
              group by execution.cycleDefinition.id)
            """)
    List<Execution> findPreviousOfNotSorted(@Param("executionIds") List<Long> executionIds);

    default List<Execution> findPreviousOf(@Param("executionIds") List<Long> executionIds){
        return sortExecutionByCycleDefinition(findPreviousOfNotSorted(executionIds));
    }

    @Query("""
            select execution from Execution execution
            where (execution.cycleDefinition.id, execution.testDateTime) in (select execution.cycleDefinition.id, min(execution.testDateTime) from Execution execution, Execution currentExecution
              where currentExecution.cycleDefinition.id = execution.cycleDefinition.id
              and currentExecution.testDateTime < execution.testDateTime
              and currentExecution.id in (:executionIds)
              group by execution.cycleDefinition.id)
            """)
    List<Execution> findNextOfNotSorted(@Param("executionIds") List<Long> executionIds);

    default List<Execution> findNextOf(@Param("executionIds") List<Long> executionIds){
        return sortExecutionByCycleDefinition(findNextOfNotSorted(executionIds));
    }

    @Query("""
            select execution from Execution execution
            where (execution.cycleDefinition.id, execution.testDateTime) in (select execution.cycleDefinition.id, max(execution.testDateTime) from Execution execution
              where execution.cycleDefinition.projectId = :projectId
              and execution.status = 'DONE'
              and execution.acceptance <> 'DISCARDED'
              group by execution.cycleDefinition.id)
            """)
    List<Execution> findLatestOfEachCycleByProjectIdNotSorted(@Param("projectId") long projectId);

    default List<Execution> findLatestOfEachCycleByProjectId(@Param("projectId") long projectId){
        return sortExecutionByCycleDefinition(findLatestOfEachCycleByProjectIdNotSorted(projectId));
    }

    default List<Execution> sortExecutionByCycleDefinition(List<Execution> executions) {
        executions.sort((execution1, execution2) -> {
            Comparator<Execution> cycleDefinitionProjectIdComparator = Comparator.comparing(c -> Long.valueOf(c.getCycleDefinition().getProjectId()), Comparator.nullsFirst(Comparator.naturalOrder()));
            Comparator<Execution> cycleDefinitionBranchPositionComparator = Comparator.comparing(c -> Integer.valueOf(c.getCycleDefinition().getBranchPosition()), Comparator.nullsFirst(Comparator.naturalOrder()));
            Comparator<Execution> cycleDefinitionBranchComparator = Comparator.comparing(c -> c.getCycleDefinition().getBranch(), Comparator.nullsFirst(Comparator.naturalOrder()));
            Comparator<Execution> cycleDefinitionNameComparator = Comparator.comparing(c -> c.getCycleDefinition().getName(), Comparator.nullsFirst(Comparator.naturalOrder()));
            return Comparator.nullsFirst(cycleDefinitionProjectIdComparator
                    .thenComparing(cycleDefinitionBranchPositionComparator)
                    .thenComparing(cycleDefinitionBranchComparator)
                    .thenComparing(cycleDefinitionNameComparator)).compare(execution1, execution2);
        });
        return executions;
    }
}
