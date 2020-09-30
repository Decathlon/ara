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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;

@ExtendWith(MockitoExtension.class)
public class TypeTransformerTest {

    @Mock
    private SourceTransformer sourceTransformer;

    @InjectMocks
    private TypeTransformer cut;

    @Test
    public void toDtoWithSource_should_transform_type() {
        // Given
        Source source = new Source();
        Type value = new Type(1L, 66L, "code", "name", true, true, source);
        Mockito.doReturn(new SourceDTO()).when(sourceTransformer).toDto(source);
        // When
        TypeWithSourceDTO result = cut.toDtoWithSource(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isEqualTo("code");
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.isBrowser()).isTrue();
        Assertions.assertThat(result.isMobile()).isTrue();
        Mockito.verify(sourceTransformer).toDto(source);
    }

    @Test
    public void toDtoWithSource_should_return_empty_object_on_null() {
        // When
        TypeWithSourceDTO result = cut.toDtoWithSource(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isNull();
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.isBrowser()).isFalse();
        Assertions.assertThat(result.isMobile()).isFalse();
        Mockito.verify(sourceTransformer, Mockito.never()).toDto(Mockito.any());
    }
}