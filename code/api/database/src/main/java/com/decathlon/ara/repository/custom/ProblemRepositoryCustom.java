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
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.filter.ProblemFilter;
import com.decathlon.ara.domain.projection.ProblemAggregate;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemRepositoryCustom {

    /**
     * @param execution an execution
     * @return the distinct number of problems associated to errors attached to the given execution
     */
    // NO projectId: execution is already restrained to the correct project
    long countProblemsOfExecution(Execution execution);

    /**
     * @param problemIds   a list of IDs of problems
     * @param executionIds a list of IDs of executions
     * @return for each problem's ID, the list of execution IDs
     */
    // NO projectId: problemIds and executionIds are already restrained to the correct project
    Map<Long, List<Long>> findProblemIdsToExecutionIdsAssociations(List<Long> problemIds, List<Long> executionIds);

    /**
     * @param projectId  the ID of the project in which to work
     * @param problemIds a list of IDs of problems
     * @return for each problem ID, an aggregate object listing various counts and statistics about this problem
     */
    // NO projectId: problemIds are already restrained to the correct project
    Map<Long, ProblemAggregate> findProblemAggregates(long projectId, List<Long> problemIds);

    /**
     * For a list of problems, return the problem IDs with the date and time of their first and last
     * execution.testDateTime error occurrences. Problems without occurrence are not returned in the resulting list.
     *
     * @param problems a list of problems to search  for first and last execution date-times occurrences
     * @return a list of aggregates containing problem ID and first and last occurrence date and times
     */
    List<FirstAndLastProblemOccurrence> findFirstAndLastProblemOccurrences(Collection<Problem> problems);

    /**
     * @param projectId the ID of the project in which to work
     * @return all problems with an assigned defect
     */
    List<Problem> findAllByProjectIdAndDefectIdIsNotEmpty(long projectId);

}
