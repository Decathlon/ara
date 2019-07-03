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

package com.decathlon.ara.service.mapper;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for a serialized JSON String and its deserialized List&lt;QualitySeverityDTO&gt;.
 */
@Mapper
@Slf4j
public abstract class QualitySeverityMapper {

    private static final TypeReference<List<QualitySeverityDTO>> TYPE_REFERENCE_TO_LIST_QUALITY_SEVERITY = new TypeReference<List<QualitySeverityDTO>>() {
    };

    @Autowired
    private ObjectMapper objectMapper;

    public String qualitySeverityDTOToString(List<QualitySeverityDTO> severities) {
        if (severities == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(severities);
        } catch (JsonProcessingException e) {
            throw new NotGonnaHappenException("JSON serializing should not have failed when serializing to a String", e);
        }
    }

    /**
     *
     * @param json
     * @return
     * @deprecated
     * @see com.decathlon.ara.service.transformer.QualitySeverityTransformer#toDtos(String)
     */
    @Deprecated
    public List<QualitySeverityDTO> stringToQualitySeverityDTO(String json) {
        if (StringUtils.isEmpty(json)) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(json, TYPE_REFERENCE_TO_LIST_QUALITY_SEVERITY);
        } catch (IOException e) {
            log.error("Cannot parse qualitySeverities: {}", json, e);
            return new ArrayList<>();
        }
    }

}
