package com.decathlon.ara.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
@Data
@Configuration
@ConfigurationProperties("ara")
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

}
