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

import com.decathlon.ara.configuration.AraConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileAssetServiceTest {

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private FileNameService fileNameService;

    @InjectMocks
    private FileAssetService cut;

    @Test
    public void saveScreenshot_should_save_file_and_return_correct_url() throws IOException {
        Path tempDirectory = null;
        try {
            // GIVEN
            tempDirectory = Files.createTempDirectory("ara_temp_unit_test_directory_");
            when(araConfiguration.getFileHomeFolder()).thenReturn(tempDirectory.toString());
            when(araConfiguration.getFileScreenshotSubFolder()).thenReturn("/directory");
            when(araConfiguration.getFileHttpAccess()).thenReturn("http://access");
            when(fileNameService.generateReportFileName("Scenario Name", "png")).thenReturn("file");
            byte[] screenshot = new byte[] { 0, 1, 2 };

            // WHEN
            final String url = cut.saveScreenshot(screenshot, "Scenario Name");

            // THEN
            assertThat(url).isEqualTo("http://access/directory/file");
            final String filePath = tempDirectory + "/directory/file";
            assertThat(new File(filePath).length()).isEqualTo(3);
        } finally {
            if (tempDirectory != null) {
                FileUtils.deleteQuietly(tempDirectory.toFile());
            }
        }
    }

    @Test
    public void saveScreenshot_should_not_fail_but_return_null_on_write_failure() {
        // GIVEN
        when(araConfiguration.getFileHomeFolder()).thenReturn("/bin/mkdir/?/not-writable"); // ... on Unix nor on Windows

        // WHEN
        final String url = cut.saveScreenshot(new byte[] { 'a', 'n', 'y' }, "any");

        // THEN
        assertThat(url).isNull();
    }

    @Test
    public void saveHttpLogs_should_save_file_and_return_correct_url() throws IOException {
        Path tempDirectory = null;
        try {
            // GIVEN
            tempDirectory = Files.createTempDirectory("ara_temp_unit_test_directory_");
            when(araConfiguration.getFileHomeFolder()).thenReturn(tempDirectory.toString());
            when(araConfiguration.getFileHttpLogsSubFolder()).thenReturn("/directory");
            when(araConfiguration.getFileHttpAccess()).thenReturn("http://access");
            when(fileNameService.generateReportFileName("http-log", "html")).thenReturn("file");
            String html = "html";

            // WHEN
            final String url = cut.saveHttpLogs(html);

            // THEN
            assertThat(url).isEqualTo("http://access/directory/file");
            final String filePath = tempDirectory.toString() + "/directory/file";
            assertThat(new File(filePath).length()).isEqualTo(4);
        } finally {
            if (tempDirectory != null) {
                FileUtils.deleteQuietly(tempDirectory.toFile());
            }
        }
    }

    @Test
    public void saveHttpLogs_should_not_fail_but_return_null_on_write_failure() {
        // GIVEN
        when(araConfiguration.getFileHomeFolder()).thenReturn("/bin/mkdir/?/not-writable"); // ... on Unix nor on Windows
        when(araConfiguration.getFileHttpLogsSubFolder()).thenReturn("/directory");

        // WHEN
        final String url = cut.saveHttpLogs("any");

        // THEN
        assertThat(url).isNull();
    }

}
