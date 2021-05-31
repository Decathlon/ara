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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.service.dto.source.SourceDTO;

@ExtendWith(MockitoExtension.class)
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