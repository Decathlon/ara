package com.decathlon.ara.v2.repository;

import com.decathlon.ara.v2.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional(Transactional.TxType.MANDATORY)
@Repository
public interface V2ProjectRepository extends JpaRepository<Project, String> {
}
