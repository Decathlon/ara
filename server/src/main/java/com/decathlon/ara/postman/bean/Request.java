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
public class Request {

    /**
     * URL of the request.
     */
    private Url url;

    /**
     * All headers sent with the request (eg. "Content-Type: application/json").
     */
    private KeyValue[] header;

    /**
     * HTTP method of the request ("GET", "POST"...).
     */
    private String method;

    /**
     * The body content sent with the HTTP request.
     */
    private Body body;

}
