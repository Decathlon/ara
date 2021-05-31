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

package com.decathlon.ara.scenario.cucumber.asset;

import com.decathlon.ara.scenario.cucumber.asset.ssh.SshClientHelper;
import com.decathlon.ara.scenario.cucumber.asset.ssh.SshException;
import com.decathlon.ara.configuration.AraConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Upload to SSH parts of the data from Cucumber and Postman reports.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(name = "ara.adapter.asset.name", havingValue = "ssh")
public class SshAssetService implements AssetService {

    @NonNull
    private final AraConfiguration araConfiguration;

    @NonNull
    private final FileNameService fileNameService;

    // Not thread-safe booleans, but we're good: they protect a lazy-loaded idempotent process (directory creation)
    private boolean screenshotsFolderCreated;
    private boolean httpLogsFolderCreated;

    /**
     * Upload a Cucumber scenario screenshot to a SSH server.
     *
     * @param screenshot   the PNG bytes of the screenshot
     * @param scenarioName the name of the scenario for which the screenshot was taken (date is prepended, and png
     *                     extension is appended to generate file name)
     * @return the complete URL of the file having been saved, or null if upload failed
     */
    @Override
    public String saveScreenshot(byte[] screenshot, String scenarioName) {
        try (SshClientHelper sshClient = connect()) {
            // sshRemoteHomeFolder is something like /opt/assets
            // subFolder is something like /screenshots
            // The full path of the folder is then something like /opt/assets/screenshots
            final String subFolder = araConfiguration.getSshScreenshotSubFolder();
            String absoluteFolderPath = araConfiguration.getSshRemoteHomeFolder() + subFolder;

            if (!screenshotsFolderCreated) {
                sshClient.mkdirRecursively(absoluteFolderPath);
                screenshotsFolderCreated = true;
            }

            String fileName = fileNameService.generateReportFileName(scenarioName, "png");
            sshClient.put(absoluteFolderPath + "/" + fileName, screenshot);

            return araConfiguration.getSshHttpAccess() + subFolder + "/" + fileName;
        } catch (SshException e) {
            log.error("Screenshot upload failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Upload a Postman HTTP logs to a SSH server.
     *
     * @param html the HTML representing the HTTP logs
     * @return the complete URL of the file having been saved, or null if upload failed
     */
    @Override
    public String saveHttpLogs(String html) {
        try (SshClientHelper sshClient = connect()) {
            // sshRemoteHomeFolder is something like /opt/assets
            // subFolder is something like /http-logs
            // The full path of the folder is then something like /opt/assets/http-logs
            final String subFolder = araConfiguration.getSshHttpLogsSubFolder();
            String absoluteFolderPath = araConfiguration.getSshRemoteHomeFolder() + subFolder;

            if (!httpLogsFolderCreated) {
                sshClient.mkdirRecursively(absoluteFolderPath);
                httpLogsFolderCreated = true;
            }

            String fileName = fileNameService.generateReportFileName("http-log", "html");
            sshClient.echo(absoluteFolderPath + "/" + fileName, html);

            return araConfiguration.getSshHttpAccess() + subFolder + "/" + fileName;
        } catch (SshException e) {
            log.error("HTTP log upload failed: {}", e.getMessage(), e);
            return null;
        }
    }

    SshClientHelper connect() throws SshException {
        return new SshClientHelper(
                araConfiguration.getSshHost(),
                araConfiguration.getSshPort(),
                araConfiguration.getSshUser(),
                araConfiguration.getSshPassword());
    }

}
