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

package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.util.FetchException;
import java.util.List;

/**
 * This interface expose a more specific method dedicated to scheduled and automatic fetchs.
 *
 * @version 1.0
 */
public interface PullFetcher extends Fetcher {

    /**
     * Given branch and cycle names, return an history of the latest job executions.
     *
     * @param projectId  the ID of the project in which to work
     * @param branchName the branch name (eg. "master", "develop"...)
     * @param cycleName  the cycle name (eg. "day", "night"...)
     * @return an history of the latest job executions (latest first)
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    List<Build> getJobHistory(long projectId, String branchName, String cycleName) throws FetchException;
}
