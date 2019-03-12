package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import com.decathlon.ara.domain.enumeration.JobStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunDTO {

    private Long id;

    private CountryDTO country;

    private TypeWithSourceDTO type;

    private String comment;

    private String platform;

    private String jobUrl;

    private JobStatus status;

    private String countryTags;

    private String severityTags;

    private Boolean includeInThresholds;

    private Date startDateTime;

    private Long estimatedDuration;

    private Long duration;

}
