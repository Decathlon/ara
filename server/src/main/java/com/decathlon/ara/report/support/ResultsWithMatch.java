package com.decathlon.ara.report.support;

import com.decathlon.ara.report.bean.Embedded;
import com.decathlon.ara.report.bean.Match;
import com.decathlon.ara.report.bean.Result;

/**
 * Ensures that class delivers method for counting results and matches.
 */
public interface ResultsWithMatch {

    Result getResult();

    Match getMatch();

    Embedded[] getEmbeddings();

}
