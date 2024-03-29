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

package com.decathlon.ara.service.dto.support;

public class UpsertResultDTO<T> {

    /**
     * The DTO object that has just been UPdated or inSERTED into database.
     */
    private T upsertedDto;

    /**
     * Describe the operation that just happened on the given DTO.
     */
    private Upsert operation;

    public UpsertResultDTO(T upsertedDto, Upsert operation) {
        super();
        this.upsertedDto = upsertedDto;
        this.operation = operation;
    }

    public T getUpsertedDto() {
        return upsertedDto;
    }

    public Upsert getOperation() {
        return operation;
    }

}
