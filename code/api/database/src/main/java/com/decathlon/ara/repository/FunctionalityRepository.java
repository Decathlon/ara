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
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.domain.projection.CountryCodeCheck;
import com.decathlon.ara.domain.projection.FunctionalityTeam;

/**
 * Spring Data JPA repository for the Functionality entity.
 */
@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, Long> {

    Optional<Functionality> findByProjectIdAndId(Long projectId, Long id);

    List<Functionality> findByProjectIdAndIdIn(Long projectId, List<Long> ids);

    List<Functionality> findAllByProjectIdOrderByOrder(long projectId);

    @EntityGraph("Functionality.scenarios")
    SortedSet<Functionality> findAllByProjectIdAndType(long projectId, FunctionalityType type);

    Functionality findByProjectIdAndNameAndParentId(long projectId, String name, Long parentId);

    List<Functionality> findAllByProjectIdAndParentIdOrderByOrder(long projectId, Long parentId);

    boolean existsByProjectIdAndTeamId(long projectId, long teamId);

    List<CountryCodeCheck> findDistinctByProjectIdAndCountryCodesContaining(long projectId, String countryCode);

    default boolean existsByProjectIdAndCountryCode(long projectId, String countryCode) {
        return findDistinctByProjectIdAndCountryCodesContaining(projectId, countryCode).stream().anyMatch(functionality -> {
            for (String existingCountryCode : functionality.getCountryCodes().split(",")) {
                if (countryCode.equals(existingCountryCode)) {
                    return true;
                }
            }
            return false;
        });
    }

    List<FunctionalityTeam> findAllByTypeAndProjectId(FunctionalityType type, long projectId);

    default Map<Long, Long> getFunctionalityTeamIds(long projectId) {
        return findAllByTypeAndProjectId(FunctionalityType.FUNCTIONALITY, projectId).stream().collect(Collectors.toMap(FunctionalityTeam::getId, FunctionalityTeam::getTeamId));
    }

}
