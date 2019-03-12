package com.decathlon.ara.domain.enumeration;

public enum JobStatus {

    /**
     * Just created in database, but the job has not been crawled yet, or it has not been run yet, so crawling failed for now.
     */
    PENDING,

    /**
     * Created in database, crawled and found the job is still running.
     */
    RUNNING,

    /**
     * Created in database, crawled and found the job is terminated (either as a success, failure or any other results) and will not be updated anymore.
     */
    DONE,

    /**
     * Created in database, crawled but the job has not been found, and the parent job terminated, so this one will never have a chance to run anymore: something went wrong before.
     */
    UNAVAILABLE

}
