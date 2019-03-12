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
public class Response {

    /**
     * The status line of the HTTP response (eg. "OK" or "Internal Server Error").
     */
    private String status;

    /**
     * The status code of the HTTP response (eg. 200 or 500).
     */
    private int code;

    /**
     * All the actual headers sent with the request (eg. "Content-Type: application/json"):
     * both configured in the collection and added by Newman during the session.
     */
    private KeyValue[] header;

    /**
     * The HTTP body of the response.
     */
    private Stream stream;

    /**
     * The time in milliseconds between request sent and response received.
     */
    private long responseTime;

}
