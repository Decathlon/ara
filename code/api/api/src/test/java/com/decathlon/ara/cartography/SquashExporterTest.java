package com.decathlon.ara.cartography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;

@ExtendWith(MockitoExtension.class)
public class SquashExporterTest {

    @Spy
    private SquashExporter sut;

    @Test
    public void getName() {
        // Given
        final String expected = "SquashTM";
        // When
        String actualName = this.sut.getName();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    public void getDescription() {
        // Given
        final String expected = "Export this cartography to import it as requirements in SquashTM";
        // When
        String actualName = this.sut.getDescription();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    public void getFormat() {
        // Given
        final String expected = "xls";
        // When
        String actualName = this.sut.getFormat();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    public void generate_should_create_a_header_row_even_when_no_functionalities() {
        // Given
        List<FunctionalityDTO> functionalities = new ArrayList<>();
        Map<String, String> requiredInfos = new HashMap<>();
        requiredInfos.put(SquashExporter.PROJECT_NAME, "project");
        requiredInfos.put(SquashExporter.USER, "user");
        // When
        byte[] result = this.sut.generate(functionalities, requiredInfos);
        // Then
        Mockito.verify(this.sut, Mockito.times(1)).createHeaderRow(Mockito.any());
        Mockito.verify(this.sut, Mockito.never()).addRowToSheet(Mockito.any(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
        Assertions.assertThat(result).isNotEmpty();
    }


    @Test
    public void generate_should_generate_one_line_per_functionality() {
        // Given
        List<FunctionalityDTO> functionalities = new ArrayList<>();
        functionalities.add(this.create_dummy_functionality(1));
        functionalities.add(this.create_dummy_functionality(2));
        Map<String, String> requiredInfos = new HashMap<>();
        requiredInfos.put(SquashExporter.PROJECT_NAME, "project");
        requiredInfos.put(SquashExporter.USER, "user");
        // When
        byte[] result = this.sut.generate(functionalities, requiredInfos);
        // Then
        Mockito.verify(this.sut, Mockito.times(1)).createHeaderRow(Mockito.any());
        Mockito.verify(this.sut, Mockito.times(functionalities.size())).addRowToSheet(Mockito.any(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
        Assertions.assertThat(result).isNotEmpty();
    }

    @Test
    public void listRequiredFields() {
        // When
        List<ExportField> fields = this.sut.listRequiredFields();
        // Then
        Assertions.assertThat(fields).hasSize(2);
        Assertions.assertThat(fields.get(0)).isNotNull();
        Assertions.assertThat(fields.get(1)).isNotNull();
        Assertions.assertThat(fields.get(0).getId()).isEqualTo(SquashExporter.PROJECT_NAME);
        Assertions.assertThat(fields.get(0).getName()).isEqualTo("Targeted project's name");
        Assertions.assertThat(fields.get(0).getType()).isEqualTo("string");
        Assertions.assertThat(fields.get(0).getDescription()).isNotBlank();
        Assertions.assertThat(fields.get(1).getId()).isEqualTo(SquashExporter.USER);
        Assertions.assertThat(fields.get(1).getName()).isEqualTo("Targeted User's name");
        Assertions.assertThat(fields.get(1).getType()).isEqualTo("string");
        Assertions.assertThat(fields.get(1).getDescription()).isNotBlank();
    }

    private FunctionalityDTO create_dummy_functionality(long id) {
        FunctionalityDTO result = new FunctionalityDTO();
        result.setId(id);
        result.setName("name-functionality-" + id);
        result.setType(FunctionalityType.FUNCTIONALITY.name());
        result.setCountryCodes("FR");
        result.setTeamId(1L);
        result.setSeverity("HIGH");
        result.setCreated("09/09/2009");
        result.setCoveredScenarios(3);
        result.setCoveredCountryScenarios("FR");
        result.setIgnoredScenarios(0);
        result.setIgnoredCountryScenarios("FR");
        result.setComment("This is a comment");
        return result;
    }
}
