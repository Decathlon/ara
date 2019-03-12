package com.decathlon.ara.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveServiceTest {

    private static final String ZIP_TYPE = "application/zip";

    @InjectMocks
    private ArchiveService cut;

    @Test
    public void unzip_ShouldUnzipTheGivenZip() throws IOException, URISyntaxException {
        // GIVEN
        File targetDir = new File(System.getProperty("java.io.tmpdir"),
                "ara-unzip_ShouldUnzipTheGivenZip_" + String.valueOf(new Date().getTime()));
        File folderTargetDir = new File(targetDir, "folder");
        File subfolderTargetDir = new File(folderTargetDir, "sub-folder");
        String zipName = "files-in-folders.zip";
        URI zipURI = ClassLoader.getSystemResource("postman/" + zipName).toURI();
        byte[] content = Files.readAllBytes(Paths.get(zipURI));
        MultipartFile zipFile = new MockMultipartFile("zip", zipName, ZIP_TYPE, content);

        // WHEN
        try {
            this.cut.unzip(zipFile, targetDir);

            // THEN
            Assertions.assertThat(targetDir.list()).contains("root-file1.json", "root-file2.json");
            Assertions.assertThat(folderTargetDir.list()).contains("sub-file1.json", "sub-file2.json");
            Assertions.assertThat(subfolderTargetDir.list()).contains("sub-sub-file1.json", "sub-sub-file2.json");
        } finally  {
            FileUtils.deleteQuietly(targetDir);
        }
    }

    @Test
    public void unzip_ShouldNotSendIOException_IfZipFileEmpty() throws IOException {
        File targetDir = new File(System.getProperty("java.io.tmpdir"),
                "ara-unzip_ShouldSendIOException_IfZipFileEmpty-" + String.valueOf(new Date().getTime()));
        MultipartFile zipFile = new MockMultipartFile("zip", "not-exisiting.zip", ZIP_TYPE, new byte[0]);

        // WHEN
        try {
            this.cut.unzip(zipFile, targetDir);
        } finally {
            FileUtils.deleteQuietly(targetDir);
        }
    }

    @Test
    public void unzip_should_unzip_even_on_empty_file() throws IOException {
        File targetDir = new File(System.getProperty("java.io.tmpdir"),
                "ara-unzip-empty-file-" + new Date().getTime());
        targetDir.mkdir();
        MockMultipartFile zip = new MockMultipartFile("zip", "empty-zip.zip", ZIP_TYPE, new byte[0]);
        try {
            this.cut.unzip(zip, targetDir);
        } finally {
            FileUtils.deleteQuietly(targetDir);
        }
    }
}
