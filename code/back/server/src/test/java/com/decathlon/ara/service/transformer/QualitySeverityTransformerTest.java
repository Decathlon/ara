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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class QualitySeverityTransformerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QualitySeverityTransformer cut;

    @Test
    public void toDtos_should_call_object_mapper() throws IOException {
        // Given
        String value = "{'key' : 'value'}";
        QualitySeverityDTO result = new QualitySeverityDTO();
        result.setPercent(98);
        List<QualitySeverityDTO> mapperList = new ArrayList<>();
        mapperList.add(result);
        Mockito.doReturn(mapperList).when(objectMapper).readValue(value, QualitySeverityTransformer.TYPE_REFERENCE);
        // When
        List<QualitySeverityDTO> qualitySeverityDTOS = this.cut.toDtos(value);
        // Then
        Assertions.assertThat(qualitySeverityDTOS).isNotNull();
        Assertions.assertThat(qualitySeverityDTOS).hasSize(1);
        Assertions.assertThat(qualitySeverityDTOS.get(0)).isNotNull();
        Assertions.assertThat(qualitySeverityDTOS.get(0).getPercent()).isEqualTo(98);
    }

    @Test
    public void toDtos_should_send_empty_list_on_exception() throws IOException {
        // Given
        String value = "{'key' : 'value'}";
        Mockito.doThrow(new JsonProcessingException("expected -- test purpose"){})
                .when(objectMapper).readValue(value, QualitySeverityTransformer.TYPE_REFERENCE);
        // When
        List<QualitySeverityDTO> qualitySeverityDTOS = this.cut.toDtos(value);
        // Then
        Assertions.assertThat(qualitySeverityDTOS).isNotNull();
        Assertions.assertThat(qualitySeverityDTOS).isEmpty();
    }
}
