package com.decathlon.ara.v2.repository;

import com.decathlon.ara.v2.domain.ScenarioVersion;
import com.decathlon.ara.v2.domain.id.ScenarioVersionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface V2ScenarioVersionRepository extends JpaRepository<ScenarioVersion, ScenarioVersionId> {
}
