package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.QError;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.service.TransactionService;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ErrorRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ErrorRepository errorRepository;

    @Mock
    private JPAQueryFactory jpaQueryFactory;

    @Mock
    private SQLQueryFactory sqlQueryFactory;

    @Mock
    private ProblemPatternRepository problemPatternRepository;

    @Mock
    private JpaCacheManager jpaCacheManager;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ErrorRepositoryImpl cut;

    @Test
    public void appendPredicateScenarioName_ShouldNotFilter_WhenScenarioNameIsNotProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutScenarioName = new ProblemPattern();
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateScenarioName(error, patternWithoutScenarioName, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateScenarioName_ShouldNotFilter_WhenScenarioNameIsNotProvidedEvenIfStartsWithIsTrue() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutScenarioName = new ProblemPattern()
                .withScenarioNameStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateScenarioName(error, patternWithoutScenarioName, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateScenarioName_ShouldFilterByExactScenarioName_WhenScenarioNameIsProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithScenarioName = new ProblemPattern()
                .withScenarioName("Name");
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateScenarioName(error, patternWithScenarioName, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.executedScenario.name = Name]");
    }

    @Test
    public void appendPredicateScenarioName_ShouldFilterByScenarioNameStart_WhenScenarioNameIsProvidedWithStartsWith() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithScenarioName = new ProblemPattern()
                .withScenarioName("Start")
                .withScenarioNameStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateScenarioName(error, patternWithScenarioName, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.executedScenario.name like Start%]");
    }

    @Test
    public void appendPredicateStep_ShouldNotFilter_WhenStepIsNotProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutStep = new ProblemPattern();
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStep(error, patternWithoutStep, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateStep_ShouldNotFilter_WhenStepIsNotProvidedEvenIfStartsWithIsTrue() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutStep = new ProblemPattern()
                .withStepStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStep(error, patternWithoutStep, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateStep_ShouldFilterByExactStep_WhenStepIsProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithStep = new ProblemPattern()
                .withStep("Name");
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStep(error, patternWithStep, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.step = Name]");
    }

    @Test
    public void appendPredicateStep_ShouldFilterByStepStart_WhenStepIsProvidedWithStartsWith() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithStep = new ProblemPattern()
                .withStep("Start")
                .withStepStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStep(error, patternWithStep, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.step like Start%]");
    }

    @Test
    public void appendPredicateStepDefinition_ShouldNotFilter_WhenStepDefinitionIsNotProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutStepDefinition = new ProblemPattern();
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStepDefinition(error, patternWithoutStepDefinition, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateStepDefinition_ShouldNotFilter_WhenStepDefinitionIsNotProvidedEvenIfStartsWithIsTrue() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutStepDefinition = new ProblemPattern()
                .withStepDefinitionStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStepDefinition(error, patternWithoutStepDefinition, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[]");
    }

    @Test
    public void appendPredicateStepDefinition_ShouldFilterByExactStepDefinition_WhenStepDefinitionIsProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithStepDefinition = new ProblemPattern()
                .withStepDefinition("Name");
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStepDefinition(error, patternWithStepDefinition, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.stepDefinition = Name]");
    }

    @Test
    public void appendPredicateStepDefinition_ShouldFilterByStepDefinitionStart_WhenStepDefinitionIsProvidedWithStartsWith() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithStepDefinition = new ProblemPattern()
                .withStepDefinition("Start")
                .withStepDefinitionStartsWith(true);
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        cut.appendPredicateStepDefinition(error, patternWithStepDefinition, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.stepDefinition like Start%]");
    }

}
