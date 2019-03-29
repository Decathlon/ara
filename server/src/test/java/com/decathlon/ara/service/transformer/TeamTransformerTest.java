package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Team;
import com.decathlon.ara.service.dto.team.TeamDTO;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TeamTransformerTest {

    @InjectMocks
    private TeamTransformer cut;

    @Test
    public void toDto_should_transform_the_object() {
        // Given
        Team team = new Team(1L, 35L, "name", true, true, new ArrayList<>());
        // When
        TeamDTO result = cut.toDto(team);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.isAssignableToFunctionalities()).isTrue();
        Assertions.assertThat(result.isAssignableToProblems()).isTrue();
    }

    @Test
    public void toDto_should_return_empty_object_on_null() {
        // When
        TeamDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.isAssignableToFunctionalities()).isFalse();
        Assertions.assertThat(result.isAssignableToProblems()).isFalse();
    }
}