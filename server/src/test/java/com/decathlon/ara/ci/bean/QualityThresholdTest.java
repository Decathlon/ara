package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.enumeration.QualityStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
