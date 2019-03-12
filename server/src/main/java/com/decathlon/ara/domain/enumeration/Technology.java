package com.decathlon.ara.domain.enumeration;

/**
 * Reporting technologies supported by ARA, for it to know how to index reports of a run.
 */
public enum Technology {

    /**
     * Cucumber job (no matter if it runs Selenium or other technologies like RestAssured or Karate): index its
     * report.json result.
     */
    CUCUMBER,

    /**
     * Job running one or more Postman collection(s) using Newman: parse all its reports/*.json reports.
     */
    POSTMAN

}
