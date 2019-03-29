package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.EffectiveProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProblemTransformerTest {

    @Mock
    private TeamTransformer teamTransformer;

    @Mock
    private RootCauseTransformer rootCauseTransformer;

    @Spy
    @InjectMocks
    private ProblemTransformer cut;

    @Test
    public void toDto_should_transform_the_do() {
        // Given
        Date closing = new Date();
        Date start = new Date(closing.getTime() - 2000L);
        Date firstSeen = new Date(start.getTime() - 2000L);
        Date lastSeen = new Date(start.getTime() - 500L);
        Team team = new Team();
        team.setId(98L);
        RootCause rootCause = new RootCause();
        rootCause.setId(25L);
        Problem value = new Problem(
                1L, 23L, "name", "comment", ProblemStatus.OPEN, team,
                // No defectId, no defectUrl, no errors due to SpringBeans.
                null, DefectExistence.NONEXISTENT, closing, rootCause, new ArrayList<>(),
                start, firstSeen, lastSeen);
        Mockito.doReturn(new TeamDTO()).when(teamTransformer).toDto(team);
        Mockito.doReturn(new RootCauseDTO()).when(rootCauseTransformer).toDto(rootCause);
        // When
        ProblemDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.getComment()).isEqualTo("comment");
        Assertions.assertThat(result.getStatus()).isEqualTo(ProblemStatus.OPEN);
        Assertions.assertThat(result.getEffectiveStatus()).isEqualTo(EffectiveProblemStatus.OPEN);
        Mockito.verify(teamTransformer).toDto(team);
        Assertions.assertThat(result.getDefectId()).isNull();
        Assertions.assertThat(result.getDefectExistence()).isEqualTo(DefectExistence.NONEXISTENT);
        Assertions.assertThat(result.getClosingDateTime()).isEqualTo(closing);
        Assertions.assertThat(result.getDefectUrl()).isNull();
        Mockito.verify(rootCauseTransformer).toDto(rootCause);
        Assertions.assertThat(result.getCreationDateTime()).isEqualTo(start);
        Assertions.assertThat(result.getFirstSeenDateTime()).isEqualTo(firstSeen);
        Assertions.assertThat(result.getLastSeenDateTime()).isEqualTo(lastSeen);
    }

    @Test
    public void toDto_should_return_empty_object_on_null() {
        // When
        ProblemDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.getComment()).isNull();
        Assertions.assertThat(result.getStatus()).isNull();
        Assertions.assertThat(result.getEffectiveStatus()).isNull();
        Mockito.verify(teamTransformer, Mockito.never()).toDto(Mockito.any());
        Assertions.assertThat(result.getDefectId()).isNull();
        Assertions.assertThat(result.getDefectExistence()).isNull();
        Assertions.assertThat(result.getClosingDateTime()).isNull();
        Mockito.verify(rootCauseTransformer, Mockito.never()).toDto(Mockito.any());
        Assertions.assertThat(result.getCreationDateTime()).isNull();
        Assertions.assertThat(result.getFirstSeenDateTime()).isNull();
        Assertions.assertThat(result.getLastSeenDateTime()).isNull();
    }

    @Test
    public void toDtos_should_transform_all() {
        // Given
        Problem problem1 = new Problem();
        problem1.setStatus(ProblemStatus.OPEN);
        Problem problem2 = new Problem();
        problem2.setStatus(ProblemStatus.OPEN);
        Problem problem3 = new Problem();
        problem3.setStatus(ProblemStatus.OPEN);
        List<Problem> values = Lists.list(problem1, problem2, problem3);
        // When
        List<ProblemDTO> result = cut.toDtos(values);
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
        List<ProblemDTO> result = cut.toDtos(new ArrayList<>());
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void toDtos_should_return_empty_list_on_null() {
        // When
        List<ProblemDTO> result = cut.toDtos(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }
}