package com.decathlon.ara.service.exception;

public class BadGatewayException extends BadRequestException {

    private static final long serialVersionUID = 2807142911588081627L;

    public BadGatewayException(final String message, final String resourceName) {
        super(message, resourceName, "bad_gateway");
    }

}
