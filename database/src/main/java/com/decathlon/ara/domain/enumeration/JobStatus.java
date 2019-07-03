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

package com.decathlon.ara.domain.enumeration;

public enum JobStatus {

    /**
     * Just created in database, but the job has not been crawled yet, or it has not been run yet, so crawling failed for now.
     */
    PENDING,

    /**
     * Created in database, crawled and found the job is still running.
     */
    RUNNING,

    /**
     * Created in database, crawled and found the job is terminated (either as a success, failure or any other results) and will not be updated anymore.
     */
    DONE,

    /**
     * Created in database, crawled but the job has not been found, and the parent job terminated, so this one will never have a chance to run anymore: something went wrong before.
     */
    UNAVAILABLE

}
