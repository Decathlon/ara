package com.decathlon.ara.service.dto.severity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import static com.decathlon.ara.service.support.DtoConstants.CODE_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SeverityDTO {

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 32, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    /**
     * The order in which the severities should appear: the lowest position should be for the highest severity.
     */
    @NotNull(message = "The position is required.")
    private Integer position;

    /**
     * The full name (eg. "Sanity Check").
     */
    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 32, message = "The name is required and must not exceed {max} characters.")
    private String name;

    /**
     * The shorter name (but still intelligible) to display on table column headers where space is constrained (eg. "Sanity Ch.").
     */
    @NotNull(message = "The short name is required.")
    @Size(min = 1, max = 16, message = "The short name is required and must not exceed {max} characters.")
    private String shortName;

    /**
     * The shortest name to display on email subjects to help keep it very short (eg. "S.C.").
     */
    @NotNull(message = "The initials are required.")
    @Size(min = 1, max = 8, message = "The initials are required and must not exceed {max} characters.")
    private String initials;

    /**
     * True to use that severity as a default one when a scenario does not declare its severity or has a nonexistent
     * one. Only one severity can be declared as the default.
     */
    private boolean defaultOnMissing;

}
