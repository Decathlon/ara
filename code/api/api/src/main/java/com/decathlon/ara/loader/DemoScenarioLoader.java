/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.loader;

import static com.decathlon.ara.loader.DemoLoaderConstants.SOURCE_CODE_API;
import static com.decathlon.ara.loader.DemoLoaderConstants.SOURCE_CODE_WEB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.scenario.cucumber.upload.CucumberScenarioUploader;
import com.decathlon.ara.scenario.postman.upload.PostmanScenarioUploader;
import com.decathlon.ara.service.exception.BadRequestException;

/**
 * Load scenarios into the Demo project.
 */
@Service
@Transactional
public class DemoScenarioLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DemoScenarioLoader.class);

    private final PostmanScenarioUploader postmanScenarioUploader;

    private final CucumberScenarioUploader cucumberScenarioUploader;

    private final DemoLoaderService demoLoaderService;

    public DemoScenarioLoader(PostmanScenarioUploader postmanScenarioUploader,
            CucumberScenarioUploader cucumberScenarioUploader, DemoLoaderService demoLoaderService) {
        this.postmanScenarioUploader = postmanScenarioUploader;
        this.cucumberScenarioUploader = cucumberScenarioUploader;
        this.demoLoaderService = demoLoaderService;
    }

    public void createScenarios(long projectId, Map<String, Long> functionalityIds) throws BadRequestException {
        cucumberScenarioUploader.uploadCucumber(projectId, SOURCE_CODE_WEB,
                demoLoaderService.replaceFunctionalityIdPlaceholders(functionalityIds,
                        getResourceAsUtf8String("reports/demo/dry-report.json")));

        final File postmanScenariosZip;
        try {
            postmanScenariosZip = getPostmanScenariosZip(functionalityIds);
        } catch (IOException e) {
            LOG.warn("DEMO|postman|Cannot generate the ZIP of Postman scenarios: doing without functionality coverage for them", e);
            return;
        }

        try {
            postmanScenarioUploader.uploadPostman(projectId, SOURCE_CODE_API, postmanScenariosZip);
        } finally {
            FileUtils.deleteQuietly(postmanScenariosZip);
        }
    }

    /**
     * Create a Postman/Newman report ZIP with a few demo Newman reports, customized a little bit to use existing
     * functionality IDs of the project.
     *
     * @param functionalityIds a map of letters (eg. "A" for the placeholder "{{F-A}}") as keys, and functionality IDs
     *                         as values
     * @return the created ZIP file
     * @throws IOException when ZIP creation fails
     */
    private File getPostmanScenariosZip(Map<String, Long> functionalityIds) throws IOException {
        File file = File.createTempFile("ara_demo_postman_scenarios_", ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
            for (Resource resource : resourceResolver.getResources("classpath*:demo/collections/**/*.json")) {
                final String content = demoLoaderService.replaceFunctionalityIdPlaceholders(functionalityIds,
                        IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));
                final String fileName = resource.getFilename();
                if (fileName == null) {
                    throw new NotGonnaHappenException(
                            "A resource can have no file name, but we specifically listed JSON files!");
                }
                ZipEntry entry = new ZipEntry(fileName);
                zip.putNextEntry(entry);
                zip.write(content.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }

        }
        return file;
    }

    /**
     * Read an existing UTF8 text resource in the classpath.<br>
     * The resource is supposed to always exist (during development, NotGonnaHappenException is thrown is that contract
     * is not met anymore).
     *
     * @param name the name of the resource to load
     * @return the resource text-content
     */
    String getResourceAsUtf8String(String name) {
        try (final InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            LOG.error("DEMO|Cannot read demo resource file in classpath: {}", name, e);
            throw new NotGonnaHappenException("Demo file should exist and be valid in classpath: " + name);
        }
    }

}
