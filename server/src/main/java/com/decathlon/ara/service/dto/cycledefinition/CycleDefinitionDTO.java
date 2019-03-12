package com.decathlon.ara.service.dto.cycledefinition;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import static com.decathlon.ara.service.support.DtoConstants.CODE_NAME_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class CycleDefinitionDTO {

    private Long id;

    @NotNull(message = "The branch name is required.")
    @Size(min = 1, max = 16, message = "The branch is required and must not exceed {max} characters.")
    private String branch;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 16, message = "The name is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_NAME_MESSAGE)
    private String name;

    @NotNull(message = "The branch position is required.")
    private Integer branchPosition;

}
