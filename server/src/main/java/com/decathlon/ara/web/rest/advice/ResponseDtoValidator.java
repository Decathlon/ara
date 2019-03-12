package com.decathlon.ara.web.rest.advice;

import com.decathlon.ara.web.rest.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RestController
public class ResponseDtoValidator extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_SEPARATOR = "{{NEW_LINE}}";

    /**
     * Transforms DTO validation annotation failures to proper error messages to be returned to the client via HTTP
     * headers.<br>
     * Annotations like {@link javax.validation.constraints.Size @Size},
     * {@link javax.validation.constraints.NotNull @NotNull} or {@link javax.validation.constraints.Pattern @Pattern}.
     *
     * @param ex      the validation exception with field binding errors
     * @param headers the headers where errors will be written to the response
     * @param status  (unused)
     * @param request the current request, whose resource name is extracted
     * @return a Bad Request response entity with populated errors in headers
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        StringBuilder message = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message
                    .append((message.length() == 0) ? "" : MESSAGE_SEPARATOR)
                    .append(error.getDefaultMessage());
        }

        headers.add(HeaderUtil.MESSAGE, message.toString());
        headers.add(HeaderUtil.ERROR, "error.validation");
        headers.add(HeaderUtil.PARAMS, getResourceName(request));

        return ResponseEntity.badRequest().headers(headers).build();
    }

    /**
     * Returns the resource name for a given API URL.<br>
     * If the URL is a project URL, "projects" is returned.<br>
     * If the URL is a resource inside a project, that resource name is returned.
     *
     * @param request the HTTP request made to the ARA's API server
     *                (with eg. URL "/api/projects/some-project/some-resources/api")
     * @return the name of the resource from the URL of the request (eg. "some-resources")
     */
    String getResourceName(WebRequest request) {
        String description = request.getDescription(false);
        String[] uriParts = description.split("/");
        String resourceName;
        if (uriParts.length >= 5) {
            resourceName = uriParts[4];
        } else if (uriParts.length >= 3) {
            resourceName = uriParts[2];
            // TODO Will return plural version ("countries" instead of "country")
        } else {
            log.error("No resource in {}", description);
            resourceName = "unknown";
        }
        return resourceName;
    }

}
