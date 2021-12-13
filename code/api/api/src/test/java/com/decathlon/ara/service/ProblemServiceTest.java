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

package com.decathlon.ara.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.repository.custom.util.TransactionAppenderUtil;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.GenericMapper;
import com.decathlon.ara.service.util.DateService;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ErrorService errorService;

    @Mock
    private ProblemPatternRepository problemPatternRepository;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private CycleDefinitionRepository cycleDefinitionRepository;

    @Mock
    private RootCauseRepository rootCauseRepository;

    @Mock
    private ProblemPatternService problemPatternService;

    @Mock
    private ProblemDenormalizationService problemDenormalizationService;

    @Mock
    private RootCauseService rootCauseService;

    @Mock
    private TeamService teamService;

    @Mock
    private DateService dateService;

    @Mock
    private DefectService defectService;

    @Mock
    private DefectAdapter defectAdapter;

    @Mock
    private GenericMapper mapper;

    @Mock
    private JpaCacheManager jpaCacheManager;

    @Mock
    private TransactionAppenderUtil transactionService;

    @InjectMocks
    private ProblemService cut;

    @Test
    void handleDefectIdChange_should_do_nothing_if_defect_did_not_change() throws BadRequestException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO(oldDefectId, DefectExistence.EXISTS, ProblemStatus.CLOSED, oldDate);

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isEqualTo(oldDefectId);
        assertThat(problemDto.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problemDto.getClosingDateTime()).isEqualTo(oldDate);
    }

    @Test
    void handleDefectIdChange_should_remove_defect_traces_when_unsetting_it() throws BadRequestException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("", DefectExistence.EXISTS, ProblemStatus.CLOSED, oldDate);
        ;
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isNull();
        assertThat(problemDto.getDefectExistence()).isNull();
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problemDto.getClosingDateTime()).isEqualTo(oldDate);
    }

    @Test
    void handleDefectIdChange_should_throw_BadRequestException_on_bad_defect_id_format() {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("bad", DefectExistence.EXISTS, ProblemStatus.CLOSED, oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId("bad"))).thenReturn(Boolean.FALSE);
        when(defectAdapter.getIdFormatHint(aProjectId)).thenReturn("HINT");

        // WHEN
        try {
            cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);
            fail("Call did not throw BadRequestException");
        } catch (BadRequestException e) {
            // THEN
            assertThat(e.getMessage()).isEqualTo("The work item id is malformed: HINT.");
            assertThat(e.getResourceName()).isEqualTo("problem");
            assertThat(e.getErrorKey()).isEqualTo("wrong_defect_id_format");
        }
    }

    @Test
    void handleDefectIdChange_should_set_existence_UNKNOWN_and_status_OPEN_when_defect_tracking_system_do_not_respond() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("new", DefectExistence.EXISTS, ProblemStatus.CLOSED, oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId("new"))).thenReturn(Boolean.TRUE);
        when(defectAdapter.getStatuses(aProjectId, Collections.singletonList("new")))
                .thenThrow(new FetchException("any"));

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isEqualTo("new");
        assertThat(problemDto.getDefectExistence()).isEqualTo(DefectExistence.UNKNOWN);
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problemDto.getClosingDateTime()).isNull();
    }

    @Test
    void handleDefectIdChange_should_throw_NotFoundException_on_unknown_defect_id() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("unknown", DefectExistence.EXISTS, ProblemStatus.CLOSED, oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId("unknown"))).thenReturn(Boolean.TRUE);
        when(defectAdapter.getStatuses(aProjectId, Collections.singletonList("unknown"))).thenReturn(Collections.emptyList());

        // WHEN
        try {
            cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);
            fail("Call did not throw NotFoundException");
        } catch (NotFoundException e) {
            // THEN
            assertThat(e.getMessage()).isEqualTo("The work item does not exist: please verify the ID, or it has perhaps been removed.");
            assertThat(e.getResourceName()).isEqualTo("defect");
            assertThat(e.getErrorKey()).isEqualTo("not_found");
        }
    }

    @Test
    void handleDefectIdChange_should_set_EXISTS_and_status_OPEN_when_defect_is_open_in_tracking_system() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("new", DefectExistence.NONEXISTENT, ProblemStatus.CLOSED, oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId("new"))).thenReturn(Boolean.TRUE);
        when(defectAdapter.getStatuses(aProjectId, Collections.singletonList("new"))).thenReturn(Collections.singletonList(
                new Defect("new", ProblemStatus.OPEN, null)));

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isEqualTo("new");
        assertThat(problemDto.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problemDto.getClosingDateTime()).isNull();
    }

    @Test
    void handleDefectIdChange_should_set_EXISTS_and_status_CLOSED_when_defect_is_closed_in_tracking_system() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date newDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = problemDTO("new", null, ProblemStatus.OPEN, null);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId("new"))).thenReturn(Boolean.TRUE);
        when(defectAdapter.getStatuses(aProjectId, Collections.singletonList("new"))).thenReturn(Collections.singletonList(
                new Defect("new", ProblemStatus.CLOSED, newDate)));

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isEqualTo("new");
        assertThat(problemDto.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problemDto.getClosingDateTime()).isEqualTo(newDate);
    }

    @Test
    void getProblemErrors_throwsNotFoundException_whenNoProblemFound() {
        // GIVEN
        Pageable pageable = mock(Pageable.class);

        // WHEN
        when(problemRepository.findByProjectIdAndId(1, 2)).thenReturn(null);

        // THEN
        assertThatThrownBy(() -> cut.getProblemErrors(1, 2, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("message", Messages.NOT_FOUND_PROBLEM)
                .hasFieldOrPropertyWithValue("resourceName", Entities.PROBLEM);
    }

    @Test
    void getProblemErrors_shouldUseIdDescOrdering_whenNoOrdering() throws NotFoundException {
        // GIVEN
        Problem problem = mock(Problem.class);
        Pageable pageable = PageRequest.of(0, 1);

        ProblemPattern problemPattern1 = mock(ProblemPattern.class);
        ProblemPattern problemPattern2 = mock(ProblemPattern.class);
        ProblemPattern problemPattern3 = mock(ProblemPattern.class);

        List<ProblemPattern> problemPatterns = Arrays.asList(problemPattern1, problemPattern2, problemPattern3);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // WHEN
        when(problemRepository.findByProjectIdAndId(1, 2)).thenReturn(problem);
        when(problem.getPatterns()).thenReturn(problemPatterns);
        when(errorService.getErrors(eq(problemPatterns), pageableCaptor.capture())).thenReturn(null);

        // THEN
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = cut.getProblemErrors(1, 2, pageable);
        assertThat(errors).isNull();
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(usedPageable.getPageSize()).isEqualTo(pageable.getPageSize());
        assertThat(usedPageable.getSort()).isEqualTo(Sort.by(Direction.DESC, "id"));
    }

    @Test
    void getProblemErrors_shouldAddIdDescOrderingAsFirstOrdering_whenOrderingFilledInPageable() throws NotFoundException {
        // GIVEN
        Problem problem = mock(Problem.class);
        Order order = new Order(Direction.ASC, "test");
        Pageable pageable = PageRequest.of(0, 1, Sort.by(order));

        ProblemPattern problemPattern1 = mock(ProblemPattern.class);
        ProblemPattern problemPattern2 = mock(ProblemPattern.class);
        ProblemPattern problemPattern3 = mock(ProblemPattern.class);

        List<ProblemPattern> problemPatterns = Arrays.asList(problemPattern1, problemPattern2, problemPattern3);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // WHEN
        when(problemRepository.findByProjectIdAndId(1, 2)).thenReturn(problem);
        when(problem.getPatterns()).thenReturn(problemPatterns);
        when(errorService.getErrors(eq(problemPatterns), pageableCaptor.capture())).thenReturn(null);

        // THEN
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = cut.getProblemErrors(1, 2, pageable);
        assertThat(errors).isNull();
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(usedPageable.getPageSize()).isEqualTo(pageable.getPageSize());
        assertThat(usedPageable.getSort()).isEqualTo(Sort.by(new Order(Direction.DESC, "id"), order));
    }

    @Test
    void getProblemErrors_returnErrorServiceGetErrorsResult() throws NotFoundException {
        // GIVEN
        Problem problem = mock(Problem.class);
        Pageable pageable = PageRequest.of(0, 1);
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> getErrorsResult = mock(Page.class);

        ProblemPattern problemPattern1 = mock(ProblemPattern.class);
        ProblemPattern problemPattern2 = mock(ProblemPattern.class);
        ProblemPattern problemPattern3 = mock(ProblemPattern.class);

        List<ProblemPattern> problemPatterns = Arrays.asList(problemPattern1, problemPattern2, problemPattern3);

        // WHEN
        when(problemRepository.findByProjectIdAndId(1, 2)).thenReturn(problem);
        when(problem.getPatterns()).thenReturn(problemPatterns);
        when(errorService.getErrors(eq(problemPatterns), any(Pageable.class))).thenReturn(getErrorsResult);

        // THEN
        Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO> errors = cut.getProblemErrors(1, 2, pageable);
        assertThat(errors).isEqualTo(getErrorsResult);
    }

    public ProblemDTO problemDTO(String defectId, DefectExistence defectExistence, ProblemStatus status, Date closingDateTime) {
        ProblemDTO problemDTO = new ProblemDTO(null, null, null, defectId, null);
        problemDTO.setDefectExistence(defectExistence);
        problemDTO.setStatus(status);
        problemDTO.setClosingDateTime(closingDateTime);
        return problemDTO;
    }

}
