package com.decathlon.ara.report.bean;

import lombok.Data;

@Data
public class Match {

    private String location;
    private Argument[] arguments = new Argument[0];

}
