package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.repository.custom.ScenarioRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Scenario entity.
 */
@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long>, ScenarioRepositoryCustom {

    List<Scenario> findAllBySourceId(Long sourceId);

    boolean existsBySourceId(Long sourceId);

}
