package com.decathlon.ara.service.exception;

import com.decathlon.ara.Entities;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ForbiddenException extends BadRequestException {

    public ForbiddenException(String resourceName, String actionDescription, Pair<String, String>... contextKeysAndValues) {
        super(getSecurityMessage(resourceName, actionDescription, contextKeysAndValues), Entities.SECURITY, "forbidden");
    }

    private static String getSecurityMessage(String resourceName, String actionDescription, Pair<String, String>... contextKeysAndValues) {
        var contextDescription = "";
        if (contextKeysAndValues.length > 0) {
            var contextAsString = Arrays.stream(contextKeysAndValues)
                    .map(contextPair -> String.format("[%s]: '%s'", contextPair.getFirst(), contextPair.getSecond()))
                    .collect(Collectors.joining("\n"));
            contextDescription = String.format("%nContext:%n%s", contextAsString);
        }

        return String.format("You are not allowed to perform this action (%s) on this resource (%s).%s", actionDescription, resourceName, contextDescription);
    }
}
