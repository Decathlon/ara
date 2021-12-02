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

package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemOccurrence;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class ErrorTransformerTest {

    @Mock
    private ProblemTransformer problemTransformer;

    @Spy
    @InjectMocks
    private ErrorTransformer cut;

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void toDto_should_transform_the_do() {
        // Given
        ProblemPattern pattern1 = new ProblemPattern();
        ProblemDTO problemDTO1 = new ProblemDTO();
        pattern1.setId(1L);
        pattern1.setScenarioName("scn1");
        problemDTO1.setId(1L);
        ProblemPattern pattern2 = new ProblemPattern();
        ProblemDTO problemDTO2 = new ProblemDTO();
        pattern2.setId(2L);
        pattern2.setScenarioName("scn2");
        problemDTO2.setId(2L);
        ProblemPattern pattern3 = new ProblemPattern();
        ProblemDTO problemDTO3 = new ProblemDTO();
        pattern3.setId(3L);
        problemDTO3.setId(3L);
        pattern3.setScenarioName("scn3");
        List<ProblemDTO> problems = Lists.list(problemDTO1, problemDTO2, problemDTO3);
        var problemOccurrences = Set.of(
                new ProblemOccurrence(new Error(), pattern1),
                new ProblemOccurrence(new Error(), pattern2),
                new ProblemOccurrence(new Error(), pattern3)
        );
        Error value = new Error(1L, 1L, null, "step",
                "def", 25, "exception", problemOccurrences);
        Mockito.doReturn(problems).when(problemTransformer).toDtos(Mockito.anyCollection());
        // When
        ErrorWithProblemsDTO result = cut.toDto(value);
        // Then
        Mockito.verify(problemTransformer).toDtos(Mockito.anyCollection());
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getStep()).isEqualTo("step");
        Assertions.assertThat(result.getStepDefinition()).isEqualTo("def");
        Assertions.assertThat(result.getStepLine()).isEqualTo(25);
        Assertions.assertThat(result.getException()).isEqualTo("exception");
    }

    @Test
    public void toDto_should_return_an_empty_object_on_null() {
        // When
        ErrorWithProblemsDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Assertions.assertThat(result.getStep()).isNull();
        Assertions.assertThat(result.getStepDefinition()).isNull();
        Assertions.assertThat(result.getStepLine()).isEqualTo(0);
        Assertions.assertThat(result.getException()).isNull();
    }

    @Test
    public void toDtos_should_transform_all() {
        // Given
        List<Error> values = Lists.list(
                new Error(), new Error(), new Error()
        );
        // When
        List<ErrorWithProblemsDTO> result = cut.toDtos(values);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(3);
        Mockito.verify(cut, Mockito.times(3)).toDto(Mockito.any());
        Assertions.assertThat(result.get(0)).isNotNull();
        Assertions.assertThat(result.get(1)).isNotNull();
        Assertions.assertThat(result.get(2)).isNotNull();
    }

    @Test
    public void toDtos_should_return_empty_list_on_empty_list() {
        // When
        List<ErrorWithProblemsDTO> results = cut.toDtos(new ArrayList<>());
        // Then
        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isEmpty();
    }

    @Test
    public void toDtos_should_return_empty_list_on_null() {
        // When
        List<ErrorWithProblemsDTO> results = cut.toDtos(null);
        // Then
        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isEmpty();
    }
}
