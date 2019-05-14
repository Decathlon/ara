package com.decathlon.ara.repository;

import com.decathlon.ara.domain.RootCause;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RootCause entity.
 */
@Repository
public interface RootCauseRepository extends JpaRepository<RootCause, Long> {

    List<RootCause> findAllByProjectIdOrderByName(long projectId);

    RootCause findByProjectIdAndId(long projectId, long id);

    RootCause findByProjectIdAndName(long projectId, String name);

}
