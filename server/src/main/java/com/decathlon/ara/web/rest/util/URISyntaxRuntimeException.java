package com.decathlon.ara.web.rest.util;

import java.net.URISyntaxException;

/**
 * URISyntaxException made unchecked for cases where it would be a programming error rather that a user error.
 */
class URISyntaxRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 2807142911588081627L;

    URISyntaxRuntimeException(URISyntaxException e) {
        super(e.getMessage(), e);
    }

}
