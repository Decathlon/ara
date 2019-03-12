package com.decathlon.ara.repository;

import com.decathlon.ara.domain.ProblemPattern;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProblemPattern entity.
 */
@Repository
public interface ProblemPatternRepository extends JpaRepository<ProblemPattern, Long> {

    @Query("SELECT problemPattern " +
            "FROM ProblemPattern problemPattern " +
            "WHERE problemPattern.problem.projectId = ?1")
    List<ProblemPattern> findAllByProjectId(long projectId);

    @Query("SELECT problemPattern " +
            "FROM ProblemPattern problemPattern " +
            "WHERE problemPattern.problem.projectId = ?1 " +
            "AND problemPattern.id = ?2")
    ProblemPattern findByProjectIdAndId(long projectId, long id);

    boolean existsByCountryId(long countryId);

    boolean existsByTypeId(long typeId);

}
