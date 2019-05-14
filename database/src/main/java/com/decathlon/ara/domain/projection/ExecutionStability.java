package com.decathlon.ara.domain.projection;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStability {
    private Long executionId;
    private Date testDate;
    private String status;
}
