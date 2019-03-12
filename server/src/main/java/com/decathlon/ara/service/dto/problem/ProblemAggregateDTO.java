package com.decathlon.ara.service.dto.problem;

import com.decathlon.ara.service.dto.stability.CycleStabilityDTO;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemAggregateDTO {

    private long patternCount;

    private long errorCount;

    private long scenarioCount;
    private String firstScenarioName;

    private long branchCount;
    private String firstBranch;

    private long releaseCount;
    private String firstRelease;

    private long versionCount;
    private String firstVersion;

    private long countryCount;
    private CountryDTO firstCountry;

    private long typeCount;
    private TypeWithSourceDTO firstType;

    private long platformCount;
    private String firstPlatform;

    private List<CycleStabilityDTO> cycleStabilities = new ArrayList<>();

}
