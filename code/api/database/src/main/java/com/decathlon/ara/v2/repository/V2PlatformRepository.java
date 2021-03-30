package com.decathlon.ara.v2.repository;

import com.decathlon.ara.v2.domain.JobPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface V2PlatformRepository extends JpaRepository<JobPlatform, Long> {
}
