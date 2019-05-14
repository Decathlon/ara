package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.repository.custom.ProblemRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Problem entity.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem>,
        ProblemRepositoryCustom, QuerydslPredicateExecutor<Problem> {

    List<Problem> findByProjectId(long projectId);

    List<Problem> findByProjectIdAndDefectExistenceIsNotAndDefectIdIsNotNull(long projectId, DefectExistence defectExistence);

    Problem findByProjectIdAndId(long projectId, long id);

    Problem findByProjectIdAndName(long projectId, String name);

    Problem findByProjectIdAndDefectId(long projectId, String defectId);

    boolean existsByProjectIdAndBlamedTeam(long projectId, Team blamedTeam);

}
