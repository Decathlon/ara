package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Scenario;
import java.util.Map;

public interface FunctionalityRepositoryCustom {

    Map<Long, Long> getFunctionalityTeamIds(long projectId);

    /**
     * @param projectId the ID of the project in which to work
     * @param countryCode a {@link Country#code}
     * @return true if and only if at least one functionality has this country code among its {@link Scenario#countryCodes}
     */
    boolean existsByProjectIdAndCountryCode(long projectId, String countryCode);

}
