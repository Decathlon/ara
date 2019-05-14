package com.decathlon.ara.repository;

import com.decathlon.ara.repository.custom.FunctionalityRepositoryCustom;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import java.util.List;
import java.util.SortedSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Functionality entity.
 */
@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, Long>, FunctionalityRepositoryCustom {

    Functionality findByProjectIdAndId(long projectId, long id);

    List<Functionality> findAllByProjectIdOrderByOrder(long projectId);

    @EntityGraph("Functionality.scenarios")
    SortedSet<Functionality> findAllByProjectIdAndType(long projectId, FunctionalityType type);

    Functionality findByProjectIdAndNameAndParentId(long projectId, String name, Long parentId);

    List<Functionality> findAllByProjectIdAndParentIdOrderByOrder(long projectId, Long parentId);

    boolean existsByProjectIdAndTeamId(long projectId, long teamId);

}
