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

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QualityThresholdTransformerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QualityThresholdTransformer cut;

    @Test
    public void toMap_should_transform_the_json() throws IOException {
        // Given
        String value = "{'key' : 'value'}";
        Map<String, QualityThreshold> result = new HashMap<>();
        result.put("present", new QualityThreshold());
        Mockito.doReturn(result).when(objectMapper).readValue(value, QualityThresholdTransformer.TYPE_REFERENCE);
        // When
        Map<String, QualityThreshold> qualityThresholdDTOS = this.cut.toMap(value);
        // Then
        Assertions.assertThat(qualityThresholdDTOS).isNotNull();
        Assertions.assertThat(qualityThresholdDTOS).hasSize(1);
        Assertions.assertThat(qualityThresholdDTOS).containsKey("present");
    }

    @Test
    public void toDtos_should_send_empty_list_on_exception() throws IOException {
        // Given
        String value = "{'key' : 'value'}";
        Mockito.doThrow(new JsonProcessingException("expected -- test purpose") {})
                .when(objectMapper).readValue(value, QualityThresholdTransformer.TYPE_REFERENCE);
        // When
        Map<String, QualityThreshold> qualityThresholdDTOS = this.cut.toMap(value);
        // Then
        Assertions.assertThat(qualityThresholdDTOS).isNotNull();
        Assertions.assertThat(qualityThresholdDTOS).isEmpty();
    }
}
