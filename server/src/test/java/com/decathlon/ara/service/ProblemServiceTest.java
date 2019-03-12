package com.decathlon.ara.service;

import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.ci.service.DateService;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.repository.CycleDefinitionRepository;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.ProblemPatternRepository;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.repository.custom.util.JpaCacheManager;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.ErrorWithExecutedScenarioAndRunAndExecutionMapper;
import com.decathlon.ara.service.mapper.ProblemAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemFilterMapper;
import com.decathlon.ara.service.mapper.ProblemMapper;
import com.decathlon.ara.service.mapper.ProblemPatternMapper;
import com.decathlon.ara.service.mapper.ProblemWithAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemWithPatternsAndAggregateMapper;
import com.decathlon.ara.service.mapper.ProblemWithPatternsMapper;
import com.decathlon.ara.service.mapper.RootCauseMapper;
import com.decathlon.ara.service.mapper.TeamMapper;
import com.decathlon.ara.defect.bean.Defect;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ErrorRepository errorRepository;

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
    private ProblemMapper problemMapper;

    @Mock
    private ProblemFilterMapper problemFilterMapper;

    @Mock
    private ProblemWithAggregateMapper problemWithAggregateMapper;

    @Mock
    private ProblemWithPatternsMapper problemWithPatternsMapper;

    @Mock
    private ProblemPatternMapper problemPatternMapper;

    @Mock
    private ErrorWithExecutedScenarioAndRunAndExecutionMapper errorWithExecutedScenarioAndRunAndExecutionMapper;

    @Mock
    private ProblemAggregateMapper problemAggregateMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private RootCauseMapper rootCauseMapper;

    @Mock
    private ProblemWithPatternsAndAggregateMapper problemWithPatternsAndAggregateMapper;

    @Mock
    private JpaCacheManager jpaCacheManager;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ProblemService cut;

    @Test
    public void handleDefectIdChange_should_do_nothing_if_defect_did_not_change() throws BadRequestException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId(oldDefectId)
                .withDefectExistence(DefectExistence.EXISTS)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);

        // WHEN
        cut.handleDefectIdChange(aProjectId, problemDto, oldDefectId);

        // THEN
        assertThat(problemDto.getDefectId()).isEqualTo(oldDefectId);
        assertThat(problemDto.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problemDto.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problemDto.getClosingDateTime()).isEqualTo(oldDate);
    }

    @Test
    public void handleDefectIdChange_should_remove_defect_traces_when_unsetting_it() throws BadRequestException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("")
                .withDefectExistence(DefectExistence.EXISTS)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);
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
    public void handleDefectIdChange_should_throw_BadRequestException_on_bad_defect_id_format() {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("bad")
                .withDefectExistence(DefectExistence.EXISTS)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId(aProjectId, "bad"))).thenReturn(Boolean.FALSE);
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
    public void handleDefectIdChange_should_set_existence_UNKNOWN_and_status_OPEN_when_defect_tracking_system_do_not_respond() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("new")
                .withDefectExistence(DefectExistence.EXISTS)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId(aProjectId, "new"))).thenReturn(Boolean.TRUE);
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
    public void handleDefectIdChange_should_throw_NotFoundException_on_unknown_defect_id() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("unknown")
                .withDefectExistence(DefectExistence.EXISTS)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId(aProjectId, "unknown"))).thenReturn(Boolean.TRUE);
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
    public void handleDefectIdChange_should_set_EXISTS_and_status_OPEN_when_defect_is_open_in_tracking_system() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date oldDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("new")
                .withDefectExistence(DefectExistence.NONEXISTENT)
                .withStatus(ProblemStatus.CLOSED)
                .withClosingDateTime(oldDate);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId(aProjectId, "new"))).thenReturn(Boolean.TRUE);
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
    public void handleDefectIdChange_should_set_EXISTS_and_status_CLOSED_when_defect_is_closed_in_tracking_system() throws BadRequestException, FetchException {
        // GIVEN
        long aProjectId = 42;
        Date newDate = new Date();
        String oldDefectId = "old";
        ProblemDTO problemDto = new ProblemDTO()
                .withDefectId("new")
                .withDefectExistence(null)
                .withStatus(ProblemStatus.OPEN)
                .withClosingDateTime(null);
        when(defectService.getAdapter(aProjectId)).thenReturn(Optional.of(defectAdapter));
        when(Boolean.valueOf(defectAdapter.isValidId(aProjectId, "new"))).thenReturn(Boolean.TRUE);
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

}
