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

import com.decathlon.ara.domain.CycleDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CycleDefinition entity.
 */
@Repository
public interface CycleDefinitionRepository extends JpaRepository<CycleDefinition, Long> {

    List<CycleDefinition> findAllByProjectIdOrderByBranchPositionAscBranchAscNameAsc(long projectId);

    CycleDefinition findAllByProjectIdAndId(long projectId, long id);

    List<CycleDefinition> findAllByProjectIdAndBranch(long projectId, String branch);

    CycleDefinition findByProjectIdAndBranchAndName(long projectId, String branch, String name);

}
