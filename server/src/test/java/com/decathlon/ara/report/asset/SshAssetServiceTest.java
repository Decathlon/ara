package com.decathlon.ara.report.asset;

import com.decathlon.ara.report.asset.ssh.SshClientHelper;
import com.decathlon.ara.report.asset.ssh.SshException;
import com.decathlon.ara.configuration.AraConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SshAssetServiceTest {

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private FileNameService fileNameService;

    @Mock
    private SshClientHelper sshClientHelper;

    @Spy
    @InjectMocks
    private SshAssetService cut;

    @Test
    public void saveScreenshot_should_upload_file_and_return_correct_url() throws SshException {
        // GIVEN
        doReturn(sshClientHelper).when(cut).connect();
        when(araConfiguration.getSshRemoteHomeFolder()).thenReturn("/home");
        when(araConfiguration.getSshScreenshotSubFolder()).thenReturn("/directory");
        when(araConfiguration.getSshHttpAccess()).thenReturn("http://access");
        when(fileNameService.generateReportFileName("Scenario Name", "png")).thenReturn("file");
        byte[] screenshot = new byte[] { };

        // WHEN
        final String url = cut.saveScreenshot(screenshot, "Scenario Name");

        // THEN
        verify(sshClientHelper, times(1)).put(eq("/home/directory/file"), same(screenshot));
        assertThat(url).isEqualTo("http://access/directory/file");
    }

    @Test
    public void saveScreenshot_should_create_directories_only_once() throws SshException {
        // GIVEN
        doReturn(sshClientHelper).when(cut).connect();
        when(araConfiguration.getSshRemoteHomeFolder()).thenReturn("/home");
        when(araConfiguration.getSshScreenshotSubFolder()).thenReturn("/directory");
        byte[] screenshot = new byte[] {};

        // WHEN
        cut.saveScreenshot(screenshot, "Scenario Name");
        cut.saveScreenshot(screenshot, "Scenario Name");

        // THEN
        verify(sshClientHelper, times(1)).mkdirRecursively("/home/directory");
    }

    @Test
    public void saveScreenshot_should_not_fail_but_return_null_on_upload_failure() throws SshException {
        // GIVEN
        doThrow(SshException.class).when(cut).connect();
        byte[] screenshot = new byte[] { 'a', 'n', 'y' };

        // WHEN
        final String url = cut.saveScreenshot(screenshot, "any");

        // THEN
        assertThat(url).isNull();
    }

    @Test
    public void saveHttpLogs_should_upload_file_and_return_correct_url() throws SshException {
        // GIVEN
        doReturn(sshClientHelper).when(cut).connect();
        when(araConfiguration.getSshRemoteHomeFolder()).thenReturn("/home");
        when(araConfiguration.getSshHttpLogsSubFolder()).thenReturn("/directory");
        when(araConfiguration.getSshHttpAccess()).thenReturn("http://access");
        when(fileNameService.generateReportFileName("http-log", "html")).thenReturn("file");
        String html = "html";

        // WHEN
        final String url = cut.saveHttpLogs(html);

        // THEN
        verify(sshClientHelper, times(1)).echo("/home/directory/file", html);
        assertThat(url).isEqualTo("http://access/directory/file");
    }

    @Test
    public void saveHttpLogs_should_create_directories_only_once() throws SshException {
        // GIVEN
        doReturn(sshClientHelper).when(cut).connect();
        when(araConfiguration.getSshRemoteHomeFolder()).thenReturn("/home");
        when(araConfiguration.getSshHttpLogsSubFolder()).thenReturn("/directory");
        String html = "any";

        // WHEN
        cut.saveHttpLogs(html);
        cut.saveHttpLogs(html);

        // THEN
        verify(sshClientHelper, times(1)).mkdirRecursively("/home/directory");
    }

    @Test
    public void saveHttpLogs_should_not_fail_but_return_null_on_upload_failure() throws SshException {
        // GIVEN
        doThrow(SshException.class).when(cut).connect();
        String html = "any";

        // WHEN
        final String url = cut.saveHttpLogs(html);

        // THEN
        assertThat(url).isNull();
    }

}
