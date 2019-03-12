package com.decathlon.ara.service.dto.request;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class NewFunctionalityDTO {

    private FunctionalityDTO functionality;

    private Long referenceId;

    private FunctionalityPosition relativePosition;

}
