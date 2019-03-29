package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.ap.internal.util.Collections;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ErrorTransformerTest {

    @Mock
    private ProblemTransformer problemTransformer;

    @Spy
    @InjectMocks
    private ErrorTransformer cut;

    @Test
    public void toDto_should_transform_the_do() {
        // Given
        ProblemPattern pattern = new ProblemPattern();
        ProblemDTO problemDTO1 = new ProblemDTO();
        pattern.setId(1L);
        problemDTO1.setId(1L);
        ProblemPattern pattern2 = new ProblemPattern();
        ProblemDTO problemDTO2 = new ProblemDTO();
        pattern2.setId(2L);
        problemDTO2.setId(2L);
        ProblemPattern pattern3 = new ProblemPattern();
        ProblemDTO problemDTO3 = new ProblemDTO();
        pattern2.setId(3L);
        problemDTO3.setId(3L);
        List<ProblemDTO> problems = Lists.list(problemDTO1, problemDTO2, problemDTO3);
        Set<ProblemPattern> problemPatterns = Collections.asSet(pattern, pattern2, pattern3);
        Error value = new Error(1L, 1L, null, "step",
                "def", 25, "exception", problemPatterns);
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
