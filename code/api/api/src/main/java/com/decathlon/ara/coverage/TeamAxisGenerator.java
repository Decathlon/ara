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

import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;

@Service
@Transactional
public class TeamAxisGenerator implements AxisGenerator {

    private final TeamRepository teamRepository;

    public TeamAxisGenerator(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public String getCode() {
        return "team";
    }

    @Override
    public String getName() {
        return "Teams";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return teamRepository.findAllByProjectIdOrderByName(projectId).stream()
                .filter(Team::isAssignableToFunctionalities)
                .map(team -> new AxisPointDTO(team.getId().toString(), team.getName(), null));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        return (functionality.getTeamId() == null ? null : new String[] { functionality.getTeamId().toString() });
    }

}
