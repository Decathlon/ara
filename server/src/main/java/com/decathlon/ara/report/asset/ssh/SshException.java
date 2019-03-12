package com.decathlon.ara.report.asset.ssh;

public class SshException extends Exception {

    private static final long serialVersionUID = 2807142911588081627L;

    SshException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
