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

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.repository.custom.ProblemRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Problem entity.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem>,
        ProblemRepositoryCustom, QuerydslPredicateExecutor<Problem> {

    List<Problem> findByProjectId(long projectId);

    List<Problem> findByProjectIdAndDefectExistenceIsNotAndDefectIdIsNotNull(long projectId, DefectExistence defectExistence);

    Problem findByProjectIdAndId(long projectId, long id);

    Problem findByProjectIdAndName(long projectId, String name);

    Problem findByProjectIdAndDefectId(long projectId, String defectId);

    boolean existsByProjectIdAndBlamedTeam(long projectId, Team blamedTeam);

}
