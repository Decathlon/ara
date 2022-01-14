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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.QError;
import com.querydsl.core.types.Predicate;

public class QErrorTest {

    @Test
    public void appendPredicateScenarioName_ShouldNotFilter_WhenScenarioNameIsNotProvided() {
        // GIVEN
        final QError error = QError.error;
        final ProblemPattern patternWithoutScenarioName = new ProblemPattern();
        final ArrayList<Predicate> predicates = new ArrayList<>();

        // WHEN
        error.appendPredicateScenarioName(patternWithoutScenarioName, predicates);

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
        error.appendPredicateScenarioName(patternWithoutScenarioName, predicates);

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
        error.appendPredicateScenarioName(patternWithScenarioName, predicates);

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
        error.appendPredicateScenarioName(patternWithScenarioName, predicates);

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
        error.appendPredicateStep(patternWithoutStep, predicates);

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
        error.appendPredicateStep(patternWithoutStep, predicates);

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
        error.appendPredicateStep(patternWithStep, predicates);

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
        error.appendPredicateStep(patternWithStep, predicates);

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
        error.appendPredicateStepDefinition(patternWithoutStepDefinition, predicates);

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
        error.appendPredicateStepDefinition(patternWithoutStepDefinition, predicates);

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
        error.appendPredicateStepDefinition(patternWithStepDefinition, predicates);

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
        error.appendPredicateStepDefinition(patternWithStepDefinition, predicates);

        // THEN
        assertThat(predicates.toString()).isEqualTo("[error.stepDefinition like Start%]");
    }

}
