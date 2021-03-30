package com.decathlon.ara.v2.exception.project;

import com.decathlon.ara.v2.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Project not found")
public class UnknownProjectException extends BusinessException {
}
