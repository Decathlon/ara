package com.decathlon.ara.features;

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class FeatureActivatorTest {

    @Mock
    private Environment environment;

    @Mock
    private Map<String, Boolean> currentStateMock;

    @Mock
    private Map<String, Boolean> defaultStatesMock;

    private FeatureActivator cut;

    @Before
    public void setup() {
        this.cut = new FeatureActivator(environment, currentStateMock, defaultStatesMock);
    }

    @Test
    public void getState_should_return_the_current_state_of_a_feature() {
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

    @Test(expected = IllegalArgumentException.class)
    public void getState_should_throw_exception_if_not_found() {
        // Given
        String feature1 = "feature-1";
        Mockito.doReturn(false).when(currentStateMock).containsKey(feature1);
        // When
        cut.getState("feature-1");
        // Then
        // Assertions made in the annotation.
    }

    @Test
    public void changeStateOf_should_change_current_state_of_feature() {
        // Given
        String feature1 = "feature-2";
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, true);
        // Then
        Mockito.verify(currentStateMock).put(feature1, true);
    }

    @Test
    public void changeStateOf_should_not_change_state_if_bad_code() {
        // Given
        String feature1 = "feature-3";
        Mockito.doReturn(false).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, true);
        // Then
        Mockito.verify(currentStateMock, Mockito.never()).put(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void changeStateOf_should_erase_and_not_flip() {
        // Given
        String feature1 = "feature-4";
        Mockito.doReturn(true).when(currentStateMock).containsKey(feature1);
        // When
        cut.changeStateOf(feature1, false);
        // Then
        Mockito.verify(currentStateMock).put(feature1, false);
    }

    @Test
    public void isEnabledByDefault_should_return_the_default_state_of_a_feature() {
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

    @Test(expected = IllegalArgumentException.class)
    public void isEnabledByDefault_should_throw_exception_if_not_found() {
        // Given
        String feature1 = "feature-7";
        Mockito.doReturn(false).when(defaultStatesMock).containsKey(feature1);
        // When
        cut.isEnabledByDefault("feature-7");
        // Then
        // Assertions made in the annotation.
    }
}
