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

import com.decathlon.ara.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for ResponseEntity creation.
 */
public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static ResponseEntity<Void> deleted(final String entityName, final long id) {
        return deleted(entityName, String.valueOf(id));
    }

    public static ResponseEntity<Void> deleted(final String entityName, final String id) {
        return ResponseEntity.ok().headers(HeaderUtil.entityDeleted(entityName, id)).build();
    }

    public static <X> ResponseEntity<X> handle(BadRequestException e) {
        if (e instanceof NotFoundException nfe) {
            return ResponseEntity.notFound()
                    .headers(HeaderUtil.notFound(nfe))
                    .build();
        } else if (e instanceof NotUniqueException nue) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.notUnique(nue))
                    .build();
        } else if (e instanceof BadGatewayException bge) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .headers(HeaderUtil.badGateway(bge))
                    .build();
        } else if (e instanceof ForbiddenException fe) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .headers(HeaderUtil.forbidden(fe))
                    .build();
        } else {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.badRequest(e))
                    .build();
        }
    }

}
