package com.decathlon.ara.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decathlon.ara.domain.ProblemOccurrence;

public interface ProblemOccurrenceRepository extends JpaRepository<ProblemOccurrence, ProblemOccurrence.ProblemOccurrenceId> {

}
