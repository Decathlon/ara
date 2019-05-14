package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class Build {

    /**
     * The URL of the Continuous Integration job, visible in the client GUI to access logs of the job.
     */
    private String url;

    /**
     * An alternate URL for the job, only for internal indexing needs (optional: either the local directory from which
     * to index or an intermediary service used to eg. compute the Continuous Integration job's hierarchy).
     */
    private String link;

    /**
     * The short display name. Eg. "b1804.1803011242" for an execution build.
     */
    private String displayName;

    /**
     * Continuous Integration job result. My be null if still running.
     *
     * @see Result Result values
     */
    private Result result;

    /**
     * True if currently running, false if terminated.
     */
    private boolean building;

    /**
     * Number of milliseconds elapsed since started (up to now if running, to the end if terminated).
     */
    private long duration;

    /**
     * Number of UNIX milliseconds representing the date & time at which the job started (since Jan 01 1970 UTC/GMT+0).
     */
    private long timestamp;

    /**
     * Theoretical number of milliseconds this job should last (estimated by the Continuous Integration server from
     * previous execution durations).
     */
    private long estimatedDuration;

    /**
     * Only for a build of an execution (not for a run or a countryDeployment): the release of the version having been
     * tested by this execution build (eg. "v2" or "1904" for the release encompassing all versions of April 2019).
     */
    private String release;

    /**
     * Only for a build of an execution (not for a run or a countryDeployment): the version having been tested by this
     * execution build (eg. "1904.3" for the third version of the April 2019 release, a timestamped version, or the Git
     * commit ID).
     */
    private String version;

    /**
     * Only for a build of an execution (not for a run or a countryDeployment): number of UNIX milliseconds representing
     * the date & time at which the version having been tested by this execution build was created (compiled...).
     */
    private Long versionTimestamp;

    /**
     * A comment or title or description to display just above the associated {@link Run} in the GUI.
     */
    private String comment;

}
