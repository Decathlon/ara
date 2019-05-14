package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByName();

    Project findOneByCode(String code);

    Project findOneByName(String name);

    Project findByDefaultAtStartup(boolean defaultAtStartup);

}
