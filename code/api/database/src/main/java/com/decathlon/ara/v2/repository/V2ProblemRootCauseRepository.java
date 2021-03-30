package com.decathlon.ara.v2.repository;

import com.decathlon.ara.v2.domain.ProblemRootCause;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface V2ProblemRootCauseRepository extends JpaRepository<ProblemRootCause, CodeWithProjectId> {
}
