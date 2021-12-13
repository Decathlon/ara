/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.projection.CountryCodeCheck;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.domain.projection.ScenarioSummary;

/**
 * Spring Data JPA repository for the Scenario entity.
 */
@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    List<Scenario> findAllBySourceId(Long sourceId);

    boolean existsBySourceId(Long sourceId);

    List<CountryCodeCheck> findDistinctBySourceProjectIdAndCountryCodesContaining(long projectId, String countryCode);

    /**
     * @param projectId the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one scenario has this country code among its {@link Scenario#countryCodes}
     */
    default boolean existsByProjectIdAndCountryCode(long projectId, String countryCode) {
        return findDistinctBySourceProjectIdAndCountryCodesContaining(projectId, countryCode).stream().anyMatch(scenario -> {
            for (String existingCountryCode : scenario.getCountryCodes().split(",")) {
                if (countryCode.equals(existingCountryCode)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return a summary of all ignored scenarios, ordered by source code, feature file and scenario name
     */
    @Query("""
            select new com.decathlon.ara.domain.projection.IgnoredScenario(scenario.source,
              scenario.featureFile, scenario.featureName, scenario.severity, scenario.name)
            from Scenario scenario
            where scenario.source.projectId = ?1 and scenario.ignored = true
            order by scenario.source.code, scenario.featureFile, scenario.name
            """)
    List<IgnoredScenario> findIgnoredScenarios(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @return the count of ignored and not ignored scenarios for each source (API, Web...) and severity
     */
    @Query("""
            select new com.decathlon.ara.domain.projection.ScenarioIgnoreCount(scenario.source, scenario.severity, scenario.ignored, count(1)) from Scenario scenario
            where scenario.source.projectId = ?1
            group by scenario.source, scenario.severity, scenario.ignored
            """)
    List<ScenarioIgnoreCount> findIgnoreCounts(long projectId);

    @Query(value = """
            SELECT new com.decathlon.ara.domain.projection.ScenarioSummary(
             scenario.id,
             scenario.source,
             scenario.featureFile,
             scenario.featureName,
             scenario.name,
             (select count(1) from scenario.functionalities functionalities),
             (CASE WHEN scenario.countryCodes IS NOT NULL AND scenario.countryCodes <> ''
             THEN TRUE
             ELSE FALSE
             END),
             (CASE WHEN scenario.severity IS NOT NULL AND scenario.severity <> ''
             THEN TRUE
             ELSE FALSE
             END),
             scenario.wrongFunctionalityIds,
             scenario.wrongCountryCodes,
             scenario.wrongSeverityCode
            )
            FROM Scenario scenario
            WHERE (select count(1) from scenario.functionalities functionalities) = 0
                   OR (scenario.wrongFunctionalityIds IS NOT NULL AND scenario.wrongFunctionalityIds <> '')
                   OR scenario.countryCodes IS NULL
                   OR scenario.countryCodes = ''
                   OR (scenario.wrongCountryCodes IS NOT NULL AND scenario.wrongCountryCodes <> '')
                   OR scenario.severity IS NULL
                   OR scenario.severity = ''
                   OR (scenario.wrongSeverityCode IS NOT NULL AND scenario.wrongSeverityCode <> '')
              AND scenario.source.projectId = ?1
            ORDER BY scenario.source.code, scenario.featureName, scenario.name, scenario.line
            """)
    public List<ScenarioSummary> findAllWithFunctionalityErrors(long projectId);

}
