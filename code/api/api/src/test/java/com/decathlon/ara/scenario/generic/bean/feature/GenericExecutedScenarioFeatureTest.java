/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.generic.bean.feature;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class GenericExecutedScenarioFeatureTest {

    @Test
    void getTagsAsString_returnEmptyString_whenNoTags() {
        // Given
        GenericExecutedScenarioFeature feature = new GenericExecutedScenarioFeature();

        // When

        // Then
        String tagsAsString = feature.getTagsAsString();
        assertThat(tagsAsString).isNotNull().isEmpty();
    }

    @Test
    void getTagsAsString_returnTagsAsString_whenSingleTagGiven() {
        // Given
        GenericExecutedScenarioFeature feature = new GenericExecutedScenarioFeature();
        TestUtil.setField(feature, "tags", List.of("tag"));

        // When

        // Then
        String tagsAsString = feature.getTagsAsString();
        assertThat(tagsAsString).isEqualTo("@tag");
    }

    @Test
    void getTagsAsString_returnTagsAsString_whenSeveralTagsGiven() {
        // Given
        GenericExecutedScenarioFeature feature = new GenericExecutedScenarioFeature();
        TestUtil.setField(feature, "tags", List.of("tag1", "tag2", "tag3"));

        // When

        // Then
        String tagsAsString = feature.getTagsAsString();
        assertThat(tagsAsString).isEqualTo("@tag1 @tag2 @tag3");
    }

}
