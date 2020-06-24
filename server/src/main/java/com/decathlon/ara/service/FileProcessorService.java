/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FileProcessorService {

    @NonNull
    private final ObjectMapper objectMapper;

    /**
     * Create a mapped object from a file (if found and processed correctly)
     * @param parentDirectory the directory containing the file to get the mapped object from
     * @param pathToFile the relative path to this file from the parent directory
     * @param objectClass the class of the object to map
     * @param <T> the type of the object to map
     * @return the mapped object
     */
    public <T> Optional<T> getMappedObjectFromFile(File parentDirectory, String pathToFile, Class<T> objectClass) {
        Optional<File> matchingFile = getMatchingSimpleFile(parentDirectory, pathToFile);
        if (!matchingFile.isPresent()) {
            return Optional.empty();
        }

        T mappedObject = null;
        try {
            mappedObject = objectMapper.readValue(matchingFile.get(), objectClass);
        } catch (IOException e) {
            log.info("Unable to process the file {}", matchingFile.get().getAbsolutePath(), e);
        }

        return Optional.ofNullable(mappedObject);
    }

    /**
     * Get a file from a parent directory and a path starting from the parent directory
     * @param parentDirectory the folder in which the file is searched
     * @param filePath the path from the parentDirectory
     * @return the file if found
     */
    public Optional<File> getMatchingSimpleFile(File parentDirectory, String filePath) {
        Optional<File> file = getMatchingFile(parentDirectory, filePath);
        if (file.isPresent() && !file.get().isFile()) {
            return Optional.empty();
        }
        return file;
    }

    /**
     * Get a directory from a parent directory and a path starting from the parent directory
     * @param parentDirectory the folder in which the file is searched
     * @param directoryPath the path from the parentDirectory
     * @return the directory if found
     */
    public Optional<File> getMatchingDirectory(File parentDirectory, String directoryPath) {
        Optional<File> file = getMatchingFile(parentDirectory, directoryPath);
        if (file.isPresent() && !file.get().isDirectory()) {
            return Optional.empty();
        }
        return file;
    }

    /**
     * Get a file (either a single file or a directory) from a parent directory and a path starting from the parent directory
     * @param parentDirectory the folder in which the file is searched
     * @param filePath the path from the parentDirectory
     * @return the file if found
     */
    private Optional<File> getMatchingFile(File parentDirectory, String filePath) {
        if (parentDirectory == null || StringUtils.isBlank(filePath)) {
            return Optional.empty();
        }
        final String finalFilePath = File.separator.equals(String.valueOf(filePath.charAt(0))) ? filePath.substring(1) : filePath;
        final String absoluteFilePath = parentDirectory.getAbsolutePath() + File.separator + finalFilePath;

        File matchingFile = new File(absoluteFilePath);

        if (!matchingFile.exists()) {
            log.error("File {} not found", absoluteFilePath);
            return Optional.empty();
        }

        return Optional.ofNullable(matchingFile);
    }
}
