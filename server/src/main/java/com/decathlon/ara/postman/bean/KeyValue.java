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
public class KeyValue {

    /**
     * Name of the header (eg. "Content-Type") or of the query parameter or form data or url-encoded request body (eg. "id").
     */
    private String key;

    /**
     * Value of the header (eg. "application/json") or of the query parameter or form data or url-encoded request body (eg. "42").
     */
    private String value;

}
