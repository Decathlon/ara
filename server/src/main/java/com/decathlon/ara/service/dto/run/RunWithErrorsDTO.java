package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.service.dto.error.ErrorDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RunWithErrorsDTO extends RunDTO {

    private List<ErrorDTO> errors;

}
