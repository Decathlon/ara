package com.decathlon.ara.repository;

import com.decathlon.ara.repository.custom.ExecutionRepositoryCustom;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.enumeration.JobStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Execution entity.
 */
@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long>, ExecutionRepositoryCustom {

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
    Execution findByProjectIdAndJobUrlOrJobLink(long projectId, String jobUrl, String jobLink);

    boolean existsByCycleDefinitionId(Long id);

}
