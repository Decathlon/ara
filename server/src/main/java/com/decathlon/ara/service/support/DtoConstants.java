package com.decathlon.ara.service.support;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoConstants {

    public static final String CODE_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    public static final String CODE_MESSAGE = "The code must be one or more groups of lower-case letters or digits, optionally separated by dashes (\"-\").";
    public static final String CODE_NAME_MESSAGE = "The name must be one or more groups of lower-case letters or digits, optionally separated by dashes (\"-\").";

}
