package com.decathlon.ara.service.dto.execution;

import com.decathlon.ara.service.dto.countrydeployment.CountryDeploymentDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ExecutionHistoryPointDTO extends ExecutionDTO {

    private Long previousId;

    private Long nextId;

    private List<CountryDeploymentDTO> countryDeployments;

    private List<RunWithQualitiesDTO> runs;

}
