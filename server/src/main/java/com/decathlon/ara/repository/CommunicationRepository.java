package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Communication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Communication entity.
 */
@Repository
public interface CommunicationRepository extends JpaRepository<Communication, Long> {

    List<Communication> findAllByProjectIdOrderByCode(long projectId);

    Communication findByProjectIdAndCode(long projectId, String code);

}
