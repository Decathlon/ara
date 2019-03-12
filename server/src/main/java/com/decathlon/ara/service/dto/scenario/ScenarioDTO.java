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
public class ScenarioDTO {

    private Long id;

    private SourceDTO source;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private boolean ignored;

    private String countryCodes;

    private String severity;

    private String name;

    private String wrongFunctionalityIds;

    private String wrongCountryCodes;

    private String wrongSeverityCode;

    private int line;

    private String content;
}
