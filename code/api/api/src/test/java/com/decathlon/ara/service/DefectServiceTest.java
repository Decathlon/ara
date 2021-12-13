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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.util.DateService;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class DefectServiceTest {

    @Mock
    private DefectAdapter defectAdapter;

    @Mock
    private SettingService settingService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private DateService dateService;

    @Spy
    @InjectMocks
    private DefectService cut;

    @Captor
    private ArgumentCaptor<List<Problem>> problemListArgument;

    @Captor
    private ArgumentCaptor<List<Defect>> defectListArgument;

    @Captor
    private ArgumentCaptor<List<String>> stringListArgument;

    @Test
    void updateStatuses_should_call_needFullIndexing_with_current_date_time() throws FetchException {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        Project project = project(projectId);
        Date startDate = new Date();
        when(dateService.now()).thenReturn(startDate);
        doReturn(Boolean.TRUE).when(cut).needFullIndexing(projectId, startDate);
        doNothing().when(cut).fullIndex(project, defectAdapter);

        // WHEN
        cut.updateStatuses(project, defectAdapter);

        // THEN
        verify(cut, times(1)).needFullIndexing(projectId, startDate);
    }

    @Test
    void updateStatuses_should_do_full_indexing_when_needed() throws FetchException {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        Project project = project(projectId);
        Date startDate = new Date();
        when(dateService.now()).thenReturn(startDate);
        doReturn(Boolean.TRUE).when(cut).needFullIndexing(projectId, startDate);
        doNothing().when(cut).fullIndex(project, defectAdapter);

        // WHEN
        cut.updateStatuses(project, defectAdapter);

        // THEN
        verify(cut, times(1)).fullIndex(project, defectAdapter);
        assertThat(cut.lastFullIndexDates.get(projectId)).isSameAs(startDate);
        assertThat(cut.lastIncrementalIndexDates.get(projectId)).isSameAs(startDate);
    }

    @Test
    void updateStatuses_should_do_incremental_indexing_when_needed() throws FetchException {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        Project project = project(projectId);
        Date lastIncrementalIndexDate = new Date(1);
        Date dateNotToUpdate = new Date(2);
        Date startDate = new Date(3);
        cut.lastFullIndexDates.put(projectId, dateNotToUpdate);
        cut.lastIncrementalIndexDates.put(projectId, lastIncrementalIndexDate);
        when(dateService.now()).thenReturn(startDate);
        doReturn(Boolean.FALSE).when(cut).needFullIndexing(projectId, startDate);
        doNothing().when(cut).incrementalIndex(project, defectAdapter, lastIncrementalIndexDate);

        // WHEN
        cut.updateStatuses(project, defectAdapter);

        // THEN
        verify(cut, times(1)).incrementalIndex(project, defectAdapter, lastIncrementalIndexDate);
        assertThat(cut.lastFullIndexDates.get(projectId)).isSameAs(dateNotToUpdate);
        assertThat(cut.lastIncrementalIndexDates.get(projectId)).isSameAs(startDate);
    }

    @Test
    void updateStatuses_should_not_crash_when_full_index_throws_exception() throws FetchException {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        Project project = project(projectId);
        Date startDate = new Date();
        when(dateService.now()).thenReturn(startDate);
        doReturn(Boolean.TRUE).when(cut).needFullIndexing(projectId, startDate);
        doThrow(new FetchException("any")).when(cut).fullIndex(project, defectAdapter);

        // WHEN
        Assertions.assertDoesNotThrow(() -> cut.updateStatuses(project, defectAdapter));
    }

    @Test
    void updateStatuses_should_not_crash_when_incremental_index_throws_exception() throws FetchException {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        Project project = project(projectId);
        Date lastIncrementalIndexDate = new Date(1);
        Date startDate = new Date();
        cut.lastIncrementalIndexDates.put(projectId, lastIncrementalIndexDate);
        when(dateService.now()).thenReturn(startDate);
        doReturn(Boolean.FALSE).when(cut).needFullIndexing(projectId, startDate);
        doThrow(new FetchException("any")).when(cut).incrementalIndex(project, defectAdapter, lastIncrementalIndexDate);

        // WHEN
        Assertions.assertDoesNotThrow(() -> cut.updateStatuses(project, defectAdapter));
    }

    @Test
    void needFullIndexing_should_return_true_if_no_full_index_done_yet() {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        final Date lastIncrementalIndexDate = new Date();
        cut.lastIncrementalIndexDates.put(projectId, lastIncrementalIndexDate);

        // WHEN
        final boolean needing = cut.needFullIndexing(projectId, new Date());

        // THEN
        assertThat(needing).isTrue();
    }

    @Test
    void needFullIndexing_should_return_true_if_no_incremental_index_done_yet_because_there_is_no_baseline_date() {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        final Date lastFullIndexDate = new Date();
        cut.lastFullIndexDates.put(projectId, lastFullIndexDate);

        // WHEN
        final boolean needing = cut.needFullIndexing(projectId, new Date());

        // THEN
        assertThat(needing).isTrue();
    }

    @Test
    void needFullIndexing_should_return_true_if_last_incremental_date_is_one_hour_ago() {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        final Date lastFullIndexDate = new Date();
        final Date anyDate = new Date();
        cut.lastFullIndexDates.put(projectId, lastFullIndexDate);
        cut.lastIncrementalIndexDates.put(projectId, anyDate);
        final Date since = new Date();
        doReturn(Long.valueOf(1)).when(cut).diffHours(eq(since), eq(lastFullIndexDate));

        // WHEN
        final boolean needing = cut.needFullIndexing(projectId, since);

        // THEN
        assertThat(needing).isTrue();
    }

    @Test
    void needFullIndexing_should_return_false_if_last_incremental_date_is_less_than_one_ago() {
        // GIVEN
        final Long projectId = Long.valueOf(12);
        final Date lastFullIndexDate = new Date();
        final Date anyDate = new Date();
        cut.lastFullIndexDates.put(projectId, lastFullIndexDate);
        cut.lastIncrementalIndexDates.put(projectId, anyDate);
        Date now = new Date();
        doReturn(Long.valueOf(0)).when(cut).diffHours(same(now), same(lastFullIndexDate));

        // WHEN
        final boolean needing = cut.needFullIndexing(projectId, now);

        // THEN
        assertThat(needing).isFalse();
    }

    @Test
    void fullIndexing_should_updateDefectAssignations_with_problems_having_defect_ids_and_with_defects_retrieved_from_defect_tracking_system() throws FetchException {
        // GIVEN
        final long aProjectId = 42;
        Project project = project(Long.valueOf(aProjectId));
        List<Problem> problemsWithDefects = Arrays.asList(
                problem("1", null, null, null),
                problem("2", null, null, null));
        when(problemRepository.findAllByProjectIdAndDefectIdIsNotEmpty(aProjectId)).thenReturn(problemsWithDefects);
        final List<Defect> statuses = Collections.singletonList(
                new Defect("2", ProblemStatus.CLOSED, new Date()));
        when(defectAdapter.getStatuses(aProjectId, Arrays.asList("1", "2"))).thenReturn(statuses);
        doNothing().when(cut).updateDefectAssignations(problemListArgument.capture(), defectListArgument.capture());

        // WHEN
        cut.fullIndex(project, defectAdapter);

        // THEN
        assertThat(problemListArgument.getValue().stream().map(Problem::getDefectId)).containsExactly("1", "2");
        assertThat(defectListArgument.getValue()).isSameAs(statuses);
    }

    @Test
    void incrementalIndex_should_update_problems_of_changed_defects() throws FetchException {
        // GIVEN
        final long aProjectId = 42;
        Project project = project(Long.valueOf(aProjectId));
        Date since = new Date();
        final Problem updatedOpenProblem = problem("updated-open", null, null, null);
        final Problem updatedClosedProblem = problem("updated-closed", null, null, null);
        List<Problem> problemsWithDefects = Arrays.asList(
                problem("", null, null, null),
                updatedOpenProblem,
                updatedClosedProblem,
                problem("not-updated", null, null, null));
        when(problemRepository.findAllByProjectIdAndDefectIdIsNotEmpty(aProjectId)).thenReturn(problemsWithDefects);
        Date closeDate = new Date();
        when(defectAdapter.getChangedDefects(eq(aProjectId), same(since))).thenReturn(Arrays.asList(
                new Defect("any1", ProblemStatus.OPEN, null),
                new Defect("updated-open", ProblemStatus.OPEN, null),
                new Defect("any2", ProblemStatus.CLOSED, closeDate),
                new Defect("updated-closed", ProblemStatus.CLOSED, closeDate)));
        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.incrementalIndex(project, defectAdapter, since);

        // THEN
        assertThat(problemListArgument.getValue())
                .hasSameElementsAs(Arrays.asList(updatedOpenProblem, updatedClosedProblem));
        assertThat(updatedOpenProblem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(updatedOpenProblem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(updatedOpenProblem.getClosingDateTime()).isNull();
        assertThat(updatedClosedProblem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(updatedClosedProblem.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(updatedClosedProblem.getClosingDateTime()).isEqualTo(closeDate);
    }

    @Test
    void incrementalIndex_should_also_try_to_refresh_still_unknown_problems() throws FetchException {
        // GIVEN
        final long aProjectId = 42;
        Project project = project(Long.valueOf(aProjectId));
        Date since = new Date();
        final Problem unknownProblem = problem("unknown", DefectExistence.UNKNOWN, null, null);
        List<Problem> problemsWithDefects = Arrays.asList(
                problem("some", null, null, null),
                unknownProblem);

        when(problemRepository.findAllByProjectIdAndDefectIdIsNotEmpty(aProjectId)).thenReturn(problemsWithDefects);
        when(defectAdapter.getChangedDefects(eq(aProjectId), same(since))).thenReturn(Collections.emptyList());
        List<Defect> defectStatuses = Collections.emptyList();
        when(defectAdapter.getStatuses(eq(aProjectId), stringListArgument.capture())).thenReturn(defectStatuses);
        doNothing().when(cut).updateDefectAssignations(problemListArgument.capture(), same(defectStatuses));

        // WHEN
        cut.incrementalIndex(project, defectAdapter, since);

        // THEN
        assertThat(stringListArgument.getValue()).containsExactly("unknown");
        verify(cut, times(1)).updateDefectAssignations(any(), same(defectStatuses));
    }

    @Test
    void updateDefectAssignations_should_set_the_defect_properties_for_an_existing_defect_that_has_just_been_created() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.UNKNOWN, null, null);
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Arrays.asList(
                new Defect("0", ProblemStatus.CLOSED, new Date()), // Don't use this value
                new Defect("1", ProblemStatus.OPEN, null));

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getClosingDateTime()).isNull();
    }

    @Test
    void updateDefectAssignations_should_update_a_problem_status_and_close_date_for_an_existing_defect_that_has_just_been_closed() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.EXISTS, null, null);
        Date closeDate = new Date();
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.singletonList(new Defect("1", ProblemStatus.CLOSED, closeDate));

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problem.getClosingDateTime()).isEqualTo(closeDate);
    }

    @Test
    void updateDefectAssignations_should_update_a_problem_close_date_for_an_existing_defect_that_has_been_reopened_and_closed_again() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.EXISTS, ProblemStatus.CLOSED, new Date(1000));
        Date closeDate = new Date();
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.singletonList(new Defect("1", ProblemStatus.CLOSED, closeDate));

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.EXISTS);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.CLOSED);
        assertThat(problem.getClosingDateTime()).isEqualTo(closeDate);
    }

    @Test
    void updateDefectAssignations_should_not_update_problem_if_all_defect_properties_are_already_set() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.EXISTS, ProblemStatus.CLOSED, new Date(1000));
        Date closeDate = new Date(1042); // Equal down to the second
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.singletonList(new Defect("1", ProblemStatus.CLOSED, closeDate));

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).isEmpty();
    }

    @Test
    void updateDefectAssignations_should_set_status_NONEXISTENT_and_reopen_problem_for_a_nonexistent_defect_that_has_finally_been_crawled() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.UNKNOWN, ProblemStatus.CLOSED, new Date());
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.emptyList();

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.NONEXISTENT);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getClosingDateTime()).isNull();
    }

    @Test
    void updateDefectAssignations_should_keep_a_problem_open_for_a_nonexistent_defect() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.NONEXISTENT, ProblemStatus.CLOSED, new Date());
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.emptyList();

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.NONEXISTENT);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getClosingDateTime()).isNull();
    }

    @Test
    void updateDefectAssignations_should_keep_a_problem_without_closing_date_for_a_nonexistent_defect() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.NONEXISTENT, ProblemStatus.OPEN, new Date());
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.emptyList();

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).hasSameElementsAs(problems);
        assertThat(problem.getDefectExistence()).isEqualTo(DefectExistence.NONEXISTENT);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.OPEN);
        assertThat(problem.getClosingDateTime()).isNull();
    }

    @Test
    void updateDefectAssignations_should_not_update_problem_if_all_properties_are_already_ok_for_problem_with_nonexistent_defect() {
        // GIVEN
        Problem problem = problem("1", DefectExistence.NONEXISTENT, ProblemStatus.OPEN, null);
        List<Problem> problems = Collections.singletonList(problem);
        List<Defect> statuses = Collections.emptyList();

        doReturn(null).when(problemRepository).saveAll(problemListArgument.capture());

        // WHEN
        cut.updateDefectAssignations(problems, statuses);

        // THEN
        assertThat(problemListArgument.getValue()).isEmpty();
    }

    @Test
    void diffHours_should_work() {
        // GIVEN
        final Date date1 = new Date(1000 * 60 * 60 + 1);
        final Date date2 = new Date(1000 * 60 * 60 * 3 + 2);

        // WHEN
        long hours = cut.diffHours(date1, date2);

        // THEN
        assertThat(hours).isEqualTo(2);
    }

    @Test
    void diffHours_should_work_in_absolute_way() {
        // GIVEN
        final Date date1 = new Date(1000 * 60 * 60 * 3 + 2);
        final Date date2 = new Date(1000 * 60 * 60 + 1);

        // WHEN
        long hours = cut.diffHours(date1, date2);

        // THEN
        assertThat(hours).isEqualTo(2);
    }

    @Test
    void areEqualDownToSeconds_should_return_true_for_two_null_dates() {
        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(null, null);

        // THEN
        assertThat(areEqual).isTrue();
    }

    @Test
    void areEqualDownToSeconds_should_return_false_for_first_date_null() {
        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(null, new Date());

        // THEN
        assertThat(areEqual).isFalse();
    }

    @Test
    void areEqualDownToSeconds_should_return_false_for_second_date_null() {
        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(new Date(), null);

        // THEN
        assertThat(areEqual).isFalse();
    }

    @Test
    void areEqualDownToSeconds_should_return_true_for_two_equal_dates() {
        // GIVEN
        final Date date = new Date();

        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(date, date);

        // THEN
        assertThat(areEqual).isTrue();
    }

    @Test
    void areEqualDownToSeconds_should_return_true_for_two_dates_separated_by_500_milliseconds() {
        // GIVEN
        final Date date1 = new Date(1000);
        final Date date2 = new Date(1500);

        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(date1, date2);

        // THEN
        assertThat(areEqual).isTrue();
    }

    @Test
    void areEqualDownToSeconds_should_return_false_for_two_dates_separated_by_more_than_1000_milliseconds() {
        // GIVEN
        final Date date1 = new Date(1000);
        final Date date2 = new Date(2001);

        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(date1, date2);

        // THEN
        assertThat(areEqual).isFalse();
    }

    @Test
    void areEqualDownToSeconds_should_return_false_for_two_dates_separated_by_more_than_1000_milliseconds_in_any_order() {
        // GIVEN
        final Date date1 = new Date(2001);
        final Date date2 = new Date(1000);

        // WHEN
        boolean areEqual = cut.areEqualDownToSeconds(date1, date2);

        // THEN
        assertThat(areEqual).isFalse();
    }

    private Project project(Long id) {
        Project project = new Project();
        TestUtil.setField(project, "id", id);
        return project;
    }

    private Problem problem(String defectId, DefectExistence defectExistence, ProblemStatus status, Date closingDateTime) {
        Problem problem = new Problem();
        problem.setDefectId(defectId);
        problem.setDefectExistence(defectExistence);
        TestUtil.setField(problem, "status", status);
        problem.setClosingDateTime(closingDateTime);
        return problem;
    }

}
