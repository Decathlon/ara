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

package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Team;
import com.decathlon.ara.service.dto.team.TeamDTO;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Team.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class TeamTransformer {

    /**
     * Transform the given Team DO to a TeamDTO object.
     * <p>
     * Returns an empty TeamDTO if the parameter is null.
     *
     * @param team the DO to transform
     * @return the result DTO.
     */
    TeamDTO toDto(Team team) {
        TeamDTO result = new TeamDTO();
        result.setId(0L);
        if (null != team) {
            result.setId(team.getId());
            result.setName(team.getName());
            result.setAssignableToProblems(team.isAssignableToProblems());
            result.setAssignableToFunctionalities(team.isAssignableToFunctionalities());
        }
        return result;
    }
}
