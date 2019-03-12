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
public class Event {

    /**
     * Defines the type of the event: the script is either a pre-request script or a test script (post-request).
     */
    private Listen listen;

    /**
     * The JavaScript content.
     */
    private Script script;

}
