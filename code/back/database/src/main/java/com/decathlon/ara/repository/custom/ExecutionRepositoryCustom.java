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

package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Execution;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExecutionRepositoryCustom {

    List<Execution> findTop10ByProjectIdAndBranchAndNameOrderByTestDateTimeDesc(long projectId, String branch, String name);

    List<Execution> findLatestOfEachCycleByProjectId(long projectId);

    // NO projectId: referenceExecutions is already restrained to the correct project
    List<Execution> findNextOf(Collection<Execution> referenceExecutions);

    // NO projectId: referenceExecutions is already restrained to the correct project
    List<Execution> findPreviousOf(Collection<Execution> referenceExecutions);

    List<Execution> getLatestEligibleVersionsByProjectId(long projectId);

}
