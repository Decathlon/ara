package com.decathlon.ara.report.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result {

    private Status status;

    @JsonProperty("error_message")
    private String errorMessage;

    private long duration;

}
