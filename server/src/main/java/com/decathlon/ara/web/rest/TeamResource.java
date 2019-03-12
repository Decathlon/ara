package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.TeamService;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import com.decathlon.ara.Entities;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Teams.
 */
@RestController
@RequestMapping(TeamResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TeamResource {

    private static final String NAME = Entities.TEAM;
    static final String PATH =  PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final TeamService service;

    @NonNull
    private final ProjectService projectService;

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param dtoToCreate the entity to create
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an ID
     */
    @PostMapping("")
    @Timed
    public ResponseEntity<TeamDTO> create(@PathVariable String projectCode, @Valid @RequestBody TeamDTO dtoToCreate) {
        if (dtoToCreate.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            TeamDTO createdDto = service.create(projectService.toId(projectCode), dtoToCreate);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId(), projectCode))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<TeamDTO> update(@PathVariable String projectCode, @PathVariable Long id, @Valid @RequestBody TeamDTO dtoToUpdate) {
        dtoToUpdate.setId(id); // HTTP PUT requires the URL to be the URL of the entity
        try {
            TeamDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping("")
    @Timed
    public ResponseEntity<List<TeamDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAll(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the entity to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable long id) {
        try {
            service.delete(projectService.toId(projectCode), id);
            return ResponseUtil.deleted(NAME, id);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
