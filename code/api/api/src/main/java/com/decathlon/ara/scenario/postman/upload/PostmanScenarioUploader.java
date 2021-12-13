package com.decathlon.ara.scenario.postman.upload;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.postman.service.PostmanScenarioIndexerService;
import com.decathlon.ara.service.exception.BadRequestException;

@Component
public class PostmanScenarioUploader {

    private static final Logger LOG = LoggerFactory.getLogger(PostmanScenarioUploader.class);

    private final ScenarioUploader uploader;

    private final PostmanScenarioIndexerService postmanScenarioIndexerService;

    public PostmanScenarioUploader(ScenarioUploader uploader,
            PostmanScenarioIndexerService postmanScenarioIndexerService) {
        this.uploader = uploader;
        this.postmanScenarioIndexerService = postmanScenarioIndexerService;
    }

    /**
     * Upload the Postman collection set of a test type.
     *
     * @param projectId  the ID of the project in which to work
     * @param sourceCode the source-code determining the location of the files that are uploaded
     * @param zipFile    the ZIP file containing .json files representing Postman collection files
     * @throws BadRequestException if the source cannot be found, the source code is not using POSTMAN technology, or something goes wrong while parsing the collection contents
     */
    public void uploadPostman(long projectId, String sourceCode, File zipFile) throws BadRequestException {
        LOG.info("SCENARIO|postman|Beginning Postman scenarios ({}) upload", sourceCode);
        uploader.processUploadedContent(projectId, sourceCode, Technology.POSTMAN, source -> {
            try {
                LOG.info("SCENARIO|postman|Starting Postman scenarios extraction...");
                return postmanScenarioIndexerService.extractScenarios(source, zipFile);
            } catch (IOException e) {
                LOG.error("SCENARIO|postman|Cannot parse uploaded Postman collections ZIP for source {}", sourceCode, e);
                throw new BadRequestException("Cannot parse uploaded Postman collections ZIP", Entities.SCENARIO, "cannot_parse_zip");
            }
        });
        LOG.info("SCENARIO|postman|Postman scenarios ({}) upload complete", sourceCode);
    }
}
