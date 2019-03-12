package com.decathlon.ara.report.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Doc Strings are handy for specifying a larger piece of text. This is inspired from Python’s Docstring syntax. In your step definition, there’s
 * no need to find this text and match it in your Regexp. It will automatically be passed as the last parameter in the step definition.
 */
@Data
public class DocString {

    private String value;

    @JsonProperty("content_type")
    private String contentType;

    private Integer line;

}
