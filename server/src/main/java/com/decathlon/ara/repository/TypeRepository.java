package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Type;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Type entity.
 */
@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

    List<Type> findAllByProjectIdOrderByCode(long projectId);

    Type findByProjectIdAndCode(long projectId, String code);

    Type findByProjectIdAndName(long projectId, String name);

    boolean existsByProjectIdAndSourceId(long projectId, long sourceId);

}
