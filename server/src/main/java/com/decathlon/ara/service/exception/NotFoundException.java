package com.decathlon.ara.service.exception;

public class NotFoundException extends BadRequestException {

    private static final long serialVersionUID = 2807142911588081627L;

    public NotFoundException(final String message, String resourceName) {
        super(message, resourceName, "not_found");
    }

}
