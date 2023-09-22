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

package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.Result;

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
     * Number of UNIX milliseconds representing the date and time at which the job started (since Jan 01 1970 UTC/GMT+0).
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
     * the date and time at which the version having been tested by this execution build was created (compiled...).
     */
    private Long versionTimestamp;

    /**
     * A comment or title or description to display just above the associated {@link Run} in the GUI.
     */
    private String comment;

    public Build() {
    }

    public Build(String url, Result result, long timestamp) {
        this.url = url;
        this.result = result;
        this.timestamp = timestamp;
    }

    public Build(String url, Result result, long timestamp, String release, String version, Long versionTimestamp) {
        this.url = url;
        this.result = result;
        this.timestamp = timestamp;
        this.release = release;
        this.version = version;
        this.versionTimestamp = versionTimestamp;
    }

    public String getUrl() {
        return url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Result getResult() {
        return result;
    }

    public boolean isBuilding() {
        return building;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    public String getRelease() {
        return release;
    }

    public String getVersion() {
        return version;
    }

    public Long getVersionTimestamp() {
        return versionTimestamp;
    }

    public String getComment() {
        return comment;
    }

}
