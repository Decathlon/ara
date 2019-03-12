package com.decathlon.ara.report.asset;

import com.decathlon.ara.configuration.AraConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Write to disk (can be a NFS mount-point or a Docker mounted volume binding... this is transparent) parts of the data
 * from Cucumber and Postman reports.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(name = "ara.adapter.asset.name", havingValue = "file", matchIfMissing = true)
public class FileAssetService implements AssetService {

    @NonNull
    private final AraConfiguration araConfiguration;

    @NonNull
    private final FileNameService fileNameService;

    /**
     * Write a Cucumber scenario screenshot to disk.
     *
     * @param screenshot   the PNG bytes of the screenshot
     * @param scenarioName the name of the scenario for which the screenshot was taken (date is prepended, and png
     *                     extension is appended to generate file name)
     * @return the complete URL of the file having been saved, or null if write failed
     */
    @Override
    public String saveScreenshot(byte[] screenshot, String scenarioName) {
        try {
            // fileHomeFolder is something like /opt/assets
            // subFolder is something like /screenshots
            // The full path of the folder is then something like /opt/assets/screenshots
            final String subFolder = araConfiguration.getFileScreenshotSubFolder();
            final String absoluteFolderPath = araConfiguration.getFileHomeFolder() + subFolder;
            final String fileName = fileNameService.generateReportFileName(scenarioName, "png");
            final File file = new File(absoluteFolderPath + File.separator + fileName);

            // Will create directories if they do not exist
            FileUtils.writeByteArrayToFile(file, screenshot);

            return araConfiguration.getFileHttpAccess() + subFolder + "/" + fileName;
        } catch (IOException e) {
            log.error("Screenshot saving failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Write a Postman HTTP logs to disk.
     *
     * @param html the HTML representing the HTTP logs
     * @return the complete URL of the file having been saved, or null if write failed
     */
    @Override
    public String saveHttpLogs(String html) {
        try {
            // fileHomeFolder is something like /opt/assets
            // subFolder is something like /http-logs
            // The full path of the folder is then something like /opt/assets/http-logs
            final String subFolder = araConfiguration.getFileHttpLogsSubFolder();
            final String absoluteFolderPath = araConfiguration.getFileHomeFolder() + subFolder;
            final String fileName = fileNameService.generateReportFileName("http-log", "html");
            final File file = new File(absoluteFolderPath + File.separator + fileName);

            // Will create directories if they do not exist
            FileUtils.write(file, html, StandardCharsets.UTF_8);

            return araConfiguration.getFileHttpAccess() + subFolder + "/" + fileName;
        } catch (IOException e) {
            log.error("HTTP log saving failed: {}", e.getMessage(), e);
            return null;
        }
    }

}
