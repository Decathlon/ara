package com.decathlon.ara.service.mapper;

import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.domain.Project;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Project and its DTO ProjectDTO.
 */
@Mapper
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {

    // All methods are parameterized for EntityMapper

}
