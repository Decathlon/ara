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
