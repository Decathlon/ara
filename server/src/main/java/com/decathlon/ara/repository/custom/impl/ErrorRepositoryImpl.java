package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.QError;
import com.decathlon.ara.domain.QProblem;
import com.decathlon.ara.domain.QProblemPattern;
import com.decathlon.ara.domain.SProblemOccurrence;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.custom.ErrorRepositoryCustom;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.service.TransactionService;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorRepositoryImpl implements ErrorRepositoryCustom {

    private static final Sort ERROR_SORTING = new Sort(Sort.Direction.ASC,
            QError.error.executedScenarioId.getMetadata().getName(),
            QError.error.stepLine.getMetadata().getName());

    private static final String LIKE_MARK = "%";

    @Autowired
    private EntityManager entityManager;

    // Cannot use constructor injection: would cause circular dependency injection
    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private SQLQueryFactory sqlQueryFactory;

    @Autowired
    private ProblemPatternRepository problemPatternRepository;

    @Autowired
    private JpaCacheManager jpaCacheManager;

    @Autowired
    private TransactionService transactionService;

    private Predicate toPredicate(long projectId, QError error, ProblemPattern pattern) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(error.executedScenario.run.execution.cycleDefinition.projectId.eq(Long.valueOf(projectId)));

        appendPredicateFeatureFile(error, pattern, predicates);
        appendPredicateFeatureName(error, pattern, predicates);
        appendPredicateScenarioName(error, pattern, predicates);
        appendPredicateStep(error, pattern, predicates);
        appendPredicateStepDefinition(error, pattern, predicates);
        appendPredicateException(error, pattern, predicates);
        appendPredicateRelease(error, pattern, predicates);
        appendPredicateCountry(error, pattern, predicates);
        appendPredicatePlatform(error, pattern, predicates);
        appendPredicateType(error, pattern, predicates);

        return ExpressionUtils.allOf(predicates);
    }

    private void appendPredicateFeatureFile(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getFeatureFile())) {
            predicates.add(error.executedScenario.featureFile.eq(pattern.getFeatureFile()));
        }
    }

    private void appendPredicateFeatureName(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getFeatureName())) {
            predicates.add(error.executedScenario.featureName.eq(pattern.getFeatureName()));
        }
    }

    void appendPredicateScenarioName(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getScenarioName())) {
            if (pattern.isScenarioNameStartsWith()) {
                predicates.add(error.executedScenario.name.like(pattern.getScenarioName() + LIKE_MARK));
            } else {
                predicates.add(error.executedScenario.name.eq(pattern.getScenarioName()));
            }
        }
    }

    void appendPredicateStep(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getStep())) {
            if (pattern.isStepStartsWith()) {
                predicates.add(error.step.like(pattern.getStep() + LIKE_MARK));
            } else {
                predicates.add(error.step.eq(pattern.getStep()));
            }
        }
    }

    void appendPredicateStepDefinition(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getStepDefinition())) {
            if (pattern.isStepDefinitionStartsWith()) {
                predicates.add(error.stepDefinition.like(pattern.getStepDefinition() + LIKE_MARK));
            } else {
                predicates.add(error.stepDefinition.eq(pattern.getStepDefinition()));
            }
        }
    }

    private void appendPredicateException(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getException())) {
            predicates.add(error.exception.like(pattern.getException() + LIKE_MARK));
        }
    }

    private void appendPredicateRelease(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getRelease())) {
            predicates.add(error.executedScenario.run.execution.release.eq(pattern.getRelease()));
        }
    }

    private void appendPredicateCountry(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (pattern.getCountry() != null && StringUtils.isNotEmpty(pattern.getCountry().getCode())) {
            predicates.add(error.executedScenario.run.country.code.eq(pattern.getCountry().getCode()));
        }
    }

    private void appendPredicatePlatform(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (StringUtils.isNotEmpty(pattern.getPlatform())) {
            predicates.add(error.executedScenario.run.platform.eq(pattern.getPlatform()));
        }
    }

    private void appendPredicateType(QError error, ProblemPattern pattern, List<Predicate> predicates) {
        if (pattern.getType() != null && StringUtils.isNotEmpty(pattern.getType().getCode())) {
            predicates.add(error.executedScenario.run.type.code.eq(pattern.getType().getCode()));
        }
        if (pattern.getTypeIsBrowser() != null) {
            predicates.add(error.executedScenario.run.type.isBrowser.eq(pattern.getTypeIsBrowser()));
        }
        if (pattern.getTypeIsMobile() != null) {
            predicates.add(error.executedScenario.run.type.isMobile.eq(pattern.getTypeIsMobile()));
        }
    }

    @Override
    public Page<Error> findMatchingErrors(long projectId, ProblemPattern pattern, Pageable pageable) {
        Pageable effectivePageable;
        if (pageable == null) {
            effectivePageable = PageRequest.of(0, 10, ERROR_SORTING);
        } else if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ERROR_SORTING);
        } else {
            effectivePageable = pageable;
        }

        // toPredicate will append the projectId
        return errorRepository.findAll(toPredicate(projectId, QError.error, pattern), effectivePageable);
    }

    @Override
    public void assignPatternToErrors(long projectId, ProblemPattern pattern) {
        // If the pattern has just been inserted by Hibernate, issue the real SQL command, because we will issue direct-SQL queries with F.K.
        entityManager.flush();

        List<Long> matchingErrorIds = jpaQueryFactory.select(QError.error.id)
                .from(QError.error)
                .where(toPredicate(projectId, QError.error, pattern)).fetch();

        SProblemOccurrence problemOccurrence = SProblemOccurrence.problemOccurrence;
        SQLInsertClause insert = sqlQueryFactory.insert(problemOccurrence);

        for (Long errorId : matchingErrorIds) {
            insert
                    .set(problemOccurrence.errorId, errorId)
                    .set(problemOccurrence.problemPatternId, pattern.getId())
                    .addBatch();
        }

        transactionService.doAfterCommit(() ->
                jpaCacheManager.evictCollections(Error.PROBLEM_PATTERNS_COLLECTION_CACHE, matchingErrorIds));

        long insertedRows = (insert.getBatchCount() > 0 ? insert.execute() : 0);

        log.info("Inserted {} problemOccurrences", Long.valueOf(insertedRows));
    }

    @Override
    public Map<Error, List<Problem>> getErrorsProblems(Collection<Error> errors) {
        List<Tuple> tuples = jpaQueryFactory.select(QError.error.id, QProblem.problem)
                .distinct()
                .from(QProblem.problem)
                .join(QProblem.problem.patterns, QProblemPattern.problemPattern)
                .join(QProblemPattern.problemPattern.errors, QError.error)
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
                    .where(toPredicate(projectId, QError.error, pattern)).fetch();

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

        log.info("Inserted {} problemOccurrences", Long.valueOf(insertedRows));

        return updatedProblems;
    }

}
