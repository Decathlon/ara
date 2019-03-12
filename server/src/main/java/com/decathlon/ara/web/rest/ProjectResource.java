package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.Entities;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

/**
 * REST controller for managing Projects.
 */
@RestController
@RequestMapping(ProjectResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectResource {

    private static final String NAME = Entities.PROJECT;
    static final String PATH = API_PATH + "/" + NAME + "s";

    @NonNull
    private final ProjectService service;

    /**
     * POST to create a new entity.
     *
     * @param dtoToCreate the entity to create
     * @return the ResponseEntity with status 201 (Created) and with body the new entity, or with status 400 (Bad Request) if the entity has
     * already an ID
     */
    @PostMapping("")
    @Timed
    public ResponseEntity<ProjectDTO> create(@Valid @RequestBody ProjectDTO dtoToCreate) {
        if (dtoToCreate.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            ProjectDTO createdDto = service.create(dtoToCreate);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId()))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (NotUniqueException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing entity.
     *
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ProjectDTO> update(@PathVariable Long id, @Valid @RequestBody ProjectDTO dtoToUpdate) {
        dtoToUpdate.setId(id); // HTTP PUT requires the URL to be the URL of the entity
        try {
            ProjectDTO updatedDto = service.update(dtoToUpdate);
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
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping("")
    @Timed
    public List<ProjectDTO> getAll() {
        return service.findAll();
    }

}
