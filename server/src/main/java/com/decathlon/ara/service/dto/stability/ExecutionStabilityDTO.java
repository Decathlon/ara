package com.decathlon.ara.service.dto.stability;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStabilityDTO {

    private Long executionId;
    private Date testDate;
    private String status;

}
