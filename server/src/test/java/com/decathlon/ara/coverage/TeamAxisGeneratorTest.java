package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamAxisGeneratorTest {

    private static final int A_PROJECT_ID = 42;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamAxisGenerator cut;

    @Test
    public void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("team");
    }

    @Test
    public void testGetName() {
        assertThat(cut.getName()).isEqualTo("Teams");
    }

    @Test
    public void testGetPoints() {
        // GIVEN
        when(teamRepository.findAllByProjectIdOrderByName(A_PROJECT_ID)).thenReturn(Arrays.asList(
                new Team().withId(Long.valueOf(1)).withName("Team 1").withAssignableToFunctionalities(true),
                new Team().withId(Long.valueOf(2)).withName("Not assignable").withAssignableToFunctionalities(false),
                new Team().withId(Long.valueOf(3)).withName("Team 3").withAssignableToFunctionalities(true)));

        // WHEN / THEN
        assertThat(cut.getPoints(A_PROJECT_ID)).containsExactly(
                new AxisPointDTO("1", "Team 1", null),
                new AxisPointDTO("3", "Team 3", null));
    }

    @Test
    public void testGetValuePoints_without_team() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    public void testGetValuePoints_with_team() {
        // GIVEN
        Functionality functionality = new Functionality().withTeamId(Long.valueOf(1));

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "1" });
    }

}
