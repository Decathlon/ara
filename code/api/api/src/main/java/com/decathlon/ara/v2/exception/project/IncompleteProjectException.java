package com.decathlon.ara.v2.exception.project;

import com.decathlon.ara.v2.exception.BusinessException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Project has not all the required fields")
public class IncompleteProjectException extends BusinessException {
}
