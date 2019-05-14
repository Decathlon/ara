package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * This object holds key+value data: it holds the count (the value) of scenarios for a given triple [ sourceCode, severityCode, ignoredOrNot ] "key".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Wither
public class ScenarioIgnoreCount {

    /**
     * Key-part: the source of the counted scenarios.
     */
    private Source source;

    /**
     * Key-part: the severity of the counted scenarios.
     */
    private String severityCode;

    /**
     * Key-part: counting ignored (true) or not ignored (false) scenarios.
     */
    private boolean ignored;

    /**
     * Value-part: the count of scenarios matching the key-part criteria.
     */
    private long count;

}
