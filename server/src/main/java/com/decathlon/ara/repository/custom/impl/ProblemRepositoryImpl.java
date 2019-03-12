package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.QExecution;
import com.decathlon.ara.domain.QError;
import com.decathlon.ara.domain.QProblem;
import com.decathlon.ara.domain.QProblemPattern;
import com.decathlon.ara.domain.QRun;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;
import com.decathlon.ara.domain.projection.ProblemAggregate;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.TypeRepository;
import com.decathlon.ara.repository.custom.ProblemRepositoryCustom;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ProblemRepositoryImpl implements ProblemRepositoryCustom {

    private static final Sort PROBLEM_CREATION_DATE_TIME_DESC =
            new Sort(Sort.Direction.DESC, QProblem.problem.creationDateTime.getMetadata().getName());

    // Cannot use constructor injection: would cause circular dependency injection
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    private static Predicate toPredicate(QProblem problem, ProblemFilter filter) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(problem.projectId.eq(Long.valueOf(filter.getProjectId())));

        if (StringUtils.isNotEmpty(filter.getName())) {
            predicates.add(problem.name.likeIgnoreCase("%" + filter.getName() + "%"));
        }

        if (filter.getStatus() != null) {
            predicates.add(computeStatusPredicate(problem, filter.getStatus()));
        }

        if (filter.getBlamedTeamId() != null) {
            predicates.add(problem.blamedTeam.id.eq(filter.getBlamedTeamId()));
        }

        if (StringUtils.isNotEmpty(filter.getDefectId())) {
            if ("none".equalsIgnoreCase(filter.getDefectId())) {
                predicates.add(problem.defectId.isNull().or(problem.defectId.isEmpty()));
            } else {
                predicates.add(problem.defectId.likeIgnoreCase("%" + filter.getDefectId() + "%"));
            }
        }

        if (filter.getDefectExistence() != null) {
            predicates.add(problem.defectExistence.eq(filter.getDefectExistence()));
        }

        if (filter.getRootCauseId() != null) {
            predicates.add(problem.rootCause.id.eq(filter.getRootCauseId()));
        }

        return ExpressionUtils.allOf(predicates);
    }

    /**
     * Compute a predicate to filter problems by their status, depending the filtering choice picked from the list of
     * filtering options. Filter is performed through the {@link Problem#getEffectiveStatus()} and some filter options
     * are a combination of several statuses.
     *
     * @param problem the problem table variable to use in the query
     * @param statusFilter the choosen filter for problem statuses
     * @return a predicate to use in a QueryDsl request
     */
    private static Predicate computeStatusPredicate(QProblem problem, ProblemStatusFilter statusFilter) {
        final BooleanExpression open = problem.status.eq(ProblemStatus.OPEN);
        final BooleanExpression closed = problem.status.eq(ProblemStatus.CLOSED);

        // This business logic is also present in another form in Problem.getEffectiveStatus()
        final BooleanExpression reappeared = problem.status.eq(ProblemStatus.CLOSED)
                .and(problem.closingDateTime.isNotNull())
                .and(problem.lastSeenDateTime.isNotNull())
                .and(problem.closingDateTime.before(problem.lastSeenDateTime));

        switch (statusFilter) {
            case OPEN:
                return open;
            case CLOSED:
                return closed.and(reappeared.not());
            case REAPPEARED:
                return reappeared;
            case OPEN_OR_REAPPEARED:
                return open.or(reappeared);
            case CLOSED_OR_REAPPEARED:
                return closed; // CLOSED status includes the REAPPEARED effectiveStatus
            default:
                throw new NotGonnaHappenException("New ProblemStatusFilter enum value not supported in code yet: " +
                        statusFilter);
        }
    }

    /**
     * @param execution an execution
     * @return the distinct number of problems associated to errors attached to the given execution
     */
    @Override
    public long countProblemsOfExecution(Execution execution) {
        return jpaQueryFactory.selectDistinct(QProblem.problem.id)
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.errors, QError.error)
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
                .join(QProblemPattern.problemPattern.errors, QError.error)
                .where(QProblem.problem.id.in(problemIds))
                .where(QError.error.executedScenario.run.execution.id.in(executionIds))
                .fetch();

        return tuples.stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(QProblem.problem.id),
                        Collectors.mapping(tuple -> tuple.get(QError.error.executedScenario.run.execution.id),
                                Collectors.toList())));
    }

    /**
     * GET all problems matching the given filter.
     *
     * @param filter   the search terms
     * @param pageable the pagination information
     * @return a page of matching problems
     */
    @Override
    public Page<Problem> findMatchingProblems(ProblemFilter filter, Pageable pageable) {
        Pageable effectivePageable;
        if (pageable == null) {
            effectivePageable = PageRequest.of(0, 10, PROBLEM_CREATION_DATE_TIME_DESC);
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), PROBLEM_CREATION_DATE_TIME_DESC);
        } else {
            effectivePageable = pageable;
        }

        // toPredicate will append the projectId
        return problemRepository.findAll(toPredicate(QProblem.problem, filter), effectivePageable);
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
                .join(QProblemPattern.problemPattern.errors, QError.error)
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
                .join(QProblemPattern.problemPattern.errors, QError.error)
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
