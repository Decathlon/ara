package com.decathlon.ara.service.mapper;

import com.decathlon.ara.domain.Team;
import com.decathlon.ara.service.dto.team.TeamDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Team and its DTO TeamDTO.
 */
@Mapper
public interface TeamMapper extends EntityMapper<TeamDTO, Team> {

    // All methods are parameterized for EntityMapper

}
