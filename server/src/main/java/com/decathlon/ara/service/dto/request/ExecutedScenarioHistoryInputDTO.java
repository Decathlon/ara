package com.decathlon.ara.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ExecutedScenarioHistoryInputDTO {

    private String cucumberId;

    private String cycleName;

    private String branch;

    private String countryCode;

    private String runTypeCode;

}
