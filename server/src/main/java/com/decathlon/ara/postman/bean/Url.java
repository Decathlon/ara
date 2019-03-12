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
public class Url {

    /**
     * Protocol (eg. "http" or "https").
     */
    private String protocol;

    /**
     * Port (eg. "8080" or "{{server_port}}").
     */
    private String port;

    /**
     * Path (eg. [ "the", "path" ] for "/the/path").
     */
    private String[] path;

    /**
     * Host (eg. [ "example", "org" ] for "example.org").
     */
    private String[] host;

    /**
     * All encoded query parameters (eg. "parameter: foo%26bar" for parameter "parameter"="foo&bar").
     */
    private KeyValue[] query;

}
