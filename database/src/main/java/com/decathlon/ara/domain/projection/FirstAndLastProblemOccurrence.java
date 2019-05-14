package com.decathlon.ara.domain.projection;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class FirstAndLastProblemOccurrence {

    private Long problemId;
    private Date firstSeenDateTime;
    private Date lastSeenDateTime;

}
