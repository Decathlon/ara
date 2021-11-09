package com.decathlon.ara.web.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.project.ProjectDTO;

@RestController
@RequestMapping("/api/admin/projects")
public class ProjectAdministrationResource {

    private ProjectService projectService;

    public ProjectAdministrationResource(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * GET all entities.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping
    public List<ProjectDTO> getAll() {
        return projectService.findAll();
    }

}
