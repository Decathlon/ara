package com.decathlon.ara.report.bean;

import lombok.Data;

@Data
public class Feature {

    private String id;
    private String name;
    private String uri;
    private String description;
    private String keyword;
    private Integer line;
    private Comment[] comments = new Comment[0];
    private Element[] elements = new Element[0];
    private Tag[] tags = new Tag[0];

    public String getReportFileName() {
        // Simplified version of
        // net.masterthought.cucumber.json.Feature.setReportFileName(int jsonFileNo, Configuration configuration) :
        // * no support for multiple report.json files (we do not use that)
        // * nor for parallel execution by official Maven plugin (we use our Cucumber fork managing parallelism more efficiently and effectively)
        return uri.replaceAll("[^\\d\\w]", "-") + ".html";
    }

}
