package com.decathlon.ara.service.transformer;

import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
        Mockito.doThrow(new IOException("expected -- test purpose"))
                .when(objectMapper).readValue(value, QualitySeverityTransformer.TYPE_REFERENCE);
        // When
        List<QualitySeverityDTO> qualitySeverityDTOS = this.cut.toDtos(value);
        // Then
        Assertions.assertThat(qualitySeverityDTOS).isNotNull();
        Assertions.assertThat(qualitySeverityDTOS).isEmpty();
    }
}