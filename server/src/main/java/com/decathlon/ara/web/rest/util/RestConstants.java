package com.decathlon.ara.web.rest.util;

import lombok.experimental.UtilityClass;

import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;

@UtilityClass
public final class RestConstants {

    /**
     * The root path of all REST API resources.
     */
    public static final String API_PATH = "/api";

    public static final String PROJECT_CODE_REQUEST_PARAMETER = "{projectCode:" + CODE_PATTERN + "}";

    /**
     * The root path of all REST API resources requiring the context of a project to be able to operate.
     */
    public static final String PROJECT_API_PATH = API_PATH + "/projects/" + PROJECT_CODE_REQUEST_PARAMETER;

}
