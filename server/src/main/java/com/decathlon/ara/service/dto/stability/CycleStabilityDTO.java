package com.decathlon.ara.service.dto.stability;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class CycleStabilityDTO {

    private String cycleName;
    private String branchName;
    private List<ExecutionStabilityDTO> executionStabilities;

}
