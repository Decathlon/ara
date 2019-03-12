package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.ProblemPatternService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.DeletePatternDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import com.decathlon.ara.Entities;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Problem Patterns.
 */
@RestController
@RequestMapping(ProblemPatternResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemPatternResource {

    private static final String NAME = Entities.PROBLEM_PATTERN;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final ProblemPatternService service;

    @NonNull
    private final ProjectService projectService;

    /**
     * DELETE one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the entity to delete
     * @return the ResponseEntity with status 200 (OK) and the deleted problem, if the pattern was the only one inside its problem
     */
    @DeleteMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<DeletePatternDTO> delete(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityDeleted(NAME, id))
                    .body(service.delete(projectService.toId(projectCode), id));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET the errors associated to one problem pattern.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the problem pattern associated to the errors to retrieve
     * @param pageable    the meta-data of the requested page
     * @return the ResponseEntity with status 200 (OK) and with body containing a page of errors of the problem pattern
     */
    @GetMapping("/{id:[0-9]+}/errors")
    @Timed
    public ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> getProblemPatternErrors(
            @PathVariable String projectCode, @PathVariable long id, Pageable pageable) {
        try {
            return ResponseEntity.ok()
                    .body(service.getProblemPatternErrors(projectService.toId(projectCode), id, pageable));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing problem pattern (and re-assign errors).
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ProblemPatternDTO> update(@PathVariable String projectCode, @PathVariable long id, @Valid @RequestBody ProblemPatternDTO dtoToUpdate) {
        dtoToUpdate.setId(Long.valueOf(id)); // HTTP PUT requires the URL to be the URL of the entity
        try {
            ProblemPatternDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
