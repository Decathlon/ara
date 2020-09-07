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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.decathlon.ara.domain.enumeration.QualityStatus;

public class QualityThresholdTest {

    @Test
    public void toStatus_should_return_FAILED_when_under_failure_threshold() {
        // GIVEN
        QualityThreshold threshold = new QualityThreshold().withFailure(50).withWarning(75);

        // WHEN
        final QualityStatus status = threshold.toStatus(49);

        // THEN
        assertThat(status).isEqualTo(QualityStatus.FAILED);
    }

    @Test
    public void toStatus_should_return_WARNING_when_over_failure_threshold_and_under_warning_threshold() {
        // GIVEN
        QualityThreshold threshold = new QualityThreshold().withFailure(50).withWarning(75);

        // WHEN
        final QualityStatus status = threshold.toStatus(50);

        // THEN
        assertThat(status).isEqualTo(QualityStatus.WARNING);
    }

    @Test
    public void toStatus_should_return_PASSED_when_over_warning_threshold() {
        // GIVEN
        QualityThreshold threshold = new QualityThreshold().withFailure(50).withWarning(75);

        // WHEN
        final QualityStatus status = threshold.toStatus(75);

        // THEN
        assertThat(status).isEqualTo(QualityStatus.PASSED);
    }

}
