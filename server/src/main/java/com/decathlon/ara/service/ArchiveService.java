package com.decathlon.ara.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * This service provide operation to easily manipulate Archives files (for now only ZIP files).
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArchiveService {

    /**
     * Unzip the given file to the given destination.
     *
     *
     * Note that if the given file is empty, then this method will log in Warning level this fact and do nothing.
     *
     * @param file        the Multipart file to unzip.
     * @param destination the destination where to put the resulting tree hierarchy in the zip.
     * @throws IOException If the file is empty, can't be read or if the process doesn't have the rights to write
     *                     at the given destination.
     */
    public void unzip(MultipartFile file, File destination) throws IOException {
        if (file.isEmpty()) {
            log.warn("The given ZIP file is empty !");
        }
        this.unzip(file.getInputStream(), destination);
    }

    /**
     * Unzip the given file to the given destination.
     *
     * @param inputStream the InputStream to the zip file to unzip.
     * @param destination the destination where to put the resulting tree hierarchy in the zip.
     * @throws IOException If the file is empty, can't be read or if the process doesn't have the rights to write
     *                     at the given destination.
     */
    public void unzip(InputStream inputStream, File destination) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry = zis.getNextEntry();
            while (null != entry) {
                File target = new File(destination, entry.getName());
                log.debug("Unzipping : {}", target.getAbsolutePath());
                if (!entry.isDirectory()) {
                    this.writeEntry(zis, target);
                }
                entry = zis.getNextEntry();
            }
        }
    }

    private void writeEntry(ZipInputStream zis, File target) throws IOException {
        Files.createDirectories(target.getParentFile().toPath());
        byte[] buffer = new byte[4096];
        try (FileOutputStream fos = new FileOutputStream(target)) {
            int len = zis.read(buffer);
            while (len > 0) {
                fos.write(buffer, 0, len);
                len = zis.read(buffer);
            }
        }
    }
}
