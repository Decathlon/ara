package com.decathlon.ara.service.exception;

import lombok.Getter;

public class BadRequestException extends Exception {

    private static final long serialVersionUID = 2807142911588081627L;

    @Getter
    private final String resourceName;

    @Getter
    private final String errorKey;

    public BadRequestException(final String message, final String resourceName, final String errorKey) {
        super(message);
        this.resourceName = resourceName;
        this.errorKey = errorKey;
    }

}
