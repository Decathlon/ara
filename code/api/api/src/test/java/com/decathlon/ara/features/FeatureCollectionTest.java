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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class FeatureCollectionTest {

    @Mock
    private Map<String, IFeature> availableFeaturesMock;

    private FeatureCollection cut;

    @BeforeEach
    public void setup() {
        this.cut = new FeatureCollection(availableFeaturesMock);
    }

    @Test
    void list_should_return_all_features_available() {
        // Given
        IFeature feature1 = Mockito.mock(IFeature.class);
        IFeature feature2 = Mockito.mock(IFeature.class);
        IFeature feature3 = Mockito.mock(IFeature.class);
        Collection<IFeature> featuresSet = new ArrayList<>();
        featuresSet.add(feature1);
        featuresSet.add(feature2);
        featuresSet.add(feature3);
        Mockito.doReturn(featuresSet).when(availableFeaturesMock).values();
        // When
        List<IFeature> resultList = cut.list();
        // Then
        Assertions.assertThat(resultList).hasSize(3);
        Assertions.assertThat(resultList).containsExactly(feature1, feature2, feature3);
    }

    @Test
    void get_should_return_the_feature() {
        // Given
        IFeature wantedFeature = Mockito.mock(IFeature.class);
        Mockito.doReturn(true).when(availableFeaturesMock).containsKey("my-feature");
        Mockito.doReturn(wantedFeature).when(availableFeaturesMock).get("my-feature");
        // When
        Optional<IFeature> iFeature = cut.get("my-feature");
        // Then
        Assertions.assertThat(iFeature.isPresent()).isTrue();
        Assertions.assertThat(iFeature.get()).isNotNull();
        Assertions.assertThat(iFeature.get()).isEqualTo(wantedFeature);
    }

    @Test
    void get_should_return_empty_if_not_found() {
        // Given
        IFeature wantedFeature = Mockito.mock(IFeature.class);
        Mockito.doReturn(false).when(availableFeaturesMock).containsKey("nope");
        // When
        Optional<IFeature> iFeature = cut.get("nope");
        // Then
        Assertions.assertThat(iFeature.isPresent()).isFalse();
        Mockito.verify(availableFeaturesMock, Mockito.never()).get(Mockito.anyString());
    }
}
