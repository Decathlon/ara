package com.decathlon.ara.web.rest;

import com.decathlon.ara.purge.service.PurgeService;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;
import static com.decathlon.ara.web.rest.PurgeResource.PURGE_BASE_API_PATH;

@RestController
@RequestMapping(PURGE_BASE_API_PATH)
public class PurgeResource {

    public static final String PURGE_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/purge";

    private static final String FORCE = "/force";
    public static final String PURGE_FORCE_API_PATH = PURGE_BASE_API_PATH + FORCE;

    private final PurgeService purgeService;

    public PurgeResource(PurgeService purgeService) {
        this.purgeService = purgeService;
    }

    /**
     * Force a project executions purge
     * @param projectCode the project code
     * @return an http response
     */
    @DeleteMapping(FORCE)
    public ResponseEntity<Void> forcePurge(@PathVariable String projectCode) {
        purgeService.purgeExecutionsByProjectCode(projectCode);
        return ResponseUtil.deleted("Purge", projectCode);
    }

}
