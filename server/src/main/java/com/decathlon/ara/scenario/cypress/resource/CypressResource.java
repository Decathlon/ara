package com.decathlon.ara.scenario.cypress.resource;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cypress.upload.CypressScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@Slf4j
@RestController
@RequestMapping(CypressResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CypressResource {

    static final String PATH = PROJECT_API_PATH + "/cypress";

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final CypressScenarioUploader cypressScenarioUploader;

    @PostMapping("scenarios/upload/{sourceCode}")
    @Timed
    public ResponseEntity<Void> uploadCucumberScenarios(@PathVariable String projectCode, @PathVariable String sourceCode, @RequestBody List<Feature> features) {
        try {
            cypressScenarioUploader.uploadScenarios(projectService.toId(projectCode), sourceCode, features);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            log.error("Failed to upload Cypress (Cucumber) scenarios for source code {}", sourceCode, e);
            return ResponseUtil.handle(e);
        }
    }

}
