package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.service.dto.source.SourceDTO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SourceTransformerTest {

    @InjectMocks
    private SourceTransformer cut;

    @Test
    public void toDto_should_transform_the_object() {
        // Given
        Source value = new Source(1L, 22L, "code", "name", 'c',
                Technology.POSTMAN, "vcsUrl", "defaultBranch", true);
        // When
        SourceDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isEqualTo("code");
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.getLetter()).isEqualTo("c");
        Assertions.assertThat(result.getTechnology()).isEqualTo(Technology.POSTMAN);
        Assertions.assertThat(result.getVcsUrl()).isEqualTo("vcsUrl");
        Assertions.assertThat(result.getDefaultBranch()).isEqualTo("defaultBranch");
        Assertions.assertThat(result.isPostmanCountryRootFolders()).isTrue();
    }

    @Test
    public void toDto_should_return_empty_object_on_null() {
        // When
        SourceDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isNull();
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.getLetter()).isNull();
        Assertions.assertThat(result.getTechnology()).isNull();
        Assertions.assertThat(result.getVcsUrl()).isNull();
        Assertions.assertThat(result.getDefaultBranch()).isNull();
        Assertions.assertThat(result.isPostmanCountryRootFolders()).isFalse();
    }
}