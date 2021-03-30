package com.decathlon.ara.v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface V2RepositoryRepository extends JpaRepository<com.decathlon.ara.v2.domain.Repository, Long> {
}
