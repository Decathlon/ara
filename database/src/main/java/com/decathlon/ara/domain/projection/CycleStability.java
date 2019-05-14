package com.decathlon.ara.domain.projection;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleStability {
    private String cycleName;
    private String branchName;
    private List<ExecutionStability> executionStabilities = new ArrayList<>();
}
