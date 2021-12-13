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

package com.decathlon.ara.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.util.TestUtil;
import com.decathlon.ara.util.builder.FunctionalityBuilder;

@ExtendWith(MockitoExtension.class)
class TeamAxisGeneratorTest {

    private static final int A_PROJECT_ID = 42;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamAxisGenerator cut;

    @Test
    void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("team");
    }

    @Test
    void testGetName() {
        assertThat(cut.getName()).isEqualTo("Teams");
    }

    @Test
    void testGetPoints() {
        // GIVEN
        when(teamRepository.findAllByProjectIdOrderByName(A_PROJECT_ID)).thenReturn(Arrays.asList(
                team(Long.valueOf(1), "Team 1", true),
                team(Long.valueOf(2), "Team 2", false),
                team(Long.valueOf(3), "Team 3", true)));

        // WHEN
        List<AxisPointDTO> points = cut.getPoints(A_PROJECT_ID).toList();

        // THEN
        Assertions.assertEquals(2, points.size());
        Assertions.assertTrue(equals(points.get(0), "1", "Team 1", null));
        Assertions.assertTrue(equals(points.get(1), "3", "Team 3", null));
    }

    @Test
    void testGetValuePoints_without_team() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    void testGetValuePoints_with_team() {
        // GIVEN
        Functionality functionality = new FunctionalityBuilder().withTeamId(Long.valueOf(1)).build();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "1" });
    }

    private Team team(Long id, String name, boolean assignableToFunctionalities) {
        Team team = new Team(id, name);
        TestUtil.setField(team, "assignableToFunctionalities", assignableToFunctionalities);
        return team;
    }

    private boolean equals(AxisPointDTO result, String id, String name, String tooltip) {
        return Objects.equals(result.getId(), id) && Objects.equals(result.getName(), name) && Objects.equals(result.getTooltip(), tooltip);
    }

}
