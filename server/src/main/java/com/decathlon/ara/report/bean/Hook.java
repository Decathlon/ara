package com.decathlon.ara.report.bean;

import com.decathlon.ara.report.support.ResultsWithMatch;
import lombok.Data;

@Data
public class Hook implements ResultsWithMatch {

    private Result result;
    private Match match;
    private Embedded[] embeddings = new Embedded[0];

}
