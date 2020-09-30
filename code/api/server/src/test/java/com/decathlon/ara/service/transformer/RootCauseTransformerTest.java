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

import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;

@ExtendWith(MockitoExtension.class)
public class RootCauseTransformerTest {

    @InjectMocks
    private RootCauseTransformer cut;

    @Test
    public void toDto_should_transform_object() {
        // Given
        RootCause value = new RootCause(1L, 23L, "name", null);
        // When
        RootCauseDTO rootCauseDTO = cut.toDto(value);
        // Then
        Assertions.assertThat(rootCauseDTO).isNotNull();
        Assertions.assertThat(rootCauseDTO.getId()).isEqualTo(1L);
        Assertions.assertThat(rootCauseDTO.getName()).isEqualTo("name");
    }

    @Test
    public void toDto_should_return_empty_object_on_null_value() {
        // When
        RootCauseDTO rootCauseDTO = cut.toDto(null);
        // Then
        Assertions.assertThat(rootCauseDTO).isNotNull();
        Assertions.assertThat(rootCauseDTO.getId()).isEqualTo(0L);
        Assertions.assertThat(rootCauseDTO.getName()).isNull();
    }
}