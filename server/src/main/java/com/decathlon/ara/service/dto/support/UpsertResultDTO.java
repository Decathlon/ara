package com.decathlon.ara.service.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

@Data
@Wither
@AllArgsConstructor
public class UpsertResultDTO<T> {

    /**
     * The DTO object that has just been UPdated or inSERTED into database.
     */
    private T upsertedDto;

    /**
     * Describe the operation that just happened on the given DTO.
     */
    private Upsert operation;

}
