package com.decathlon.ara.repository;

import com.decathlon.ara.domain.ExecutionCompletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ExecutionCompletionRequest entity.
 */
@Repository
public interface ExecutionCompletionRequestRepository extends JpaRepository<ExecutionCompletionRequest, String> {

    // Everything is done by Spring when it implements the JpaRepository

}
