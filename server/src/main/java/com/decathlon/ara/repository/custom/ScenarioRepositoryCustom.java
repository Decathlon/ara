package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;
import java.util.List;

public interface ScenarioRepositoryCustom {

    /**
     * @param projectId the ID of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    List<ScenarioSummary> findAllWithFunctionalityErrors(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return the count of ignored and not ignored scenarios for each source (API, Web...) and severity
     */
    List<ScenarioIgnoreCount> findIgnoreCounts(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return a summary of all ignored scenarios, ordered by source code, feature file and scenario name
     */
    List<IgnoredScenario> findIgnoredScenarios(long projectId);

    /**
     * @param projectId   the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one scenario has this country code among its {@link Scenario#countryCodes}
     */
    boolean existsByProjectIdAndCountryCode(long projectId, String countryCode);

}
