package com.decathlon.ara.service.dto.type;

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
public class TypeDTO {

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 16, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 50, message = "The name is required and must not exceed {max} characters.")
    private String name;

    private boolean isBrowser;

    private boolean isMobile;

}
