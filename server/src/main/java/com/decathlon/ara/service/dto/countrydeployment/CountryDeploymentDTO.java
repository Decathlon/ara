package com.decathlon.ara.service.dto.countrydeployment;

import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.service.dto.country.CountryDTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryDeploymentDTO {

    private Long id;

    private CountryDTO country;

    private String platform;

    private String jobUrl;

    private JobStatus status;

    private Result result;

    private Date startDateTime;

    private Long estimatedDuration;

    private Long duration;

}
