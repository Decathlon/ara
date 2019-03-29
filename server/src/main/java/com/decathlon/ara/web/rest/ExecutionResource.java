package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.ExecutionService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.execution.ExecutionCriteriaDTO;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithHandlingCountsDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

/**
 * REST controller for managing Cycle Runs.
 */
@Slf4j
@RestController
@RequestMapping(ExecutionResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionResource {

    static final String PATH = PROJECT_API_PATH + "/" + Entities.EXECUTION + "s";
    private static final String VALIDATION_ERRROR = "validation";

    @NonNull
    private final ExecutionService service;

    @NonNull
    private final ExecutionHistoryService executionHistoryService;

    @NonNull
    private final ProjectService projectService;

    /**
     * GET a paginated list of all entities.
     *
     * @param projectCode the code of the project in which to work
     * @param pageable    the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body containing a page of entities
     */
    @GetMapping("")
    @Timed
    public ResponseEntity<Page<ExecutionWithHandlingCountsDTO>> getPage(@PathVariable String projectCode, Pageable pageable) {
        try {
            return ResponseEntity.ok().body(service.findAll(projectService.toId(projectCode), pageable));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the single entity to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the teamDTO, or with status 404 (Not Found)
     */
    @GetMapping("/{id:[0-9]+}")
    @Timed
    public ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> getOne(@PathVariable String projectCode, @PathVariable long id) {
        ExecutionCriteriaDTO criteria = new ExecutionCriteriaDTO();
        criteria.setWithSucceed(false);
        try {
            return ResponseEntity.ok().body(service.findOneWithRuns(projectService.toId(projectCode), id, criteria));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET one entity.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the id of the single entity to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the teamDTO, or with status 404 (Not Found)
     */
    @GetMapping("/{id:[0-9]+}/with-successes")
    @Timed
    public ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> getOneWithSuccesses(@PathVariable String projectCode, @PathVariable long id) {
        ExecutionCriteriaDTO criteria = new ExecutionCriteriaDTO();
        criteria.setWithSucceed(true);
        try {
            return ResponseEntity.ok().body(service.findOneWithRuns(projectService.toId(projectCode), id, criteria));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to discard an execution while assigning it a discard reason.
     *
     * @param projectCode   the code of the project in which to work
     * @param id            the ID of the execution to discard
     * @param discardReason the reason explaining to discarding of the execution
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}/discard")
    @Timed
    public ResponseEntity<ExecutionDTO> discard(@PathVariable String projectCode, @PathVariable long id, @RequestBody String discardReason) {
        try {
            return ResponseEntity.ok().body(service.discard(projectService.toId(projectCode), id, discardReason));
        } catch (BadRequestException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * PUT to un-discard an execution and reset its discard reason. Does nothing if the execution is not discarded.
     *
     * @param projectCode the code of the project in which to work
     * @param id          the ID of the execution to un-discard
     * @return the ResponseEntity with status 200 (OK) and with body the updated entity, or with status 400 (Bad Request) if the entity is not
     * valid, or with status 500 (Internal Server Error) if the entity couldn't be updated
     */
    @PutMapping("/{id:[0-9]+}/un-discard")
    @Timed
    public ResponseEntity<ExecutionDTO> unDiscard(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.unDiscard(projectService.toId(projectCode), id));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/latest")
    @Timed
    public ResponseEntity<List<ExecutionHistoryPointDTO>> getLatestExecutionHistories(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(executionHistoryService.getLatestExecutionHistories(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/{id:[0-9]+}/history")
    @Timed
    public ResponseEntity<ExecutionHistoryPointDTO> getExecutionHistory(@PathVariable String projectCode, @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(executionHistoryService.getExecution(projectService.toId(projectCode), id));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * The execution job is about to complete: using this request, the job signals this to ARA, and ARA sets a flag on the execution.<br>
     * When the crawler will next run, it will unset the flag at the same time as indexing the very latest data about the job.<br>
     * Between the time the flag is set and the flag is unset, the request to get quality status will reply STILL_COMPUTING
     * as it cannot guarantee it has all necessary information yet in order to return the definitive quality status.<br>
     * Only a crawling started AFTER the completion-request is guaranteed to have the latest data, so only such crawling will unset the flag.
     *
     * @param projectCode the code of the project in which to work
     * @param jobUrl      the job URL of the execution to mark for completion
     * @return a 404 error if the project does not exist
     */
    @PostMapping("/request-completion")
    @Timed
    public ResponseEntity<Void> requestCompletion(@PathVariable String projectCode, @RequestParam String jobUrl) {
        try {
            service.requestCompletion(projectService.toId(projectCode), jobUrl);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Get the quality status of the execution for the given job.<br>
     * WARNING: to be sure to get reliable results, /request-completion must be called prior to this call.<br>
     * "STILL_COMPUTING" is returned if it too soon to get the definitive quality status: in this case, you must
     * query again every few tens of seconds until something else is returned: it will be the definitive quality status.
     *
     * @param projectCode the code of the project in which to work
     * @param jobUrl      the job URL of the execution
     * @return "STILL_COMPUTING" if the flag set by /request-completion is still there (indexation is not done yet),
     * or one of the {@link QualityStatus} enumeration names when the
     * definitive quality status of the execution is known
     */
    @GetMapping("/quality-status")
    @Timed
    public ResponseEntity<String> getQualityStatus(@PathVariable String projectCode, @RequestParam String jobUrl) {
        try {
            return ResponseEntity.ok().body(service.getQualityStatus(projectService.toId(projectCode), jobUrl));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * GET latest blocking and eligible executions for each branch
     *
     * @param projectCode the code of the project in which to work
     * @return The ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping("/latest-eligible-versions")
    @Timed
    public ResponseEntity<List<ExecutionDTO>> getLatestEligibleVersions(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.getLatestEligibleVersions(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    /**
     * Retrieve the zip file POSTed to the given project and unzip it.
     *
     * @param projectCode the code of the project related to the given postman results.
     * @param branch      the branch of the given execution
     * @param cycle       the cycle of the given execution
     * @param zipFile     a zip containing Postman result to index for the given project.
     * @return The ResponseEntity with status 200 (OK) if the zip was correctly extracted and ready to be indexed, a
     * 400 (BAD REQUEST) if the zip can't be read or the given project hasn't enabled the file indexing or a 500 if an
     * internal error occurs during the indexation.
     */
    @PostMapping(value = "/upload")
    public ResponseEntity<Void> upload(@PathVariable String projectCode,
                                       @RequestParam("branch") String branch,
                                       @RequestParam("cycle") String cycle,
                                       @RequestParam("zip") MultipartFile zipFile) {
        ResponseEntity<Void> result;
        log.info("Receiving new zip report for project {}...", projectCode);
        try {
            long projectId = projectService.toId(projectCode);
            service.uploadExecutionReport(projectId, projectCode, branch, cycle, zipFile);
            result = ResponseEntity.ok().build();
        } catch (NotFoundException | IllegalArgumentException e) {
            log.error("The given project doesn't exists or doesn't use the FS indexer.", e);
            result = ResponseUtil.handle(new BadRequestException(e.getMessage(), Entities.EXECUTION, VALIDATION_ERRROR));
        } catch (IOException ex) {
            log.error("Unable to index the uploaded execution.", ex);
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return result;
    }

    @PostMapping("/{id:[0-9]+}/filtered")
    public ResponseEntity<ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> getOneFiltered(@PathVariable String projectCode,
                                                                                                                                      @PathVariable long id,
                                                                                                                                      @RequestBody ExecutionCriteriaDTO criteria) {
        try {
            long projectId = projectService.toId(projectCode);
            return ResponseEntity.ok(service.findOneWithRuns(projectId, id, criteria));
        } catch (BadRequestException ex) {
            return ResponseUtil.handle(ex);
        }
    }

}
