package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ExporterTest {
    private static String TEST_STR = "This is a test string";

    private Exporter sut;

    @Before
    public void setUp() {
        this.sut = Mockito.spy(new BaseTestExporter());
    }

    @Test
    public void generateAndEncodeB64_should_return_the_encoded_generated_byte_array() {
        // Given
        List<FunctionalityDTO> functionalities = new ArrayList<>();
        byte[] decodedArray = TEST_STR.getBytes(StandardCharsets.UTF_8);
        byte[] expected = Base64.getEncoder().encode(decodedArray);
        // When
        byte[] actual = this.sut.generateAndEncodeB64(functionalities, null);
        // Then
        Assertions.assertThat(actual).containsExactly(expected);
        Mockito.verify(this.sut).generate(functionalities, null);
    }

    @Test
    public void suitableFor_should_return_false_when_export_name_is_null() {
        // When
        boolean result = this.sut.suitableFor(null);
        // Then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void suitableFor_should_check_that_export_name_is_equal_to_id() {
        // Given
        String exportName = TEST_STR.toLowerCase().replace(" ", "_");
        // When
        boolean result = this.sut.suitableFor(exportName);
        // Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void getId_should_return_the_name_in_lowercase_and_replace_space_by_underscores() {
        // Given
        String expected = TEST_STR.toLowerCase().replace(" ", "_");
        // When
        String actual = this.sut.getId();
        // Then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void listRequiredFields_should_return_an_empty_list_by_default() {
        // When
        List<ExportField> actual = this.sut.listRequiredFields();
        // Then
        Assertions.assertThat(actual).isEmpty();
    }


    class BaseTestExporter extends Exporter {

        @Override
        public String getName() {
            return TEST_STR;
        }

        @Override
        public String getDescription() {
            return null; // Not the purpose of this test class
        }

        @Override
        public String getFormat() {
            return null; // Not the purpose of this test class
        }

        @Override
        protected byte[] generate(List<FunctionalityDTO> functionalities, Map<String, String> requiredInfos) {
            return this.getName().getBytes(StandardCharsets.UTF_8);
        }
    }
}