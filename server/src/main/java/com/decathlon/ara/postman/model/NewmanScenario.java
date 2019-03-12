package com.decathlon.ara.postman.model;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.postman.bean.Execution;
import com.decathlon.ara.postman.bean.Failure;
import com.decathlon.ara.postman.bean.Item;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewmanScenario {

    /**
     * The failures of the item's execution: never null, but can be empty if no failure happened.
     */
    private final List<Failure> failures = new ArrayList<>();

    /**
     * The resulting ExecutedScenario currently built by the 3 Newman report elements relative to this folder or request and its execution and optional failure.
     */
    private ExecutedScenario scenario;

    /**
     * A folder or request, never null.
     */
    private Item item;

    /**
     * The execution of the item.<br>
     * Can be null if it had not got a chance to execute (request was in a not-ran folder, or item is a folder).<br>
     * BUT we remove all NewmanScenarios without any execution early on in the processing, so in most processing code, execution will not be null.
     */
    private Execution execution;

}
