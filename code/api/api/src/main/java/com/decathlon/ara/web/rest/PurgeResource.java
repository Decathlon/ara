package com.decathlon.ara.web.rest;

import com.decathlon.ara.purge.service.PurgeService;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@RestController
@RequestMapping(PurgeResource.PATH)
public class PurgeResource {

    static final String PATH = PROJECT_API_PATH + "/purge";

    private static final String FORCE = "/force";
    public static final String FORCE_PATH = PATH + FORCE;

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
