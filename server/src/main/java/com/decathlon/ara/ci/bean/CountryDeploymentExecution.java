package com.decathlon.ara.ci.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class CountryDeploymentExecution {

    /**
     * Country code. Eg. "be"...
     */
    private String country;

    /**
     * The build that ran (or is running) to deploy this platform.
     */
    private Build build;

}
