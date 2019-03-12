package com.decathlon.ara.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureCollectionTest {

    @Mock
    private Map<String, IFeature> availableFeaturesMock;

    private FeatureCollection cut;

    @Before
    public void setup() {
        this.cut = new FeatureCollection(availableFeaturesMock);
    }

    @Test
    public void list_should_return_all_features_available() {
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
    public void get_should_return_the_feature() {
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
    public void get_should_return_empty_if_not_found() {
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
