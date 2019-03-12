package com.decathlon.ara.loader;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.service.ScenarioService;
import com.decathlon.ara.service.exception.BadRequestException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.decathlon.ara.loader.DemoLoaderConstants.SOURCE_CODE_API;
import static com.decathlon.ara.loader.DemoLoaderConstants.SOURCE_CODE_WEB;

/**
 * Load scenarios into the Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoScenarioLoader {

    @NonNull
    private final ScenarioService scenarioService;

    @NonNull
    private final DemoLoaderService demoLoaderService;

    public void createScenarios(long projectId, Map<String, Long> functionalityIds) throws BadRequestException {
        scenarioService.uploadCucumber(projectId, SOURCE_CODE_WEB,
                demoLoaderService.replaceFunctionalityIdPlaceholders(functionalityIds,
                        getResourceAsUtf8String("reports/demo/dry-report.json")));

        final File postmanScenariosZip;
        try {
            postmanScenariosZip = getPostmanScenariosZip(functionalityIds);
        } catch (IOException e) {
            log.error("Cannot generate the ZIP of Postman scenarios: doing without functionality coverage for them", e);
            return;
        }

        try {
            scenarioService.uploadPostman(projectId, SOURCE_CODE_API, postmanScenariosZip);
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
            log.error("Cannot read demo resource file in classpath: {}", name, e);
            throw new NotGonnaHappenException("Demo file should exist and be valid in classpath: " + name);
        }
    }

}
