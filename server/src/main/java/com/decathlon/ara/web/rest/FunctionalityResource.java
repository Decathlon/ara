package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.coverage.CoverageService;
import com.decathlon.ara.service.FunctionalityService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.coverage.CoverageDTO;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.functionality.FunctionalityWithChildrenDTO;
import com.decathlon.ara.service.dto.request.MoveFunctionalityDTO;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioDTO;
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
 * REST controller for managing Functionalities.
 */
@RestController
@RequestMapping(FunctionalityResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FunctionalityResource {

    static final String PATH = PROJECT_API_PATH + "/functionalities";
    private static final String NAME = Entities.FUNCTIONALITY;

    @NonNull
    private final FunctionalityService service;

    @NonNull
    private final CoverageService coverageService;

    @NonNull
    private final ProjectService projectService;

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the tree of entities in body
     */
    @GetMapping("")
    @Timed
    public ResponseEntity<List<FunctionalityWithChildrenDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAllAsTree(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing functionality (or a folder).
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<FunctionalityDTO> updateProperties(@PathVariable String projectCode, @PathVariable Long id, @Valid @RequestBody FunctionalityDTO dtoToUpdate) {
        dtoToUpdate.setId(id); // HTTP PUT requires the URL to be the URL of the entity
        try {
            FunctionalityDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST to create a new entity.
     *
     * @param projectCode the code of the project in which to work
     * @param newDto the entity to create, relative to another entity
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an ID
     */
    @PostMapping("")
    @Timed
    public ResponseEntity<FunctionalityDTO> create(@PathVariable String projectCode, @Valid @RequestBody NewFunctionalityDTO newDto) {
        if (newDto.getFunctionality().getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            FunctionalityDTO createdDto = service.create(projectService.toId(projectCode), newDto);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId(), projectCode))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity, and its children, if any.
     *
     * @param projectCode the code of the project in which to work
     * @param id the id of the entity to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<Void> delete(@PathVariable String projectCode, @PathVariable long id) {
        try {
            service.delete(projectService.toId(projectCode), id);
            return ResponseUtil.deleted(NAME, id);
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * POST to move a functionality or folder to another place in the tree.
     *
     * @param projectCode the code of the project in which to work
     * @param moveRequest the entity to move, relative to another entity
     * @return the ResponseEntity with status 200 (OK)
     */
    @PostMapping("/move")
    @Timed
    public ResponseEntity<FunctionalityDTO> move(@PathVariable String projectCode, @Valid @RequestBody MoveFunctionalityDTO moveRequest) {
        try {
            FunctionalityDTO updatedDto = service.move(projectService.toId(projectCode), moveRequest);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityMoved(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET scenarios covering the given functionality.
     *
     * @param projectCode the code of the project in which to work
     * @param id the ID of the functionality to query
     * @return the ResponseEntity with status 200 (OK) and the list of scenarios covering the functionality, status 404 if the entity is not found, or 400 if requesting scenarios of a folder
     */
    @GetMapping("/{id:[0-9]+}/scenarios")
    @Timed
    public ResponseEntity<List<ScenarioDTO>> getScenarios(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.findScenarios(projectService.toId(projectCode), id));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/coverage")
    @Timed
    public ResponseEntity<CoverageDTO> getCoverage(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(coverageService.computeCoverage(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

}
