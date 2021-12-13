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

package com.decathlon.ara.service.dto.request;

public class MoveFunctionalityDTO {

    /**
     * The functionality ID to move.
     */
    private Long sourceId;

    /**
     * The destination reference ID: null if reference is the (virtual) root folder.
     */
    private Long referenceId;

    /**
     * The position where to move sourceId relative to referenceId.
     */
    private FunctionalityPosition relativePosition;

    public Long getSourceId() {
        return sourceId;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public FunctionalityPosition getRelativePosition() {
        return relativePosition;
    }

}
