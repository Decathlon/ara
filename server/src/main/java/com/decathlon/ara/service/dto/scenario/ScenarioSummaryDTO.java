package com.decathlon.ara.service.dto.scenario;

import com.decathlon.ara.service.dto.source.SourceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioSummaryDTO {

    private Long id;
    private SourceDTO source;
    private String featureFile;
    private String featureName;
    private String name;

    private int functionalityCount;
    private boolean hasCountryCodes;
    private boolean hasSeverity;

    private String wrongFunctionalityIds;
    private String wrongCountryCodes;
    private String wrongSeverityCode;

}
