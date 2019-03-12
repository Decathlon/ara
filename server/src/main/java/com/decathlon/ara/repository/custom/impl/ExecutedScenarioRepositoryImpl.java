package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.QError;
import com.decathlon.ara.domain.QExecutedScenario;
import com.decathlon.ara.domain.QExecution;
import com.decathlon.ara.domain.QProblem;
import com.decathlon.ara.domain.QProblemPattern;
import com.decathlon.ara.domain.QRun;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import com.decathlon.ara.repository.custom.ExecutedScenarioRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutedScenarioRepositoryImpl implements ExecutedScenarioRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ExecutedScenario> findHistory(long projectId, String cucumberId, String branch, String cycleName, String countryCode, String runTypeCode) {
        final QExecutedScenario scenario = QExecutedScenario.executedScenario;

        JPAQuery<ExecutedScenario> query = jpaQueryFactory.select(scenario)
                .from(scenario)
                .where(scenario.run.execution.cycleDefinition.projectId.eq(projectId))
                .where(scenario.cucumberId.eq(cucumberId));

        if (StringUtils.isNotEmpty(branch)) {
            query = query.where(scenario.run.execution.branch.eq(branch));
        }

        if (StringUtils.isNotEmpty(cycleName)) {
            query = query.where(scenario.run.execution.name.eq(cycleName));
        }

        if (StringUtils.isNotEmpty(countryCode)) {
            query = query.where(scenario.run.country.code.eq(countryCode));
        }

        if (StringUtils.isNotEmpty(runTypeCode)) {
            query = query.where(scenario.run.type.code.eq(runTypeCode));
        }

        return query
                // Chronological order of executions
                .orderBy(scenario.run.execution.testDateTime.asc())
                // Then order each runs like Run::compareTo
                .orderBy(scenario.run.country.code.asc())
                .orderBy(scenario.run.type.code.asc())
                // And finally, order scenarios by line (given the cucumberId, they are from the same .feature file and same scenario name)
                .orderBy(scenario.line.asc())
                .fetch();
    }

    /**
     * @param runIds the IDs of the Runs where to find ExecutedScenarios
     * @return all executed-scenario of the runs, with minimal information (id, runId, name, severity) and count of errors and problem-patterns
     */
    @Override
    public List<ExecutedScenarioWithErrorAndProblemJoin> findAllErrorAndProblemCounts(Set<Long> runIds) {
        // The selected entity
        final QExecutedScenario executedScenario = QExecutedScenario.executedScenario;

        // Downward joins
        final QError error = QError.error;
        final QProblemPattern problemPattern = QProblemPattern.problemPattern;
        final QProblem problem = QProblem.problem;

        // Upward joins
        final QRun run = QRun.run;
        final QExecution execution = QExecution.execution;

        return jpaQueryFactory
                .select(Projections.constructor(ExecutedScenarioWithErrorAndProblemJoin.class,
                        executedScenario.id,
                        executedScenario.runId,
                        executedScenario.severity,
                        executedScenario.name,
                        unhandledCount(execution, error, problem),
                        handledCount(execution, problem)))
                .from(executedScenario)

                // Downward joins
                .leftJoin(executedScenario.errors, error)
                .leftJoin(error.problemPatterns, problemPattern)
                .leftJoin(problemPattern.problem, problem)

                // Upward joins
                .leftJoin(executedScenario.run, run)
                .leftJoin(run.execution, execution)

                .where(executedScenario.runId.in(runIds))
                .groupBy(executedScenario.id)
                .fetch();
    }

    private NumberExpression<Long> unhandledCount(QExecution execution, QError error, QProblem problem) {
        final BooleanExpression thereIsAnError = error.id.isNotNull();
        final BooleanExpression theErrorHasNoProblem = problem.id.isNull();
        final BooleanExpression theErrorHasAReappearedProblem = problem.status.eq(ProblemStatus.CLOSED)
                .and(problem.closingDateTime.before(execution.testDateTime));

        return new CaseBuilder()
                .when(thereIsAnError.and(theErrorHasNoProblem.or(theErrorHasAReappearedProblem)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private NumberExpression<Long> handledCount(QExecution execution, QProblem problem) {
        final BooleanExpression thereIsAProblem = problem.id.isNotNull();
        final BooleanExpression theProblemIsOpen = problem.status.eq(ProblemStatus.OPEN);
        final BooleanExpression closingDateIsNullOrAfterTestDate = problem.closingDateTime.isNull()
                .or(problem.closingDateTime.before(execution.testDateTime).not());
        final BooleanExpression theProblemIsClosedAndDidNotReappear = problem.status.eq(ProblemStatus.CLOSED)
                .and(closingDateIsNullOrAfterTestDate);

        return new CaseBuilder()
                .when(thereIsAProblem.and(theProblemIsOpen.or(theProblemIsClosedAndDidNotReappear)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

}
