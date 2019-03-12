package com.decathlon.ara.service.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    private Long id;

    private String step;

    private String stepDefinition;

    private int stepLine;

    private String exception;

}
