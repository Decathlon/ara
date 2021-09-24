package com.decathlon.ara.scenario.postman.upload;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.postman.service.PostmanScenarioIndexerService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PostmanScenarioUploader {

    @NonNull
    private final ScenarioUploader uploader;

    @NonNull
    private final PostmanScenarioIndexerService postmanScenarioIndexerService;

    /**
     * Upload the Postman collection set of a test type.
     *
     * @param projectId  the ID of the project in which to work
     * @param sourceCode the source-code determining the location of the files that are uploaded
     * @param zipFile    the ZIP file containing .json files representing Postman collection files
     * @throws BadRequestException if the source cannot be found, the source code is not using POSTMAN technology, or something goes wrong while parsing the collection contents
     */
    public void uploadPostman(long projectId, String sourceCode, File zipFile) throws BadRequestException {
        log.info("SCENARIO|postman|Beginning postman scenarios ({}) upload", sourceCode);
        uploader.processUploadedContent(projectId, sourceCode, Technology.POSTMAN, source -> {
            try {
                return postmanScenarioIndexerService.extractScenarios(source, zipFile);
            } catch (IOException e) {
                log.error("SCENARIO|postman|Cannot parse uploaded Postman collections ZIP for source {}", sourceCode, e);
                throw new BadRequestException("Cannot parse uploaded Postman collections ZIP", Entities.SCENARIO, "cannot_parse_zip");
            }
        });
        log.info("SCENARIO|postman|Postman scenarios ({}) upload complete", sourceCode);
    }
}
