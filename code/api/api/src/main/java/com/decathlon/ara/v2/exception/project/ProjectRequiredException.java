package com.decathlon.ara.v2.exception.project;

import com.decathlon.ara.v2.exception.BusinessException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Project was required but not given")
public class ProjectRequiredException extends BusinessException {
}
