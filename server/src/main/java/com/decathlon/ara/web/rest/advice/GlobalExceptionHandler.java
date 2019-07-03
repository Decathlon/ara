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
