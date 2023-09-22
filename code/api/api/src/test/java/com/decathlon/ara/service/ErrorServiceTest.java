package com.decathlon.ara.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.GenericMapper;

@ExtendWith(MockitoExtension.class)
class ErrorServiceTest {

    private ErrorRepository errorRepository = mock(ErrorRepository.class);

    private GenericMapper mapper = mock(GenericMapper.class);

    private ErrorService errorService = new ErrorService(errorRepository, null, null, null, null, null, null, mapper, null, null);

    @Test
    void getProblemErrors_returnNoErrors_whenNoErrorFound() throws NotFoundException {
        // GIVEN
        Pageable pageable = mock(Pageable.class);

        ProblemPattern problemPattern1 = mock(ProblemPattern.class);
        ProblemPattern problemPattern2 = mock(ProblemPattern.class);
        ProblemPattern problemPattern3 = mock(ProblemPattern.class);

        List<ProblemPattern> problemPatterns = Arrays.asList(problemPattern1, problemPattern2, problemPattern3);

        // WHEN
        when(errorRepository.findDistinctByProblemOccurrencesProblemPatternIn(problemPatterns, pageable)).thenReturn(null);

        // THEN
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = errorService.getErrors(problemPatterns, pageable);
        Assertions.assertNull(errors);
    }

    @Test
    void getProblemErrors_returnErrors_whenErrorsFound() throws NotFoundException {
        // GIVEN
        Pageable pageable = mock(Pageable.class);
        Error error1 = mock(Error.class);
        Error error2 = mock(Error.class);
        Error error3 = mock(Error.class);
        Page<Error> errorPage = new PageImpl<>(Arrays.asList(error1, error2, error3));

        ProblemPattern problemPattern1 = mock(ProblemPattern.class);
        ProblemPattern problemPattern2 = mock(ProblemPattern.class);
        ProblemPattern problemPattern3 = mock(ProblemPattern.class);

        ErrorWithExecutedScenarioAndRunAndExecutionDTO result1 = mock(ErrorWithExecutedScenarioAndRunAndExecutionDTO.class);
        ErrorWithExecutedScenarioAndRunAndExecutionDTO result2 = mock(ErrorWithExecutedScenarioAndRunAndExecutionDTO.class);
        ErrorWithExecutedScenarioAndRunAndExecutionDTO result3 = mock(ErrorWithExecutedScenarioAndRunAndExecutionDTO.class);

        List<ProblemPattern> problemPatterns = Arrays.asList(problemPattern1, problemPattern2, problemPattern3);

        // WHEN
        when(errorRepository.findDistinctByProblemOccurrencesProblemPatternIn(problemPatterns, pageable)).thenReturn(errorPage);
        when(mapper.map(error1, ErrorWithExecutedScenarioAndRunAndExecutionDTO.class)).thenReturn(result1);
        when(mapper.map(error2, ErrorWithExecutedScenarioAndRunAndExecutionDTO.class)).thenReturn(result2);
        when(mapper.map(error3, ErrorWithExecutedScenarioAndRunAndExecutionDTO.class)).thenReturn(result3);

        // THEN
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = errorService.getErrors(problemPatterns, pageable);
        Assertions.assertEquals(3, errorPage.getContent().size());
        Assertions.assertEquals(result1, errors.getContent().get(0));
        Assertions.assertEquals(result2, errors.getContent().get(1));
        Assertions.assertEquals(result3, errors.getContent().get(2));
    }

}
