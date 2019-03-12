package com.decathlon.ara.report.asset.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds an SSH and SFTP connection and provide simple accesses.
 */
@Slf4j
public class SshClientHelper implements Closeable {

    private static final String BECAUSE = " because: ";

    private final Session session;

    private ChannelSftp sftpChannel;

    /**
     * Connect to the given machine by SSH.
     *
     * @param host     machine's hostname
     * @param port     machine's port (usually 22)
     * @param user     username
     * @param password user's password
     * @throws SshException on connection failure
     */
    public SshClientHelper(final String host, final int port, final String user, final String password) throws SshException {
        try {
            // TODO JSch.setHostKeyRepository() instead of StrictHostKeyChecking=no?
            // LIKE http://wiki.jsch.org/index.php?Manual/Examples/SftpFileCopyExample
            session = new JSch().getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            throw new SshException("Cannot connect to " + user + "@" + host + ":" + port + BECAUSE + e.getMessage(), e);
        }
    }

    /**
     * Create folders recursively.
     *
     * @param remotePath the full path to create (all intermediary folders will be created too if they do not exist yet)
     * @throws SshException when something goes wrong while interacting with the server
     */
    public void mkdirRecursively(final String remotePath) throws SshException {
        final Deque<File> files = new ArrayDeque<>();
        File file = new File(remotePath);
        files.push(file);
        while (file.getParentFile() != null) {
            files.push(file.getParentFile());
            file = file.getParentFile();
        }
        while (!files.isEmpty()) {
            String poppedFile = files.pop().toString().replace('\\', '/');
            SftpATTRS stat = null;
            try {
                stat = getSftpChannel().stat(poppedFile);
            } catch (final SftpException e) {
                // Folder does not exist.
                // We expect this error and that's the simplest and fastest way to detect it.
                log.debug("Path {} does not exist: making a directory for it", poppedFile, e);
            }

            if (stat == null) {
                try {
                    getSftpChannel().mkdir(poppedFile);
                } catch (SftpException e) {
                    throw new SshException("Cannot make directory " + poppedFile + BECAUSE + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Upload a text file as UTF-8.
     *
     * @param remoteFile the remote file (to be created or updated) full path (absolute folder + file name) where to put
     *                   the file content
     * @param content    a text file content (will be saved as UTF-8)
     * @throws SshException when something goes wrong while interacting with the server
     */
    public void echo(final String remoteFile, final String content) throws SshException {
        put(remoteFile, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Upload a binary file.
     *
     * @param remoteFile the remote file (to be created or updated) full path (absolute folder + file name) where to put
     *                   the file content
     * @param bytes      a binary file content
     * @throws SshException when something goes wrong while interacting with the server
     */
    public void put(final String remoteFile, final byte[] bytes) throws SshException {
        try {
            getSftpChannel().put(new ByteArrayInputStream(bytes), remoteFile, ChannelSftp.OVERWRITE);
        } catch (SftpException e) {
            throw new SshException("Cannot upload to " + remoteFile + BECAUSE + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (sftpChannel != null) {
            sftpChannel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * @return an SFTP channel, to use all SFTP available methods (a new one the first time it is called, the same one
     * on next calls)
     * @throws SshException when something goes wrong while creating the SFTP channel (on the first call)
     */
    private ChannelSftp getSftpChannel() throws SshException {
        if (sftpChannel == null) {
            try {
                final Channel channel = session.openChannel("sftp");
                channel.connect();
                sftpChannel = (ChannelSftp) channel;
            } catch (JSchException e) {
                throw new SshException("Cannot open SFTP channel" + BECAUSE + e.getMessage(), e);
            }
        }
        return sftpChannel;
    }

}
