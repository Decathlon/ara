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

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;

public class NewFunctionalityDTO {

    private FunctionalityDTO functionality;

    private Long referenceId;

    private FunctionalityPosition relativePosition;

    public NewFunctionalityDTO() {
    }

    public NewFunctionalityDTO(FunctionalityDTO functionality, Long referenceId,
            FunctionalityPosition relativePosition) {
        this.functionality = functionality;
        this.referenceId = referenceId;
        this.relativePosition = relativePosition;
    }

    public FunctionalityDTO getFunctionality() {
        return functionality;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public FunctionalityPosition getRelativePosition() {
        return relativePosition;
    }

}
