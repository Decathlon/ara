package com.decathlon.ara.service.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionCriteriaDTO {

    String country;
    String exception;
    String feature;
    String handling;
    Long problem;
    String scenario;
    boolean scenarioDetails;
    String severity;
    String step;
    Long team;
    String type;
    boolean withSucceed;
}
