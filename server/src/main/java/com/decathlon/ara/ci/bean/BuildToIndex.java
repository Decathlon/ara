package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.CycleDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class BuildToIndex {

    private CycleDefinition cycleDefinition;

    private Build build;

}
