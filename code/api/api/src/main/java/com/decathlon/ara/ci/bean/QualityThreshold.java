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

import com.decathlon.ara.domain.enumeration.QualityStatus;

public class QualityThreshold {

    /**
     * The percent below which a complete build is considered {@link QualityStatus#FAILED}.
     */
    private int failure;

    /**
     * The percent below which a complete build is considered {@link QualityStatus#WARNING},
     * if the percent is equal or greater than {@code failure}.
     * At this level or above, it is considered {@link QualityStatus#PASSED}
     */
    private int warning;

    public QualityThreshold() {

    }

    public QualityThreshold(int failure, int warning) {
        this.failure = failure;
        this.warning = warning;
    }

    /**
     * @param percent given a percentage and this threshold configuration, compute the status
     * @return the status, depending on the percent and this threshold configuration
     */
    public QualityStatus toStatus(int percent) {
        if (percent < failure) {
            return QualityStatus.FAILED;
        } else if (percent < warning) {
            return QualityStatus.WARNING;
        }
        return QualityStatus.PASSED;
    }

    public int getFailure() {
        return failure;
    }

    public int getWarning() {
        return warning;
    }

}
