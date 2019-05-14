package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.QExecution;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.custom.ExecutionRepositoryCustom;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionRepositoryImpl implements ExecutionRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Execution> findTop10ByProjectIdAndBranchAndNameOrderByTestDateTimeDesc(long projectId, String branch, String name) {
        QExecution execution = QExecution.execution;
        return jpaQueryFactory
                .select(execution)
                .from(execution)
                .where(execution.cycleDefinition.projectId.eq(Long.valueOf(projectId)))
                .where(execution.cycleDefinition.branch.eq(branch))
                .where(execution.cycleDefinition.name.eq(name))
                .orderBy(execution.testDateTime.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<Execution> findLatestOfEachCycleByProjectId(long projectId) {
        return findExecutionsDescribedBy(getLatestFingerprints(projectId));
    }

    @Override
    public List<Execution> findNextOf(Collection<Execution> referenceExecutions) {
        return findExecutionsDescribedBy(getNextOrPreviousFingerprints(referenceExecutions, true));
    }

    @Override
    public List<Execution> findPreviousOf(Collection<Execution> referenceExecutions) {
        return findExecutionsDescribedBy(getNextOrPreviousFingerprints(referenceExecutions, false));
    }

    private List<Execution> findExecutionsDescribedBy(JPAQuery<Tuple> fingerprints) {
        QExecution execution = QExecution.execution;
        final List<Execution> executions = jpaQueryFactory
                .select(execution)
                .from(execution)
                .where(Expressions.list(
                        execution.cycleDefinition.id,
                        execution.testDateTime).in(fingerprints))
                .fetch();
        // Do not order in the SQL query, as it would add an expensive join (and not returned by the query)
        executions.sort((execution1, execution2) -> {
            Comparator<Execution> cycleDefinitionProjectIdComparator = comparing(c -> Long.valueOf(c.getCycleDefinition().getProjectId()), nullsFirst(naturalOrder()));
            Comparator<Execution> cycleDefinitionBranchPositionComparator = comparing(c -> Integer.valueOf(c.getCycleDefinition().getBranchPosition()), nullsFirst(naturalOrder()));
            Comparator<Execution> cycleDefinitionBranchComparator = comparing(c -> c.getCycleDefinition().getBranch(), nullsFirst(naturalOrder()));
            Comparator<Execution> cycleDefinitionNameComparator = comparing(c -> c.getCycleDefinition().getName(), nullsFirst(naturalOrder()));
            return nullsFirst(cycleDefinitionProjectIdComparator
                    .thenComparing(cycleDefinitionBranchPositionComparator)
                    .thenComparing(cycleDefinitionBranchComparator)
                    .thenComparing(cycleDefinitionNameComparator)).compare(execution1, execution2);
        });
        return executions;
    }

    private JPAQuery<Tuple> getLatestFingerprints(long projectId) {
        QExecution execution = QExecution.execution;
        return jpaQueryFactory
                .select(execution.cycleDefinition.id,
                        execution.testDateTime.max())
                .from(execution)
                .where(execution.cycleDefinition.projectId.eq(Long.valueOf(projectId)))
                .where(execution.status.eq(JobStatus.DONE))
                .where(execution.acceptance.notIn(ExecutionAcceptance.DISCARDED))
                .groupBy(execution.cycleDefinition.id);
    }

    private JPAQuery<Tuple> getNextOrPreviousFingerprints(Collection<Execution> referenceExecutions, boolean next) {
        QExecution execution = QExecution.execution;
        QExecution currentExecution = new QExecution("currentExecution");
        return jpaQueryFactory
                .select(execution.cycleDefinition.id,
                        (next ? execution.testDateTime.min() : execution.testDateTime.max()))
                .from(execution, currentExecution)
                // Restrict currentExecution to only the requested reference executions
                .where(currentExecution.id.in(referenceExecutions.stream().map(Execution::getId).collect(Collectors.toSet())))
                // Join both tables
                .where(currentExecution.cycleDefinition.id.eq(execution.cycleDefinition.id))
                .where(next ? currentExecution.testDateTime.lt(execution.testDateTime) : currentExecution.testDateTime.gt(execution.testDateTime))
                .groupBy(execution.cycleDefinition.id);
    }

    @Override
    public List<Execution> getLatestEligibleVersionsByProjectId(long projectId) {
        QExecution execution = QExecution.execution;
        QExecution subExecution = new QExecution("sub");

        final JPQLQuery<Tuple> descriptionOfLatestEligibleExecutionPerBranch = JPAExpressions
                .select(subExecution.cycleDefinition.branch, subExecution.testDateTime.max())
                .from(subExecution)
                .where(getEligibilityClause(subExecution, projectId))
                .groupBy(subExecution.branch);

        // Given all execution with eligible status (blocking validation, ended and succeed),
        // find the latest ones per branch
        // then return all data of the executions matching the found (branch,testDateTime) couples.
        // WARNING: it's possible a user will launch two cycles for the same branch at the exact same time:
        // we should only return the one that matches eligibility status
        // => that's why we re-run the WHERE clause in the root request
        // (we assume only one of the two cycles will be blocking)
        return jpaQueryFactory
                .select(execution)
                .from(execution)
                .where(Expressions.list(execution.cycleDefinition.branch, execution.testDateTime)
                        .in(descriptionOfLatestEligibleExecutionPerBranch))
                .where(getEligibilityClause(execution, projectId))
                .orderBy(execution.branch.asc())
                .fetch();
    }

    /**
     * @param execution the execution table
     * @param projectId the ID of the project in which to work
     * @return a WHERE clause returning not-discarded finished jobs with blocking validation enabled and with
     * acceptable quality status
     */
    private BooleanExpression getEligibilityClause(QExecution execution, long projectId) {
        return execution.cycleDefinition.projectId.eq(Long.valueOf(projectId))
                .and(execution.status.eq(JobStatus.DONE))
                .and(execution.acceptance.notIn(ExecutionAcceptance.DISCARDED))
                .and(execution.blockingValidation.eq(Boolean.TRUE))
                .and(execution.qualityStatus.in(QualityStatus.PASSED, QualityStatus.WARNING));
    }

}
