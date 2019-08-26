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

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.repository.custom.ExecutedScenarioRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ExecutedScenario entity.
 */
@Repository
public interface ExecutedScenarioRepository extends JpaRepository<ExecutedScenario, Long>, JpaSpecificationExecutor<ExecutedScenario>,
        ExecutedScenarioRepositoryCustom, QuerydslPredicateExecutor<ExecutedScenario> {

    @Query("SELECT DISTINCT executedScenario.featureName " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.featureName")
    List<String> findDistinctFeatureNameByProjectId(long projectId);

    @Query("SELECT DISTINCT executedScenario.featureFile " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.featureFile")
    List<String> findDistinctFeatureFileByProjectId(long projectId);

    @Query("SELECT DISTINCT executedScenario.name " +
            "FROM ExecutedScenario executedScenario " +
            "WHERE executedScenario.run.execution.cycleDefinition.projectId = ?1 " +
            "ORDER BY executedScenario.name")
    List<String> findDistinctNameByProjectId(long projectId);

    @Query("SELECT es " +
            "FROM ExecutedScenario es " +
            "WHERE es.run.execution.cycleDefinition.projectId = ?1 " +
            "AND es.id = ?2 ")
    ExecutedScenario findOne(long projectId, long executedScenarioId);

}
