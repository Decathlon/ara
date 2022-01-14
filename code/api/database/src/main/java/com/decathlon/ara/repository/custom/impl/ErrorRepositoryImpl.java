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

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.custom.ErrorRepositoryCustom;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ErrorRepositoryImpl implements ErrorRepositoryCustom {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private SQLQueryFactory sqlQueryFactory;

    @Autowired
    private ProblemPatternRepository problemPatternRepository;

    @Autowired
    private JpaCacheManager jpaCacheManager;

    @Autowired
    private TransactionAppenderUtil transactionAppenderUtil;

    @Override
    public void assignPatternToErrors(long projectId, ProblemPattern pattern) {
        // If the pattern has just been inserted by Hibernate, issue the real SQL command, because we will issue direct-SQL queries with F.K.
        entityManager.flush();

        List<Long> matchingErrorIds = jpaQueryFactory.select(QError.error.id)
                .from(QError.error)
                .where(QError.error.toFilterPredicate(projectId, pattern)).fetch();

        SProblemOccurrence problemOccurrence = SProblemOccurrence.problemOccurrence;
        SQLInsertClause insert = sqlQueryFactory.insert(problemOccurrence);

        for (Long errorId : matchingErrorIds) {
            insert
                    .set(problemOccurrence.errorId, errorId)
                    .set(problemOccurrence.problemPatternId, pattern.getId())
                    .addBatch();
        }

        transactionAppenderUtil.doAfterCommit(() ->
                jpaCacheManager.evictCollections(Error.PROBLEM_OCCURRENCES_COLLECTION_CACHE, matchingErrorIds));

        long insertedRows = (insert.getBatchCount() > 0 ? insert.execute() : 0);

        log.info("PROBLEM|error|Inserted {} problemOccurrences", Long.valueOf(insertedRows));
    }

    @Override
    public Map<Error, List<Problem>> getErrorsProblems(Collection<Error> errors) {
        List<Tuple> tuples = jpaQueryFactory.select(QError.error.id, QProblem.problem)
                .distinct()
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.problemOccurrences, QProblemOccurrence.problemOccurrence)
                .join(QProblemOccurrence.problemOccurrence.error, QError.error)
                .where(QError.error.id.in(errors.stream().map(Error::getId).collect(Collectors.toSet())))
                .fetch();

        return tuples.stream()
                .collect(Collectors.groupingBy(tuple -> {
                    Long errorId = tuple.get(QError.error.id);
                    return errors.stream().filter(error -> error.getId().equals(errorId)).findFirst().get();
                }, Collectors.mapping(tuple -> tuple.get(QProblem.problem), Collectors.toList())));
    }

    /**
     * When new errors get indexed into ARA, this method will assign them existing problems if at least one of the
     * problems's patterns match the errors.
     *
     * @param projectId the ID of the project in which to work
     * @param errorIds  the IDs of the new errors that were just created
     * @return all problems that were assigned one of the given new errors by this method
     */
    @Override
    public Set<Problem> autoAssignProblemsToNewErrors(long projectId, List<Long> errorIds) {
        SProblemOccurrence problemOccurrence = SProblemOccurrence.problemOccurrence;
        SQLInsertClause insert = sqlQueryFactory.insert(problemOccurrence);

        Set<Problem> updatedProblems = new HashSet<>();

        for (ProblemPattern pattern : problemPatternRepository.findAllByProjectId(projectId)) {
            List<Long> matchingErrorIds = jpaQueryFactory.select(QError.error.id)
                    .from(QError.error)
                    .where(QError.error.id.in(errorIds))
                    .where(QError.error.toFilterPredicate(projectId, pattern)).fetch();

            if (!matchingErrorIds.isEmpty()) {
                updatedProblems.add(pattern.getProblem());

                for (Long errorId : matchingErrorIds) {
                    insert
                            .set(problemOccurrence.errorId, errorId)
                            .set(problemOccurrence.problemPatternId, pattern.getId())
                            .addBatch();
                }
            }
        }

        long insertedRows = (insert.getBatchCount() > 0 ? insert.execute() : 0);

        log.info("PROBLEM|error|Inserted {} problemOccurrences", Long.valueOf(insertedRows));

        return updatedProblems;
    }

}
