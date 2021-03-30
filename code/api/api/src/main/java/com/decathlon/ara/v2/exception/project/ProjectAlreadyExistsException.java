package com.decathlon.ara.v2.exception.project;

import com.decathlon.ara.v2.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Project already exists")
public class ProjectAlreadyExistsException extends BusinessException {
}
