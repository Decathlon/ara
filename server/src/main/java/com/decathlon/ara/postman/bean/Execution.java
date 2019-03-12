package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Execution {

    /**
     * Object with only the UUID of the request, to map a request's ID with its execution.
     */
    private ItemId item;

    /**
     * Description of the executed request (not for a folder).
     */
    private Request request;

    /**
     * The actual HTTP response of the request.
     */
    private Response response;

    /**
     * All executed assertions on this request (both from the request test-script and parent folders test-scripts).<br>
     * The list of assertions is created by JavaScript: to list all assertions, there is no other way than executing the collection.
     */
    private Assertion[] assertions;

}
