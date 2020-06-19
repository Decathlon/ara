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
        Optional<File> matchingFile = getMatchingFile(parentDirectory, pathToFile);
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
     * Get a file from a directory and a path starting from the parent directory
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

        if (!(matchingFile.exists() && matchingFile.isFile())) {
            log.error("File {} not found", absoluteFilePath);
            return Optional.empty();
        }

        return Optional.ofNullable(matchingFile);
    }
}
