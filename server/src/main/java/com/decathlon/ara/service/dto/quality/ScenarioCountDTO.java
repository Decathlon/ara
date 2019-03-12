package com.decathlon.ara.service.dto.quality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioCountDTO {

    private int total;
    private int failed;
    private int passed;

}
