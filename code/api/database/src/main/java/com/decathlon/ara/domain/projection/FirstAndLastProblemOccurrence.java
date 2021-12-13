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

package com.decathlon.ara.domain.projection;

import java.util.Date;

public class FirstAndLastProblemOccurrence {

    private Long problemId;
    private Date firstSeenDateTime;
    private Date lastSeenDateTime;

    public FirstAndLastProblemOccurrence() {
    }

    public FirstAndLastProblemOccurrence(Long problemId, Date firstSeenDateTime, Date lastSeenDateTime) {
        this.problemId = problemId;
        this.firstSeenDateTime = firstSeenDateTime;
        this.lastSeenDateTime = lastSeenDateTime;
    }

    public Long getProblemId() {
        return problemId;
    }

    public Date getFirstSeenDateTime() {
        return firstSeenDateTime;
    }

    public Date getLastSeenDateTime() {
        return lastSeenDateTime;
    }

}
