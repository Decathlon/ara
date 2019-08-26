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

package com.decathlon.ara.service.exception;

import lombok.Getter;

public class NotUniqueException extends BadRequestException {

    private static final long serialVersionUID = 2807142911588081627L;

    /**
     * The name of the duplicate property between two entities, violating a unique constraint on that entity.
     */
    @Getter
    private final String duplicatePropertyName;

    /**
     * The primary key of the other entity with same duplicatePropertyName value.
     */
    @Getter
    private final String otherEntityKey;

    public NotUniqueException(final String message, final String resourceName, final String duplicatePropertyName, final Long otherEntityKey) {
        this(message, resourceName, duplicatePropertyName, otherEntityKey.toString());
    }

    public NotUniqueException(final String message, final String resourceName, final String duplicatePropertyName, final String otherEntityKey) {
        super(message, resourceName, "not_unique");
        this.duplicatePropertyName = duplicatePropertyName;
        this.otherEntityKey = otherEntityKey;
    }

}
