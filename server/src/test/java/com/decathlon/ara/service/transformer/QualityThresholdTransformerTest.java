package com.decathlon.ara.service.transformer;

import com.decathlon.ara.ci.bean.QualityThreshold;
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
        Mockito.doThrow(new IOException("expected -- test purpose"))
                .when(objectMapper).readValue(value, QualityThresholdTransformer.TYPE_REFERENCE);
        // When
        Map<String, QualityThreshold> qualityThresholdDTOS = this.cut.toMap(value);
        // Then
        Assertions.assertThat(qualityThresholdDTOS).isNotNull();
        Assertions.assertThat(qualityThresholdDTOS).isEmpty();
    }
}