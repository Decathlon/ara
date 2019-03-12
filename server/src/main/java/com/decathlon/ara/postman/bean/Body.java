package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {

    /**
     * Either:
     * <ul>
     * <li>"formdata": the {@code formData} property has fields sent to the server</li>
     * <li>"urlencoded": the {@code urlEncoded} property has fields sent to the server</li>
     * <li>"raw": the {@code raw} property has the body sent to the server, as a String (usually a JSON content)</li>
     * <li>"file": the file is not saved in the Postman exported collection, so this cannot be played by Newman</li>
     * <li>null: the request has no body</li>
     * </ul>
     */
    private String mode;

    /**
     * @see #mode
     */
    @JsonProperty("formdata")
    private KeyValue[] formData;

    /**
     * @see #mode
     */
    @JsonProperty("urlencoded")
    private KeyValue[] urlEncoded;

    /**
     * @see #mode
     */
    private String raw;

}
