package com.decathlon.ara.service.dto.ignore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ScenarioIgnoreCountDTO {

    private long ignored;
    private long total;

    public int getPercent() {
        return (total == 0 ? 0 : (int) Math.ceil(100 * ignored / (double) total));
    }

}
