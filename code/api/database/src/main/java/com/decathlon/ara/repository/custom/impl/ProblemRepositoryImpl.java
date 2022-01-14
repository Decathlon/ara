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

package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import com.decathlon.ara.domain.projection.ProblemAggregate;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.repository.custom.ProblemRepositoryCustom;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProblemRepositoryImpl implements ProblemRepositoryCustom {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    /**
     * @param execution an execution
     * @return the distinct number of problems associated to errors attached to the given execution
     */
    @Override
    public long countProblemsOfExecution(Execution execution) {
        return jpaQueryFactory.selectDistinct(QProblem.problem.id)
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.problemOccurrences, QProblemOccurrence.problemOccurrence)
                .join(QProblemOccurrence.problemOccurrence.error, QError.error)
                .where(QError.error.executedScenario.run.execution.id.eq(execution.getId()))
                .fetchCount();
    }

    /**
     * @param problemIds  a list of IDs of problems
     * @param executionIds a list of IDs of executions
     * @return for each problem's ID, the list of execution IDs
     */
    @Override
    public Map<Long, List<Long>> findProblemIdsToExecutionIdsAssociations(List<Long> problemIds, List<Long> executionIds) {
        List<Tuple> tuples = jpaQueryFactory.selectDistinct(QProblem.problem.id, QError.error.executedScenario.run.execution.id)
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.problemOccurrences, QProblemOccurrence.problemOccurrence)
                .join(QProblemOccurrence.problemOccurrence.error, QError.error)
                .where(QProblem.problem.id.in(problemIds))
                .where(QError.error.executedScenario.run.execution.id.in(executionIds))
                .fetch();

        return tuples.stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(QProblem.problem.id),
                        Collectors.mapping(tuple -> tuple.get(QError.error.executedScenario.run.execution.id),
                                Collectors.toList())));
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param problemIds a list of IDs of problems
     * @return for each problem ID, an aggregate object listing various counts and statistics about this problem
     */
    @Override
    public Map<Long, ProblemAggregate> findProblemAggregates(long projectId, List<Long> problemIds) {
        QueryResults<Tuple> results = jpaQueryFactory.select(
                QProblem.problem.id,

                QProblemPattern.problemPattern.countDistinct(),
                QError.error.countDistinct(),

                QError.error.executedScenario.name.countDistinct(),
                QError.error.executedScenario.name.min(),

                QExecution.execution.branch.countDistinct(),
                QExecution.execution.branch.min(),

                QExecution.execution.release.countDistinct(),
                QExecution.execution.release.min(),

                QExecution.execution.version.countDistinct(),
                QExecution.execution.version.min(),

                QRun.run.country.countDistinct(),
                QRun.run.country.code.min(),

                QRun.run.type.countDistinct(),
                QRun.run.type.code.min(),

                QRun.run.platform.countDistinct(),
                QRun.run.platform.min()
        )
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.problemOccurrences, QProblemOccurrence.problemOccurrence)
                .join(QProblemOccurrence.problemOccurrence.error, QError.error)
                .join(QError.error.executedScenario.run, QRun.run)
                .join(QRun.run.execution, QExecution.execution)
                .where(QProblem.problem.id.in(problemIds))
                .groupBy(QProblem.problem.id)
                .fetchResults();

        Map<Long, ProblemAggregate> aggregates = new HashMap<>();

        for (Tuple result : results.getResults()) {
            ProblemAggregate aggregate = new ProblemAggregate();
            aggregates.put(result.get(QProblem.problem.id), aggregate);

            aggregate.setPatternCount(result.get(QProblemPattern.problemPattern.countDistinct()).longValue());
            aggregate.setErrorCount(result.get(QError.error.countDistinct()).longValue());

            aggregate.setScenarioCount(result.get(QError.error.executedScenario.name.countDistinct()).longValue());
            aggregate.setFirstScenarioName(result.get(QError.error.executedScenario.name.min()));

            aggregate.setBranchCount(result.get(QExecution.execution.branch.countDistinct()).longValue());
            aggregate.setFirstBranch(result.get(QExecution.execution.branch.min()));

            aggregate.setReleaseCount(result.get(QExecution.execution.release.countDistinct()).longValue());
            aggregate.setFirstRelease(result.get(QExecution.execution.release.min()));

            aggregate.setVersionCount(result.get(QExecution.execution.version.countDistinct()).longValue());
            aggregate.setFirstVersion(result.get(QExecution.execution.version.min()));

            aggregate.setCountryCount(result.get(QRun.run.country.countDistinct()).longValue());
            aggregate.setFirstCountry(countryRepository.findByProjectIdAndCode(projectId, result.get(QRun.run.country.code.min())));

            aggregate.setTypeCount(result.get(QRun.run.type.countDistinct()).longValue());
            aggregate.setFirstType(typeRepository.findByProjectIdAndCode(projectId, result.get(QRun.run.type.code.min())));

            aggregate.setPlatformCount(result.get(QRun.run.platform.countDistinct()).longValue());
            aggregate.setFirstPlatform(result.get(QRun.run.platform.min()));
        }

        return aggregates;
    }

    /**
     * For a list of problems, return the problem IDs with the date and time of their first and last
     * execution.testDateTime error occurrences. Problems without occurrence are not returned in the resulting list.
     *
     * @param problems a list of problems to search  for first and last execution date-times occurrences
     * @return a list of aggregates containing problem ID and first and last occurrence date and times
     */
    @Override
    public List<FirstAndLastProblemOccurrence> findFirstAndLastProblemOccurrences(Collection<Problem> problems) {
        return jpaQueryFactory
                .select(Projections.constructor(FirstAndLastProblemOccurrence.class,
                        QProblem.problem.id,
                        QExecution.execution.testDateTime.min(),
                        QExecution.execution.testDateTime.max()))
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.problemOccurrences, QProblemOccurrence.problemOccurrence)
                .join(QProblemOccurrence.problemOccurrence.error, QError.error)
                .join(QError.error.executedScenario.run, QRun.run)
                .join(QRun.run.execution, QExecution.execution)
                .where(QProblem.problem.id.in(problems.stream()
                        .map(Problem::getId)
                        .collect(Collectors.toSet())))
                .groupBy(QProblem.problem.id)
                .fetch();
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return all problems with an assigned defect
     */
    @Override
    public List<Problem> findAllByProjectIdAndDefectIdIsNotEmpty(long projectId) {
        return jpaQueryFactory
                .select(QProblem.problem)
                .from(QProblem.problem)
                .where(QProblem.problem.projectId.eq(Long.valueOf(projectId)))
                .where(QProblem.problem.defectId.isNotEmpty())
                .fetch()
                .stream()
                .filter(p -> StringUtils.isNotBlank(p.getDefectId()))
                .collect(Collectors.toList());
    }

}
