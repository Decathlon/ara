package com.decathlon.ara.cartography;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
class AraExporterIT {

    @Autowired
    private AraExporter sut;

    @Test
    void getName_should_return_ARA() {
        // Given
        final String expected = "ARA";
        // When
        String actualName = this.sut.getName();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    void getDescription_should_return_the_ara_description() {
        // Given
        final String expected = "Export this cartography to import it in another ARA project";
        // When
        String actualName = this.sut.getDescription();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    void getFormat_should_return_json() {
        // Given
        final String expected = "json";
        // When
        String actualName = this.sut.getFormat();
        // Then
        Assertions.assertThat(actualName).isEqualTo(expected);
    }

    @Test
    void generate_should_use_ara_cartography_mapper() {
        // Given
        final AraCartographyMapper mapper = new AraCartographyMapper();
        final List<FunctionalityDTO> functionalities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            functionalities.add(this.create_dummy_functionality(i));
        }
        final byte[] expected = mapper.asString(functionalities).getBytes(StandardCharsets.UTF_8);
        // When
        byte[] actual = this.sut.generate(functionalities, null);
        // Then
        Assertions.assertThat(actual).containsExactly(expected);
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
