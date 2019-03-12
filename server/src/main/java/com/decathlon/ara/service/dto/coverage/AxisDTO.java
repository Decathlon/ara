package com.decathlon.ara.service.dto.coverage;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class AxisDTO {

    private String code;
    private String name;
    private List<AxisPointDTO> points;

}
