package com.decathlon.ara.service.dto.coverage;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverageDTO {

    private List<AxisDTO> axes;
    private int[] values;

}
