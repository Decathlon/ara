package com.decathlon.ara.service.dto.functionality;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class FunctionalityDTO {

    private Long id;

    private Long parentId;

    private double order;

    private String type;

    // NOTE: For this DTO, most validations are done in the Service, as it can represents a functionality OR folder
    // (folders have far fewer required fields)

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 512, message = "The name is required and must not exceed {max} characters.")
    private String name;

    @Size(max = 128)
    private String countryCodes;

    private Long teamId;

    private String severity;

    @Size(max = 10, message = "The created date or release must not exceed {max} characters.")
    private String created;

    private Boolean started;

    private Boolean notAutomatable;

    private Integer coveredScenarios;

    private String coveredCountryScenarios;

    private Integer ignoredScenarios;

    private String ignoredCountryScenarios;

    private String comment;

}
