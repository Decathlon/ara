package com.decathlon.ara.web.rest.advice;

import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<Void> notUnique(NotUniqueException exception) {
        return ResponseEntity.badRequest().headers(HeaderUtil.notUnique(exception)).build();
    }

    @ExceptionHandler
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<Void> notFound(NotFoundException exception) {
        return ResponseEntity.notFound().headers(HeaderUtil.notFound(exception)).build();
    }

    @ExceptionHandler
    public ResponseEntity<Void> badRequest(BadRequestException exception) {
        return ResponseEntity.badRequest().headers(HeaderUtil.badRequest(exception)).build();
    }

}
