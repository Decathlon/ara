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

package com.decathlon.ara.service.dto.type;

import javax.validation.Valid;

import com.decathlon.ara.service.dto.source.SourceDTO;

public class TypeWithSourceDTO extends TypeDTO {

    @Valid
    private SourceDTO source;

    public TypeWithSourceDTO() {
    }

    public TypeWithSourceDTO(String code) {
        super(code, null, false, false);
    }

    public SourceDTO getSource() {
        return source;
    }

}
