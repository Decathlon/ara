package com.decathlon.ara.web.rest.util;

import com.decathlon.ara.service.exception.BadGatewayException;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for ResponseEntity creation.
 */
@UtilityClass
public final class ResponseUtil {

    public static ResponseEntity<Void> deleted(final String entityName, final long id) {
        return deleted(entityName, String.valueOf(id));
    }

    public static ResponseEntity<Void> deleted(final String entityName, final String id) {
        return ResponseEntity.ok().headers(HeaderUtil.entityDeleted(entityName, id)).build();
    }

    public static <X> ResponseEntity<X> handle(BadRequestException e) {
        if (e instanceof NotFoundException) {
            return ResponseEntity.notFound()
                    .headers(HeaderUtil.notFound((NotFoundException) e))
                    .build();
        } else if (e instanceof NotUniqueException) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.notUnique((NotUniqueException) e))
                    .build();
        } else if (e instanceof BadGatewayException) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .headers(HeaderUtil.badGateway((BadGatewayException) e))
                    .build();
        } else {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.badRequest(e))
                    .build();
        }
    }

}
