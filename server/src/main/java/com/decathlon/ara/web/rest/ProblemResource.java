package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.service.ProblemService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.error.ErrorWithExecutedScenarioAndRunAndExecutionDTO;
import com.decathlon.ara.service.dto.problem.ProblemDTO;
import com.decathlon.ara.service.dto.problem.ProblemFilterDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithAggregateDTO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsAndAggregateTDO;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.response.PickUpPatternDTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Problems.
 */
@RestController
@RequestMapping(ProblemResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemResource {

    private static final String NAME = Entities.PROBLEM;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final ProblemService service;

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
    public ResponseEntity<ProblemWithPatternsDTO> create(@PathVariable String projectCode, @Valid @RequestBody ProblemWithPatternsDTO dtoToCreate) {
        if (dtoToCreate.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.idMustBeEmpty(NAME)).build();
        }
        try {
            ProblemWithPatternsDTO createdDto = service.create(projectService.toId(projectCode), dtoToCreate);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + createdDto.getId(), projectCode))
                    .headers(HeaderUtil.entityCreated(NAME, createdDto.getId()))
                    .body(createdDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET one entity
     *
     * @param projectCode the code of the project in which to work
     * @param id the id of the entity to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the entity, or with status 404 (Not Found)
     */
    @GetMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ProblemWithPatternsAndAggregateTDO> getOne(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.findOneWithPatterns(projectService.toId(projectCode), id));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET the errors associated to one problem.
     *
     * @param projectCode the code of the project in which to work
     * @param id       the id of the problem associated to the errors to retrieve
     * @param pageable the meta-data of the requested page
     * @return the ResponseEntity with status 200 (OK) and with body containing a page of errors of the problem
     */
    @GetMapping("/{id:[0-9]+}/errors")
    @Timed
    public ResponseEntity<Page<ErrorWithExecutedScenarioAndRunAndExecutionDTO>> getProblemErrors(@PathVariable String projectCode, @PathVariable long id, Pageable pageable) {
        try {
            return ResponseEntity.ok().body(service.getProblemErrors(projectService.toId(projectCode), id, pageable));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * DELETE one entity.
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
     * GET all problems matching the given filter.
     *
     * @param projectCode the code of the project in which to work
     * @param filter   the search terms
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body containing a page of open problems
     */
    @PostMapping("/filter")
    @Timed
    public ResponseEntity<Page<ProblemWithAggregateDTO>> getMatchingOnes(@PathVariable String projectCode, @RequestBody ProblemFilterDTO filter, Pageable pageable) {
        try {
            return ResponseEntity.ok().body(service.findMatchingProblems(projectService.toId(projectCode), filter, pageable));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Append a new pattern to the given problem.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the problem where to append the pattern to
     * @param newPatternDto the new pattern to append to the existing problem
     * @return the ResponseEntity with status 200 (OK) and with body containing the saved problem pattern
     */
    @PostMapping("/{id:[0-9]+}/append-pattern")
    @Timed
    public ResponseEntity<ProblemPatternDTO> appendPattern(@PathVariable String projectCode, @PathVariable long id, @Valid @RequestBody ProblemPatternDTO newPatternDto) {
        try {
            ProblemPatternDTO savedPattern = service.appendPattern(projectService.toId(projectCode), id, newPatternDto);
            return ResponseEntity
                    .created(HeaderUtil.uri(PATH + "/" + id + "/" + savedPattern.getId(), projectCode))
                    .headers(HeaderUtil.entityCreated(Entities.PROBLEM_PATTERN, savedPattern.getId()))
                    .body(savedPattern);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to update an existing problem properties (without affecting its patterns).
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the entity to update
     * @param dtoToUpdate the entity to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ProblemDTO> updateProperties(@PathVariable String projectCode, @PathVariable long id, @Valid @RequestBody ProblemDTO dtoToUpdate) {
        dtoToUpdate.setId(Long.valueOf(id)); // HTTP PUT requires the URL to be the URL of the entity
        try {
            ProblemDTO updatedDto = service.updateProperties(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(NAME, updatedDto.getId()))
                    .body(updatedDto);
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Move a (source) pattern from its own problem to another (destination) problem.
     * If the source problem becomes without pattern, it is removed.
     *
     * @param projectCode the code of the project in which to work
     * @param destinationProblemId the problem where to move the pattern to
     * @param sourcePatternId      the pattern to be moved
     * @return the destination problem, and the source problem if the source problem has been removed (because it now has no pattern)
     */
    @PostMapping("/{destinationProblemId:[0-9]+}/pick-up-pattern/{sourcePatternId:[0-9]+}")
    @Timed
    public ResponseEntity<PickUpPatternDTO> pickUpPattern(@PathVariable String projectCode, @PathVariable long destinationProblemId, @PathVariable long sourcePatternId) {
        try {
            return ResponseEntity.ok().body(service.pickUpPattern(projectService.toId(projectCode), destinationProblemId, sourcePatternId));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to close a problem while assigning it a root cause.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the problem to close
     * @param rootCauseId the ID of the root-cause to set before closing the problem
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}/close/{rootCauseId:[0-9]+}")
    @Timed
    public ResponseEntity<ProblemDTO> close(@PathVariable String projectCode, @PathVariable long id, @PathVariable long rootCauseId) {
        try {
            return ResponseEntity.ok().body(service.close(projectService.toId(projectCode), id, rootCauseId));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to reopen a closed problem. Does nothing if the problem is already open.
     *
     * @param projectCode the code of the project in which to work
     * @param id the ID of the problem to reopen
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}/reopen")
    @Timed
    public ResponseEntity<ProblemDTO> reopen(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.reopen(projectService.toId(projectCode), id));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Query the defect status from the external defect tracking system, update the problem status and return the
     * updated problem.<br>
     * Does nothing if no defect ID is assigned.<br>
     * If the system can be contacted, the problem's defectExistence gets changed to EXISTS or NONEXISTENT.<br>
     * Otherwise, a 502 error (Bad Gateway) is returned with a message indicating the system cannot be contacted.
     *
     * @param projectCode the code of the project in which to work
     * @param id the id of the problem to refresh its defect
     * @return the ResponseEntity with status 200 (OK) and with body the entity, or with status 404 (Not Found), or with
     * status 502 if the defect tracking system cannot be reached or returned an error
     */
    @PutMapping("/{id:[0-9]+}/refresh-defect-status")
    @Timed
    public ResponseEntity<ProblemDTO> refreshDefectStatus(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.refreshDefectStatus(projectService.toId(projectCode), id));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Recompute the firstSeenDateTime and lastSeenDateTime of all Problems. This should never be necessary, unless an
     * external event modified data in database without using the ARA APIs.
     *
     * @param projectCode the code of the project in which to work
     * @return a 404 error if the project does not exist
     */
    @PostMapping("/recompute-first-and-last-seen-date-times")
    @Timed
    public ResponseEntity<Void> recomputeFirstAndLastSeenDateTimes(@PathVariable String projectCode) {
        try {
            service.recomputeFirstAndLastSeenDateTimes(projectService.toId(projectCode));
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

}
