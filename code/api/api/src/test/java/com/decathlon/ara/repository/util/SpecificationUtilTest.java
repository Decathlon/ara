package com.decathlon.ara.repository.util;

import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.query.criteria.internal.PathImplementor;
import org.hibernate.query.criteria.internal.expression.function.ParameterizedFunctionExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;
import com.decathlon.ara.util.TestUtil;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
class SpecificationUtilTest {

    @Autowired
    private EntityManager entityManager;

    private enum PredicateType {
        EQUAL,
        LIKE,
        LESS_THAN,
        GREATER_THAN,
        IS_NULL,
        IS_NOT_NULL,
        NOT,
        IN,
        AND,
        OR;
    }

    private record PredicateWithInfo(Predicate original, Expression<?> expression, Object value, PredicateType type) implements Predicate {

        @Override
        public Predicate isNull() {
            return original.isNull();
        }

        @Override
        public Predicate isNotNull() {
            return original.isNotNull();
        }

        @Override
        public Predicate in(Object... values) {
            return original.in(values);
        }

        @Override
        public Predicate in(Expression<?>... values) {
            return original.in(values);
        }

        @Override
        public Predicate in(Collection<?> values) {
            return original.in(values);
        }

        @Override
        public Predicate in(Expression<Collection<?>> values) {
            return original.in(values);
        }

        @Override
        public <X> Expression<X> as(Class<X> type) {
            return original.as(type);
        }

        @Override
        public Selection<Boolean> alias(String name) {
            return original.alias(name);
        }

        @Override
        public boolean isCompoundSelection() {
            return original.isCompoundSelection();
        }

        @Override
        public List<Selection<?>> getCompoundSelectionItems() {
            return original.getCompoundSelectionItems();
        }

        @Override
        public Class<? extends Boolean> getJavaType() {
            return original.getJavaType();
        }

        @Override
        public String getAlias() {
            return original.getAlias();
        }

        @Override
        public BooleanOperator getOperator() {
            return original.getOperator();
        }

        @Override
        public boolean isNegated() {
            return original.isNegated();
        }

        @Override
        public List<Expression<Boolean>> getExpressions() {
            return original.getExpressions();
        }

        @Override
        public Predicate not() {
            return new PredicateWithInfo(original.not(), null, this, PredicateType.NOT);
        }

        private String getName() {
            return SpecificationUtilTest.getName(expression);
        }

        @SuppressWarnings("unchecked")
        private List<PredicateWithInfo> getElements() {
            if (type == PredicateType.AND || type == PredicateType.OR) {
                return (List<PredicateWithInfo>) value;
            }
            return null;
        }

        private PredicateWithInfo getNegated() {
            if (type == PredicateType.NOT) {
                return (PredicateWithInfo) value;
            }
            return null;
        }

    }

    private static void appendName(StringBuilder builder, Expression<?> expressionToAppend) {
        if (expressionToAppend == null) {
            return;
        }
        Expression<?> currentExpression = expressionToAppend;
        if (expressionToAppend instanceof ParameterizedFunctionExpression<?> function) {
            List<Expression<?>> argumentExpressions = function.getArgumentExpressions();
            currentExpression = argumentExpressions.get(0);
        }
        if (currentExpression instanceof Path<?> path) {
            appendName(builder, path.getParentPath());
        }
        if (expressionToAppend instanceof ParameterizedFunctionExpression<?> function) {
            builder.append(function.getFunctionName()).append('(');
        }
        if (currentExpression instanceof Root<?>) {
            builder.append("root");
        } else if (currentExpression instanceof PathImplementor<?> pathImplementor) {
            builder.append(pathImplementor.getAttribute().getName());
        }
        if (expressionToAppend instanceof ParameterizedFunctionExpression<?> function) {
            builder.append(')');
        }
        builder.append('.');
    }

    private static String getName(Expression<?> expression) {
        StringBuilder builder = new StringBuilder();
        appendName(builder, expression);
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private void prepareTest(CriteriaBuilder criteriaBuilder, PredicateType... additionalTypes) {
        Set<PredicateType> types = Arrays.stream(additionalTypes).collect(Collectors.toSet());
        types.add(PredicateType.EQUAL);
        types.add(PredicateType.AND);
        prepareTest(criteriaBuilder, types);
    }

    private void prepareTest(CriteriaBuilder criteriaBuilder, Set<PredicateType> types) {
        if (types != null) {
            for (PredicateType type : types) {
                prepareTest(criteriaBuilder, type, false);
            }
        }
    }

    private void prepareTest(CriteriaBuilder criteriaBuilder, PredicateType type, boolean mockAdditional) {
        switch (type) {
            case EQUAL: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Object value = invocationOnMock.getArgument(1);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, value, PredicateType.EQUAL);
                }).when(criteriaBuilder).equal(any(), any(Object.class));
                break;
            }
            case LIKE: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Object value = invocationOnMock.getArgument(1);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, value, PredicateType.LIKE);
                }).when(criteriaBuilder).like(any(), any(String.class));
                break;
            }
            case LESS_THAN: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Object value = invocationOnMock.getArgument(1);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, value, PredicateType.LESS_THAN);
                }).when(criteriaBuilder).lessThan(any(), Mockito.<Path<Date>>any());
                break;
            }
            case GREATER_THAN: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Object value = invocationOnMock.getArgument(1);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, value, PredicateType.GREATER_THAN);
                }).when(criteriaBuilder).greaterThan(any(), any(Date.class));
                break;
            }
            case IS_NULL: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, null, PredicateType.IS_NULL);
                }).when(criteriaBuilder).isNull(any());
                break;
            }
            case IS_NOT_NULL: {
                Mockito.doAnswer(invocationOnMock -> {
                    Expression<?> expression = invocationOnMock.getArgument(0);
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, expression, null, PredicateType.IS_NOT_NULL);
                }).when(criteriaBuilder).isNotNull(any());
                break;
            }
            case AND: {
                Answer<Predicate> answer = invocationOnMock -> {
                    List<PredicateWithInfo> predicates = new ArrayList<>();
                    predicates.addAll(Arrays.stream(invocationOnMock.getArguments()).map(PredicateWithInfo.class::cast).toList());
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, null, predicates, PredicateType.AND);
                };
                if (!mockAdditional) {
                    Mockito.doAnswer(answer).when(criteriaBuilder).and(any());
                } else {
                    Mockito.doAnswer(answer).when(criteriaBuilder).and(any(), any());
                }
                break;
            }
            case OR: {
                Mockito.doAnswer(invocationOnMock -> {
                    List<PredicateWithInfo> predicates = new ArrayList<>();
                    predicates.addAll(Arrays.stream(invocationOnMock.getArguments()).map(PredicateWithInfo.class::cast).toList());
                    Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
                    return new PredicateWithInfo(predicate, null, predicates, PredicateType.OR);
                }).when(criteriaBuilder).or(any(), any());
                break;
            }
            default:
                Assertions.fail("unsuported PredicateType : " + type);
        }
    }

    @Test
    void errorSpecificationShouldHaveAtLeastEqualsPredicateOnProjectIdWhenNoPropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, new ProblemPattern(), null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnFeatureFileWhenFeatureFilePropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "featureFile", "file");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getFeatureFile(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.featureFile", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnFeatureNameWhenFeatureNamePropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "featureName", "name");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getFeatureName(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.featureName", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnScenarioNameWhenScenarioNamePropertyOfProblemPatternIsSetAndIsScenarioNameStartsWithIsFalse() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "scenarioName", "scenarioName");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getScenarioName(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.name", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveStartWithPredicateOnScenarioNameWhenScenarioNamePropertyOfProblemPatternIsSetAndIsScenarioNameStartsWithIsTrue() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.LIKE);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "scenarioName", "scenarioName");
        TestUtil.setField(problemPattern, "scenarioNameStartsWith", true);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getScenarioName() + "%", predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.name", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.LIKE, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnStepWhenStepPropertyOfProblemPatternIsSetAndIsStepStartsWithIsFalse() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "step", "step");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getStep(), predicates.get(1).value());
        Assertions.assertEquals("root.step", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveStartWithPredicateOnStepWhenStepPropertyOfProblemPatternIsSetAndIsStepStartsWithIsTrue() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.LIKE);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "step", "step");
        TestUtil.setField(problemPattern, "stepStartsWith", true);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getStep() + "%", predicates.get(1).value());
        Assertions.assertEquals("root.step", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.LIKE, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnStepDefinitionWhenStepDefinitionPropertyOfProblemPatternIsSetAndIsStepDefinitionStartsWithIsFalse() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "stepDefinition", "stepDefinition");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getStepDefinition(), predicates.get(1).value());
        Assertions.assertEquals("root.stepDefinition", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveStartWithPredicateOnStepDefinitionWhenStepDefinitionPropertyOfProblemPatternIsSetAndIsStepDefinitionStartsWithIsTrue() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.LIKE);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "stepDefinition", "stepDefinition");
        TestUtil.setField(problemPattern, "stepDefinitionStartsWith", true);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getStepDefinition() + "%", predicates.get(1).value());
        Assertions.assertEquals("root.stepDefinition", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.LIKE, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnExceptionWhenExceptionPropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "exception", "exception");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getException(), predicates.get(1).value());
        Assertions.assertEquals("root.exception", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnReleaseWhenReleasePropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "release", "release");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getRelease(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.execution.release", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldNotHaveAnyAdditionalPredicateWhenCountryPropertyOfProblemPatternIsSetButCodeIsNull() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "country", new Country());
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void errorSpecificationShouldNotHaveAnyAdditionalPredicateWhenCountryPropertyOfProblemPatternIsSetButCodeIsEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        Country country = new Country();
        TestUtil.setField(country, "code", "");
        TestUtil.setField(problemPattern, "country", country);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnCountryCodeWhenCountryPropertyOfProblemPatternIsSetAndCodeIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        Country country = new Country();
        TestUtil.setField(country, "code", "code");
        TestUtil.setField(problemPattern, "country", country);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(country.getCode(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.country.code", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnPlatformWhenPlatformPropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "platform", "platform");
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getPlatform(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.platform", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldNotHaveAnyAdditionalPredicateWhenTypePropertyOfProblemPatternIsSetButCodeIsNull() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "type", new Type());
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void errorSpecificationShouldNotHaveAnyAdditionalPredicateWhenTypePropertyOfProblemPatternIsSetButCodeIsEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        Type type = new Type();
        TestUtil.setField(type, "code", "");
        TestUtil.setField(problemPattern, "type", type);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnTypeCodeWhenTypePropertyOfProblemPatternIsSetAndCodeIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        Type type = new Type();
        TestUtil.setField(type, "code", "code");
        TestUtil.setField(problemPattern, "type", type);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(type.getCode(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.type.code", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnTypeIsBrowserCodeWhenTypeIsBrowserPropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "typeIsBrowser", true);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getTypeIsBrowser(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.type.isBrowser", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveEqualsPredicateOnTypeIsMobileCodeWhenTypeIsMobilePropertyOfProblemPatternIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemPattern problemPattern = new ProblemPattern();
        TestUtil.setField(problemPattern, "typeIsMobile", false);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, problemPattern, null);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemPattern.getTypeIsMobile(), predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.type.isMobile", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void errorSpecificationShouldHaveInPredicateOnErrorIdsWhenErrorIdsIsNotNull() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Error> criteriaQuery = criteriaBuilder.createQuery(Error.class);
        Root<Error> root = criteriaQuery.from(Error.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Path<Object> executedScenario = root.get("executedScenario");
        Path<Object> run = executedScenario.get("run");
        Path<Object> idPath = run.get("id");
        root = Mockito.spy(root);
        Path<Object> executedScenarioSpy = Mockito.spy(executedScenario);
        Path<Object> runSpy = Mockito.spy(run);
        Path<Object> idPathSpy = Mockito.spy(idPath);
        Mockito.doAnswer(invocationOnMock -> {
            if ("executedScenario".equals(invocationOnMock.getArgument(0))) {
                return executedScenarioSpy;
            } else {
                return invocationOnMock.callRealMethod();
            }
        }).when(root).get(Mockito.anyString());
        Mockito.doAnswer(invocationOnMock -> {
            if ("run".equals(invocationOnMock.getArgument(0))) {
                return runSpy;
            } else {
                return invocationOnMock.callRealMethod();
            }
        }).when(executedScenarioSpy).get(Mockito.anyString());
        ;
        Mockito.doAnswer(invocationOnMock -> {
            if ("id".equals(invocationOnMock.getArgument(0))) {
                return idPathSpy;
            } else {
                return invocationOnMock.callRealMethod();
            }
        }).when(runSpy).get(Mockito.anyString());
        Mockito.doAnswer(invocationOnMock -> {
            List<Long> value = invocationOnMock.getArgument(0);
            Predicate predicate = (Predicate) invocationOnMock.callRealMethod();
            return new PredicateWithInfo(predicate, idPath, value, PredicateType.IN);
        }).when(idPathSpy).in(Mockito.anyCollection());
        List<Long> errorIds = List.of(1l, 2l);
        Specification<Error> errorSpecification = SpecificationUtil.toErrorSpecification(1, new ProblemPattern(), errorIds);
        PredicateWithInfo predicate = (PredicateWithInfo) errorSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.executedScenario.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(errorIds, predicates.get(1).value());
        Assertions.assertEquals("root.executedScenario.run.id", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.IN, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveAtLeastEqualsPredicateOnProjectIdWhenNoPropertyOfProblemFilterIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(new ProblemFilter());
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(1, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
    }

    @Test
    void problemSpecificationShouldHaveLikeIgnoreCasePredicateOnNameWhenNamePropertyOfProblemFilterIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.LIKE);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "name", "nameWithUpperChar");
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("%" + problemFilter.getName().toLowerCase() + "%", predicates.get(1).value());
        Assertions.assertEquals("root.lower(name)", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.LIKE, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveEqualsPredicateOnStatusWhenStatusPropertyOfProblemFilterIsSetToOPEN() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.LESS_THAN);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "status", ProblemStatusFilter.OPEN);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(ProblemStatus.OPEN, predicates.get(1).value());
        Assertions.assertEquals("root.status", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveComplexPredicateOnStatusAndStatusDateTimeWhenStatusPropertyOfProblemFilterIsSetToCLOSED() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.IS_NOT_NULL, PredicateType.LESS_THAN);
        prepareTest(criteriaBuilder, PredicateType.AND, true);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "status", ProblemStatusFilter.CLOSED);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        PredicateWithInfo closedAndPredicate = predicates.get(1);
        Assertions.assertEquals(PredicateType.AND, closedAndPredicate.type());
        List<PredicateWithInfo> closedAndPredicateElements = closedAndPredicate.getElements();
        Assertions.assertEquals(2, closedAndPredicateElements.size());
        Assertions.assertEquals(ProblemStatus.CLOSED, closedAndPredicateElements.get(0).value());
        Assertions.assertEquals("root.status", closedAndPredicateElements.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, closedAndPredicateElements.get(0).type());
        Assertions.assertEquals(PredicateType.NOT, closedAndPredicateElements.get(1).type());
        assertIsReapearedPredicate(closedAndPredicateElements.get(1).getNegated());
    }

    @Test
    void problemSpecificationShouldHaveComplexPredicateOnStatusAndStatusDateTimeWhenStatusPropertyOfProblemFilterIsSetToREAPPEARED() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.IS_NOT_NULL, PredicateType.LESS_THAN);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "status", ProblemStatusFilter.REAPPEARED);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        assertIsReapearedPredicate(predicates.get(1));
    }

    @Test
    void problemSpecificationShouldHaveComplexPredicateOnStatusAndStatusDateTimeWhenStatusPropertyOfProblemFilterIsSetToOPEN_OR_REAPPEARED() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.IS_NOT_NULL, PredicateType.LESS_THAN, PredicateType.OR);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "status", ProblemStatusFilter.OPEN_OR_REAPPEARED);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        PredicateWithInfo closedAndPredicate = predicates.get(1);
        Assertions.assertEquals(PredicateType.OR, closedAndPredicate.type());
        List<PredicateWithInfo> closedAndPredicateElements = closedAndPredicate.getElements();
        Assertions.assertEquals(2, closedAndPredicateElements.size());
        Assertions.assertEquals(ProblemStatus.OPEN, closedAndPredicateElements.get(0).value());
        Assertions.assertEquals("root.status", closedAndPredicateElements.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, closedAndPredicateElements.get(0).type());
        assertIsReapearedPredicate(closedAndPredicateElements.get(1));
    }

    @Test
    void problemSpecificationShouldHaveEqualsPredicateOnStatusWhenStatusPropertyOfProblemFilterIsSetToCLOSED_OR_REAPPEARED() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.LESS_THAN);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "status", ProblemStatusFilter.CLOSED_OR_REAPPEARED);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(ProblemStatus.CLOSED, predicates.get(1).value());
        Assertions.assertEquals("root.status", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveEqualsPredicateOnBlamedTeamWhenBlamedTeamIdPropertyOfProblemFilterIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "blamedTeamId", 1l);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemFilter.getBlamedTeamId(), predicates.get(1).value());
        Assertions.assertEquals("root.blamedTeam", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveContainsIgnoreCasePredicateOnDefectIdWhenDefectIdPropertyOfProblemFilterIsSetToAValueDifferentOfNone() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.LIKE);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "defectId", "idWithUpperChar");
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("%" + problemFilter.getDefectId().toLowerCase() + "%", predicates.get(1).value());
        Assertions.assertEquals("root.lower(defectId)", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.LIKE, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveNotNullAndNotEmptyPredicateOnDefectIdWhenDefectIdPropertyOfProblemFilterIsSetToNone() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.IS_NULL, PredicateType.OR);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "defectId", "none");
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(PredicateType.OR, predicates.get(1).type());
        List<PredicateWithInfo> defectIdPredicateElements = predicates.get(1).getElements();
        Assertions.assertEquals(2, defectIdPredicateElements.size());
        Assertions.assertEquals("root.defectId", defectIdPredicateElements.get(0).getName());
        Assertions.assertEquals(PredicateType.IS_NULL, defectIdPredicateElements.get(0).type());
        Assertions.assertEquals("", defectIdPredicateElements.get(1).value());
        Assertions.assertEquals("root.defectId", defectIdPredicateElements.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, defectIdPredicateElements.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveEqualsPredicateOnDefectExistenceWhenDefectExistencePropertyOfProblemFilterIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "defectExistence", DefectExistence.EXISTS);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemFilter.getDefectExistence(), predicates.get(1).value());
        Assertions.assertEquals("root.defectExistence", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    @Test
    void problemSpecificationShouldHaveEqualsPredicateOnRootCauseWhenRootCauseIdPropertyOfProblemFilterIsSet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Problem> criteriaQuery = criteriaBuilder.createQuery(Problem.class);
        Root<Problem> root = criteriaQuery.from(Problem.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        ProblemFilter problemFilter = new ProblemFilter();
        TestUtil.setField(problemFilter, "rootCauseId", 1l);
        Specification<Problem> problemSpecification = SpecificationUtil.toProblemSpecification(problemFilter);
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, null, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(0l, predicates.get(0).value());
        Assertions.assertEquals("root.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals(problemFilter.getRootCauseId(), predicates.get(1).value());
        Assertions.assertEquals("root.rootCause", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
    }

    private void assertIsReapearedPredicate(PredicateWithInfo reappearedAndPredicate) {
        Assertions.assertEquals(PredicateType.AND, reappearedAndPredicate.type());
        List<PredicateWithInfo> reappearedAndPredicateElements = reappearedAndPredicate.getElements();
        Assertions.assertEquals(4, reappearedAndPredicateElements.size());
        Assertions.assertEquals(ProblemStatus.CLOSED, reappearedAndPredicateElements.get(0).value());
        Assertions.assertEquals("root.status", reappearedAndPredicateElements.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, reappearedAndPredicateElements.get(0).type());
        Assertions.assertEquals("root.closingDateTime", reappearedAndPredicateElements.get(1).getName());
        Assertions.assertEquals(PredicateType.IS_NOT_NULL, reappearedAndPredicateElements.get(1).type());
        Assertions.assertEquals("root.lastSeenDateTime", reappearedAndPredicateElements.get(2).getName());
        Assertions.assertEquals(PredicateType.IS_NOT_NULL, reappearedAndPredicateElements.get(2).type());
        Assertions.assertEquals("root.closingDateTime", reappearedAndPredicateElements.get(3).getName());
        Assertions.assertEquals("root.lastSeenDateTime", getName((Expression<?>) reappearedAndPredicateElements.get(3).value()));
        Assertions.assertEquals(PredicateType.LESS_THAN, reappearedAndPredicateElements.get(3).type());
    }

    @Test
    void executedScenarioSpecificationShouldHaveAtLeastEqualsPredicateOnProjectIdAndCucumnerIdAndOrderByOnTestDateTimeCountryCodeTypeCodeAndLineWhenNoAllOtherParametersAreNull() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", null, null, null, null, Optional.ofNullable(null));
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(2, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("cucumberId", predicates.get(1).value());
        Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
        List<Order> orderList = criteriaQuery.getOrderList();
        Assertions.assertEquals(4, orderList.size());
        Assertions.assertEquals("root.run.execution.testDateTime", getName(orderList.get(0).getExpression()));
        Assertions.assertTrue(orderList.get(0).isAscending());
        Assertions.assertEquals("root.run.country.code", getName(orderList.get(1).getExpression()));
        Assertions.assertTrue(orderList.get(1).isAscending());
        Assertions.assertEquals("root.run.type.code", getName(orderList.get(2).getExpression()));
        Assertions.assertTrue(orderList.get(2).isAscending());
        Assertions.assertEquals("root.line", getName(orderList.get(3).getExpression()));
        Assertions.assertTrue(orderList.get(3).isAscending());
    }

    @Test
    void executedScenarioSpecificationShouldHaveEqualsPredicateOnBranchWhenBranchParametersIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", "branch", null, null, null, Optional.ofNullable(null));
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(3, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("cucumberId", predicates.get(1).value());
        Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
        Assertions.assertEquals("branch", predicates.get(2).value());
        Assertions.assertEquals("root.run.execution.branch", predicates.get(2).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(2).type());
    }

    @Test
    void executedScenarioSpecificationShouldHaveEqualsPredicateOnExecutionNameWhenCycleNameParametersIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", null, "cycleName", null, null, Optional.ofNullable(null));
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(3, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("cucumberId", predicates.get(1).value());
        Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
        Assertions.assertEquals("cycleName", predicates.get(2).value());
        Assertions.assertEquals("root.run.execution.name", predicates.get(2).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(2).type());
    }

    @Test
    void executedScenarioSpecificationShouldHaveEqualsPredicateOnCountryCodeWhenCountryCodeParametersIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", null, null, "countryCode", null, Optional.ofNullable(null));
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(3, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("cucumberId", predicates.get(1).value());
        Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
        Assertions.assertEquals("countryCode", predicates.get(2).value());
        Assertions.assertEquals("root.run.country.code", predicates.get(2).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(2).type());
    }

    @Test
    void executedScenarioSpecificationShouldHaveEqualsPredicateOnTypeCodeWhenTypeCodeParametersIsNotEmpty() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder);
        Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", null, null, null, "typeCode", Optional.ofNullable(null));
        PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
        Assertions.assertEquals(PredicateType.AND, predicate.type());
        List<PredicateWithInfo> predicates = predicate.getElements();
        Assertions.assertEquals(3, predicates.size());
        Assertions.assertEquals(1l, predicates.get(0).value());
        Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
        Assertions.assertEquals("cucumberId", predicates.get(1).value());
        Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
        Assertions.assertEquals("typeCode", predicates.get(2).value());
        Assertions.assertEquals("root.run.type.code", predicates.get(2).getName());
        Assertions.assertEquals(PredicateType.EQUAL, predicates.get(2).type());
    }

    @Test
    void executedScenarioSpecificationShouldHaveGreaterThanPredicateOnTestDateTimeWhendurationParametersIsNotNull() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExecutedScenario> criteriaQuery = criteriaBuilder.createQuery(ExecutedScenario.class);
        Root<ExecutedScenario> root = criteriaQuery.from(ExecutedScenario.class);
        criteriaBuilder = Mockito.spy(criteriaBuilder);
        prepareTest(criteriaBuilder, PredicateType.GREATER_THAN);

        LocalDateTime nowMockDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        try (MockedStatic<LocalDateTime> localDateTimeMockStatic = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTimeMockStatic.when(LocalDateTime::now).thenReturn(nowMockDateTime);
            Specification<ExecutedScenario> problemSpecification = SpecificationUtil.toExecutedScenarioSpecification(1l, "cucumberId", null, null, null, null, Optional.ofNullable(Period.ofDays(1)));
            PredicateWithInfo predicate = (PredicateWithInfo) problemSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
            Assertions.assertEquals(PredicateType.AND, predicate.type());
            List<PredicateWithInfo> predicates = predicate.getElements();
            Assertions.assertEquals(3, predicates.size());
            Assertions.assertEquals(1l, predicates.get(0).value());
            Assertions.assertEquals("root.run.execution.cycleDefinition.projectId", predicates.get(0).getName());
            Assertions.assertEquals(PredicateType.EQUAL, predicates.get(0).type());
            Assertions.assertEquals("cucumberId", predicates.get(1).value());
            Assertions.assertEquals("root.cucumberId", predicates.get(1).getName());
            Assertions.assertEquals(PredicateType.EQUAL, predicates.get(1).type());
            Assertions.assertEquals(new Date(-24 * 3600000), predicates.get(2).value());
            Assertions.assertEquals("root.run.execution.testDateTime", predicates.get(2).getName());
            Assertions.assertEquals(PredicateType.GREATER_THAN, predicates.get(2).type());
        }

    }

}
