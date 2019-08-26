/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.web.rest.util;

import com.decathlon.ara.service.exception.BadGatewayException;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 */
@UtilityClass
@Slf4j
public final class HeaderUtil {

    private static final String APPLICATION_NAME = "ara";

    // Information about an operation being processed successfully
    public static final String ALERT = "X-" + APPLICATION_NAME + "-alert";

    // Information about an error that occurred
    public static final String ERROR = "X-" + APPLICATION_NAME + "-error";
    public static final String PARAMS = "X-" + APPLICATION_NAME + "-params";
    public static final String MESSAGE = "X-" + APPLICATION_NAME + "-message";

    // More information about a not-unique error that occurred
    public static final String DUPLICATE_PROPERTY_NAME = "X-" + APPLICATION_NAME + "-duplicatePropertyName";
    public static final String OTHER_ENTITY_KEY = "X-" + APPLICATION_NAME + "-otherEntityKey";

    public static HttpHeaders entityCreated(String resourceName, Long createdId) {
        return entityCreated(resourceName, createdId.toString());
    }

    public static HttpHeaders entityCreated(String resourceName, String createdId) {
        return createAlert(APPLICATION_NAME + "." + resourceName + ".created", createdId);
    }

    public static HttpHeaders entityUpdated(String resourceName, Long updatedId) {
        return entityUpdated(resourceName, updatedId.toString());
    }

    public static HttpHeaders entityUpdated(String resourceName, String updatedId) {
        return createAlert(APPLICATION_NAME + "." + resourceName + ".updated", updatedId);
    }

    public static HttpHeaders entityMoved(String resourceName, Long movedId) {
        return createAlert(APPLICATION_NAME + "." + resourceName + ".moved", movedId.toString());
    }

    public static HttpHeaders entityDeleted(String resourceName, long deletedId) {
        return entityDeleted(resourceName, String.valueOf(deletedId));
    }

    public static HttpHeaders entityDeleted(String resourceName, String deletedId) {
        return createAlert(APPLICATION_NAME + "." + resourceName + ".deleted", deletedId);
    }

    public static HttpHeaders exception(String resourceName, Exception e) {
        return createError(resourceName, "exception", e.getMessage());
    }

    public static HttpHeaders idMustBeEmpty(String resourceName) {
        return createError(resourceName, "id_exists", "A new " + resourceName + " cannot already have an ID.");
    }

    public static HttpHeaders notFound(NotFoundException e) {
        return createError(e.getResourceName(), e.getErrorKey(), e.getMessage());
    }

    public static HttpHeaders badGateway(BadGatewayException e) {
        return createError(e.getResourceName(), e.getErrorKey(), e.getMessage());
    }

    public static HttpHeaders notUnique(NotUniqueException e) {
        HttpHeaders headers = createError(e.getResourceName(), e.getErrorKey(), e.getMessage());
        headers.add(DUPLICATE_PROPERTY_NAME, e.getDuplicatePropertyName());
        headers.add(OTHER_ENTITY_KEY, e.getOtherEntityKey());
        return headers;
    }

    public static HttpHeaders badRequest(BadRequestException e) {
        return createError(e.getResourceName(), e.getErrorKey(), e.getMessage());
    }

    public static URI uri(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new URISyntaxRuntimeException(e);
        }
    }

    public static URI uri(String str, String projectCode) {
        return uri(str.replace(RestConstants.PROJECT_CODE_REQUEST_PARAMETER, projectCode));
    }

    private static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ALERT, message);
        headers.add(PARAMS, param);
        return headers;
    }

    private static HttpHeaders createError(String resourceName, String errorKey, String errorMessage) {
        log.debug("Responding error header: {}", errorMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add(ERROR, "error." + errorKey);
        headers.add(PARAMS, resourceName);
        headers.add(MESSAGE, errorMessage);
        return headers;
    }

}
