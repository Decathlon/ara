package com.decathlon.ara.service.dto.ignore;

import com.decathlon.ara.service.dto.severity.SeverityDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioIgnoreSeverityDTO {

    private final ScenarioIgnoreCountDTO counts = new ScenarioIgnoreCountDTO();

    private SeverityDTO severity;
    private List<ScenarioIgnoreFeatureDTO> features;

}
