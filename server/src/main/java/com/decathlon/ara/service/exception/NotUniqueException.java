package com.decathlon.ara.service.exception;

import lombok.Getter;

public class NotUniqueException extends BadRequestException {

    private static final long serialVersionUID = 2807142911588081627L;

    /**
     * The name of the duplicate property between two entities, violating a unique constraint on that entity.
     */
    @Getter
    private final String duplicatePropertyName;

    /**
     * The primary key of the other entity with same duplicatePropertyName value.
     */
    @Getter
    private final String otherEntityKey;

    public NotUniqueException(final String message, final String resourceName, final String duplicatePropertyName, final Long otherEntityKey) {
        this(message, resourceName, duplicatePropertyName, otherEntityKey.toString());
    }

    public NotUniqueException(final String message, final String resourceName, final String duplicatePropertyName, final String otherEntityKey) {
        super(message, resourceName, "not_unique");
        this.duplicatePropertyName = duplicatePropertyName;
        this.otherEntityKey = otherEntityKey;
    }

}
