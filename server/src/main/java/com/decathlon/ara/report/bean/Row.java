package com.decathlon.ara.report.bean;

import lombok.Data;

@Data
public class Row {

    private Integer line;
    private String[] cells = new String[0];
    private Comment[] comments = new Comment[0];

}
