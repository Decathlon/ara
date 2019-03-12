package com.decathlon.ara.service.dto.communication;

import com.decathlon.ara.domain.enumeration.CommunicationType;
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
public class CommunicationDTO {

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 32, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 64, message = "The name is required and must not exceed {max} characters.")
    private String name;

    private CommunicationType type;

    private String message;

}
