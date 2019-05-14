package com.decathlon.ara.service.dto.execution;

import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ExecutionDTO {

    private Long id;

    private String branch;

    private String name;

    private String release;

    private String version;

    private Date buildDateTime;

    private Date testDateTime;

    private String jobUrl;

    private JobStatus status;

    private Result result;

    private ExecutionAcceptance acceptance;

    private String discardReason;

    private boolean blockingValidation;

    private Map<String, QualityThreshold> qualityThresholds;

    private QualityStatus qualityStatus;

    private List<QualitySeverityDTO> qualitySeverities;

    private Long duration;

    private Long estimatedDuration;

}
