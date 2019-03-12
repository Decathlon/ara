package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Severity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Severity entity.
 */
@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {

    /**
     * @param projectId the ID of the project in which to work
     * @return all severities of the project ordered by position
     */
    List<Severity> findAllByProjectIdOrderByPosition(long projectId);

    Severity findByProjectIdAndCode(long projectId, String code);

    Severity findByProjectIdAndName(long projectId, String name);

    Severity findByProjectIdAndShortName(long projectId, String shortName);

    Severity findByProjectIdAndInitials(long projectId, String initials);

    Severity findByProjectIdAndPosition(long projectId, int position);

    Severity findByProjectIdAndDefaultOnMissing(long projectId, boolean defaultOnMissing);

}
