package com.decathlon.ara.service.dto.ignore;

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
public class ScenarioIgnoreFeatureDTO {

    private final List<String> scenarios = new ArrayList<>();

    private String file;
    private String name;

}
