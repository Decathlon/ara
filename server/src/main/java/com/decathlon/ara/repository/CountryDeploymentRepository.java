package com.decathlon.ara.repository;

import com.decathlon.ara.domain.CountryDeployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CountryDeployment entity.
 */
@Repository
public interface CountryDeploymentRepository extends JpaRepository<CountryDeployment, String> {

    boolean existsByCountryId(long countryId);

}
