package com.decathlon.ara.repository;

import com.decathlon.ara.repository.custom.ErrorRepositoryCustom;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Error entity.
 */
@Repository
public interface ErrorRepository extends JpaRepository<Error, Long>, JpaSpecificationExecutor<Error>, ErrorRepositoryCustom,
        QuerydslPredicateExecutor<Error> {

    // NO projectId: patterns is already restrained to the correct project
    Page<Error> findDistinctByProblemPatternsInOrderById(List<ProblemPattern> patterns, Pageable pageable);

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
