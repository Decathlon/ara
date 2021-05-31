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

package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Type.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class TypeTransformer {

    @Autowired
    private SourceTransformer sourceTransformer;

    /**
     * Transform the given Type DO to a TypeWithSourceDTO object.
     * <p>
     * Returns an empty TypeWithSourceDTO if the parameter is null.
     *
     * @param type the DO to transform
     * @return the result DTO.
     */
    TypeWithSourceDTO toDtoWithSource(Type type) {
        TypeWithSourceDTO result = new TypeWithSourceDTO();
        if (null != type) {
            result.setCode(type.getCode());
            result.setName(type.getName());
            result.setBrowser(type.isBrowser());
            result.setMobile(type.isMobile());
            result.setSource(sourceTransformer.toDto(type.getSource()));
        }
        return result;
    }
}
