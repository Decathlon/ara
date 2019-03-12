package com.decathlon.ara.ci.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class Artifact {

    /**
     * The path of the artifact, to append after "{jobUrl}/artifact/".<br>
     * Eg. "reports/json/report.json".
     */
    private String relativePath;

}
