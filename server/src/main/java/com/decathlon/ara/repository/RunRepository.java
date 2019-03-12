package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Run entity.
 */
@Repository
public interface RunRepository extends JpaRepository<Run, Long> {

    boolean existsByCountryId(long countryId);

    @Query("SELECT DISTINCT run.country " +
            "FROM Run run " +
            "WHERE run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY run.country.name")
    List<Country> findDistinctCountryByProjectId(long projectId);

    @Query("SELECT DISTINCT run.type " +
            "FROM Run run " +
            "WHERE run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY run.type.name")
    List<Type> findDistinctTypeByProjectId(long projectId);

    @Query("SELECT DISTINCT run.platform " +
            "FROM Run run " +
            "WHERE run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY run.platform")
    List<String> findDistinctPlatformByProjectId(long projectId);

    boolean existsByTypeId(long typeId);

}
