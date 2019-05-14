package com.decathlon.ara.repository;

import com.decathlon.ara.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Country entity.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findAllByProjectIdOrderByCode(long projectId);

    Country findByProjectIdAndCode(long projectId, String code);

    Country findByProjectIdAndName(long projectId, String name);

    @Query("SELECT country.code FROM Country country WHERE country.projectId = ?1")
    List<String> findCodesByProjectId(long projectId);

}
