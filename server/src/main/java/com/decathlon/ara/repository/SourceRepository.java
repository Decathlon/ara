package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Source;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Source entity.
 */
@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findAllByProjectIdOrderByName(long projectId);

    Source findByProjectIdAndCode(long projectId, String code);

    Source findByProjectIdAndName(long projectId, String name);

    Source findByProjectIdAndLetter(long projectId, char letter);

}
