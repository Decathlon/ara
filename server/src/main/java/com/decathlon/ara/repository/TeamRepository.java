package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Team entity.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByProjectIdOrderByName(long projectId);

    Team findByProjectIdAndId(long projectId, long id);

    Team findByProjectIdAndName(long projectId, String name);

}
