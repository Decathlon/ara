package com.decathlon.ara.service.dto.quality;

import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class QualitySeverityDTO {

    private SeverityDTO severity;

    private ScenarioCountDTO scenarioCounts;

    /**
     * Deduced from {@code scenarioCounts}.
     */
    private int percent;

    /**
     * Deduced from {@code percent} and the quality thresholds of this {@code severity} at the time of execution.
     */
    private QualityStatus status;

}
