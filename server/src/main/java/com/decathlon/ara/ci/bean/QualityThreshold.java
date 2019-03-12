package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.enumeration.QualityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
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

}
