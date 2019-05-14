package com.decathlon.ara.repository;

import com.decathlon.ara.domain.CycleDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CycleDefinition entity.
 */
@Repository
public interface CycleDefinitionRepository extends JpaRepository<CycleDefinition, Long> {

    List<CycleDefinition> findAllByProjectIdOrderByBranchPositionAscBranchAscNameAsc(long projectId);

    CycleDefinition findAllByProjectIdAndId(long projectId, long id);

    List<CycleDefinition> findAllByProjectIdAndBranch(long projectId, String branch);

    CycleDefinition findByProjectIdAndBranchAndName(long projectId, String branch, String name);

}
