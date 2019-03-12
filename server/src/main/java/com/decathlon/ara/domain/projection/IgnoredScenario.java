package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Wither
public class IgnoredScenario {

    private Source source;
    private String featureFile;
    private String featureName;
    private String severity;
    private String name;

}
