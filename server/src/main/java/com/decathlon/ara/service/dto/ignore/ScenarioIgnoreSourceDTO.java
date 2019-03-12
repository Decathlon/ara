package com.decathlon.ara.service.dto.ignore;

import com.decathlon.ara.service.dto.source.SourceDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioIgnoreSourceDTO {

    /**
     * All severities with at least one ignored scenario, and the ALL special-severity.
     */
    private final List<ScenarioIgnoreSeverityDTO> severities = new ArrayList<>();

    /**
     * Either the source-code location of the aggregation, or {@link SourceDTO#ALL} for the
     * grand totals per severities.
     */
    private SourceDTO source;

}
