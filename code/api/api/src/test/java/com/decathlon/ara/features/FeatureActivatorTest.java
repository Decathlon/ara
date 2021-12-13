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

package com.decathlon.ara.features;

import static org.junit.Assert.assertThrows;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class FeatureActivatorTest {

    @Mock
    private Environment environment;

    @Mock
    private Map<String, Boolean> currentStateMock;

    @Mock
    private Map<String, Boolean> defaultStatesMock;

    private FeatureActivator cut;

    @BeforeEach
    void setup() {
        this.cut = new FeatureActivator(environment);
        TestUtil.setField(cut, "currentStates", currentStateMock);
        TestUtil.setField(cut, "defaultStates", defaultStatesMock);
    }

    @Test
    void getState_should_return_the_current_state_of_a_feature() {
        // Given
        String feature1 = "my-feature";
        String feature2 = "second-one";
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature1);
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature2);
        Mockito.doReturn(false).when(currentStateMock).get(feature1);
        Mockito.doReturn(true).when(currentStateMock).get(feature2);

        // When
        boolean stateFeature1 = cut.getState("my-feature");
        boolean stateFeature2 = cut.getState("second-one");

        // Then
        Assertions.assertThat(stateFeature1).isFalse();
        Assertions.assertThat(stateFeature2).isTrue();
    }

    @Test
    void getState_should_throw_exception_if_not_found() {
        // Given
        String feature1 = "feature-1";
        Mockito.doReturn(false).when(currentStateMock).containsKey(feature1);
        // When
        assertThrows(IllegalArgumentException.class, () -> cut.getState("feature-1"));
        // Then
        // Assertions made in the annotation.
    }

    @Test
    void changeStateOf_should_change_current_state_of_feature() {
        // Given
        String feature1 = "feature-2";
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, true);
        // Then
        Mockito.verify(currentStateMock).put(feature1, true);
    }

    @Test
    void changeStateOf_should_not_change_state_if_bad_code() {
        // Given
        String feature1 = "feature-3";
        Mockito.doReturn(false).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, true);
        // Then
        Mockito.verify(currentStateMock, Mockito.never()).put(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    void changeStateOf_should_erase_and_not_flip() {
        // Given
        String feature1 = "feature-4";
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, false);
        // Then
        Mockito.verify(currentStateMock).put(feature1, false);
    }

    @Test
    void isEnabledByDefault_should_return_the_default_state_of_a_feature() {
        // Given
        String feature1 = "feature-5";
        String feature2 = "feature-6";
        Mockito.doReturn(true).when(defaultStatesMock).containsKey(feature1);
        Mockito.doReturn(true).when(defaultStatesMock).containsKey(feature2);
        Mockito.doReturn(false).when(defaultStatesMock).get(feature1);
        Mockito.doReturn(true).when(defaultStatesMock).get(feature2);

        // When
        boolean stateFeature1 = cut.isEnabledByDefault("feature-5");
        boolean stateFeature2 = cut.isEnabledByDefault("feature-6");

        // Then
        Assertions.assertThat(stateFeature1).isFalse();
        Assertions.assertThat(stateFeature2).isTrue();
    }

    @Test
    void isEnabledByDefault_should_throw_exception_if_not_found() {
        // Given
        String feature1 = "feature-7";
        Mockito.doReturn(false).when(defaultStatesMock).containsKey(feature1);
        // When
        assertThrows(IllegalArgumentException.class, () -> cut.isEnabledByDefault("feature-7"));
        // Then
        // Assertions made in the annotation.
    }
}
