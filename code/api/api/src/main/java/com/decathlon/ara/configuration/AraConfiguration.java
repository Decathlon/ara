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

package com.decathlon.ara.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>ARA's application configuration options to be used by the Java code.</p>
 * <p>
 * Note: every property needs to be documented with javadoc (it is a public interface to be used by customers):
 * Spring will automatically generate target/classes/META-INF/spring-configuration-metadata.json
 * for IDEs to auto-complete and document the application*.properties configuration files
 * </p>
 * <p>
 * Important: if you define a new configuration key but only use it in @Value("${ara.foo-bar}")
 * then you need to document the new keys in src/main/resources/META-INF/additional-spring-configuration-metadata.json
 * Spring will embed it in the generated spring-configuration-metadata.json for IDE auto-completion.
 * </p>
 */
@Configuration
@ConfigurationProperties("ara")
@Import(DataSourceConfiguration.class)
public class AraConfiguration {

    /**
     * For SshAssetService: the host to which to connect to send asserts (screenshots, HTTP logs).
     */
    private String sshHost;

    /**
     * For SshAssetService: the port to which to connect to send asserts (screenshots, HTTP logs).
     */
    private int sshPort;

    /**
     * For SshAssetService: the user with which to connect to send asserts (screenshots, HTTP logs).
     */
    private String sshUser;

    /**
     * For SshAssetService: the user's password with which to connect to send asserts (screenshots, HTTP logs).
     */
    private String sshPassword;

    /**
     * For SshAssetService: the absolute folder path in which all asserts will be sent (screenshots, HTTP logs).
     */
    private String sshRemoteHomeFolder;

    /**
     * For SshAssetService: the absolute URL where assets will be found after upload (screenshots, HTTP logs).
     */
    private String sshHttpAccess;

    /**
     * For SshAssetService: the folder path in which to upload screenshots
     *
     * @see #sshRemoteHomeFolder the parent folder of this sub-folder
     * @see #sshHttpAccess the parent URL where this sub-folder can be accessed from the outside
     */
    private String sshScreenshotSubFolder;

    /**
     * For SshAssetService: the folder path in which to upload HTTP logs
     *
     * @see #sshRemoteHomeFolder the parent folder of this sub-folder
     * @see #sshHttpAccess the parent URL where this sub-folder can be accessed from the outside
     */
    private String sshHttpLogsSubFolder;

    /**
     * For FileAssetService: the absolute folder path in which all asserts will be written (screenshots, HTTP logs).
     */
    private String fileHomeFolder;

    /**
     * For FileAssetService: the absolute URL where assets will be found after having been written to disk (screenshots, HTTP logs).
     */
    private String fileHttpAccess;

    /**
     * For FileAssetService: the folder path in which to write screenshots
     *
     * @see #fileHomeFolder the parent folder of this sub-folder
     * @see #fileHttpAccess the parent URL where this sub-folder can be accessed from the outside
     */
    private String fileScreenshotSubFolder;

    /**
     * For FileAssetService: the folder path in which to write HTTP logs
     *
     * @see #fileHomeFolder the parent folder of this sub-folder
     * @see #fileHttpAccess the parent URL where this sub-folder can be accessed from the outside
     */
    private String fileHttpLogsSubFolder;

    /**
     * The base URL of the Web client graphical interface (mainly for sent mails to point to the correct URLs).
     */
    private String clientBaseUrl;

    /**
     * Maximum number of days to keep executions.
     * Can be exceeded if and only if it would result in having less that minExecutionsToKeepPerCycle executions left.
     * -1 to disable such limit.
     */
    private Integer maxExecutionDaysToKeep;

    /**
     * Minimum number of jobs to keep per cycleDefinition.
     * This has precedence over maxExecutionDaysToKeep to keep at least a certain number of jobs even if not so many were
     * executed recently.
     * -1 to disable such limit.
     */
    private Integer minExecutionsToKeepPerCycle;

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
    }

    public String getSshRemoteHomeFolder() {
        return sshRemoteHomeFolder;
    }

    public void setSshRemoteHomeFolder(String sshRemoteHomeFolder) {
        this.sshRemoteHomeFolder = sshRemoteHomeFolder;
    }

    public String getSshHttpAccess() {
        return sshHttpAccess;
    }

    public void setSshHttpAccess(String sshHttpAccess) {
        this.sshHttpAccess = sshHttpAccess;
    }

    public String getSshScreenshotSubFolder() {
        return sshScreenshotSubFolder;
    }

    public void setSshScreenshotSubFolder(String sshScreenshotSubFolder) {
        this.sshScreenshotSubFolder = sshScreenshotSubFolder;
    }

    public String getSshHttpLogsSubFolder() {
        return sshHttpLogsSubFolder;
    }

    public void setSshHttpLogsSubFolder(String sshHttpLogsSubFolder) {
        this.sshHttpLogsSubFolder = sshHttpLogsSubFolder;
    }

    public String getFileHomeFolder() {
        return fileHomeFolder;
    }

    public void setFileHomeFolder(String fileHomeFolder) {
        this.fileHomeFolder = fileHomeFolder;
    }

    public String getFileHttpAccess() {
        return fileHttpAccess;
    }

    public void setFileHttpAccess(String fileHttpAccess) {
        this.fileHttpAccess = fileHttpAccess;
    }

    public String getFileScreenshotSubFolder() {
        return fileScreenshotSubFolder;
    }

    public void setFileScreenshotSubFolder(String fileScreenshotSubFolder) {
        this.fileScreenshotSubFolder = fileScreenshotSubFolder;
    }

    public String getFileHttpLogsSubFolder() {
        return fileHttpLogsSubFolder;
    }

    public void setFileHttpLogsSubFolder(String fileHttpLogsSubFolder) {
        this.fileHttpLogsSubFolder = fileHttpLogsSubFolder;
    }

    public String getClientBaseUrl() {
        return clientBaseUrl;
    }

    public void setClientBaseUrl(String clientBaseUrl) {
        this.clientBaseUrl = clientBaseUrl;
    }

    public Integer getMaxExecutionDaysToKeep() {
        return maxExecutionDaysToKeep;
    }

    public void setMaxExecutionDaysToKeep(Integer maxExecutionDaysToKeep) {
        this.maxExecutionDaysToKeep = maxExecutionDaysToKeep;
    }

    public Integer getMinExecutionsToKeepPerCycle() {
        return minExecutionsToKeepPerCycle;
    }

    public void setMinExecutionsToKeepPerCycle(Integer minExecutionsToKeepPerCycle) {
        this.minExecutionsToKeepPerCycle = minExecutionsToKeepPerCycle;
    }

}
