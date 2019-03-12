package com.decathlon.ara.service.dto.response;

import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistinctStatisticsDTO {

    private List<String> releases;
    private List<CountryDTO> countries;
    private List<TypeWithSourceDTO> types;
    private List<String> platforms;
    private List<String> featureNames;
    private List<String> featureFiles;
    private List<String> scenarioNames;
    private List<String> steps;
    private List<String> stepDefinitions;

}
