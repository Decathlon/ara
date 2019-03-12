package com.decathlon.ara.service;

import com.decathlon.ara.features.FeatureActivator;
import com.decathlon.ara.features.FeatureCollection;
import com.decathlon.ara.features.IFeature;
import com.decathlon.ara.service.dto.feature.DetailledFeatureDTO;
import com.decathlon.ara.service.dto.feature.FeatureDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureServiceTest {

    @Mock
    private FeatureCollection featureCollectionMock;

    @Mock
    private FeatureActivator featureActivatorMock;

    @InjectMocks
    private FeatureService cut;

    @Test
    public void listAll_should_return_the_list_of_features() {
        // Given
        String feature1Code = "feature-1";
        String feature2Code = "my-feature";
        List<IFeature> features = new ArrayList<>();
        IFeature mockFeature1 = given_a_feature(feature1Code, "Desc 1", "Feature 1");
        IFeature mockFeature2 = given_a_feature(feature2Code, "Desc 2", "Feature 2");
        features.add(mockFeature1);
        features.add(mockFeature2);
        Mockito.doReturn(features).when(featureCollectionMock).list();
        Mockito.doReturn(false).when(featureActivatorMock).getState(feature1Code);
        Mockito.doReturn(true).when(featureActivatorMock).getState(feature2Code);
        // When
        List<FeatureDTO> featuresDTO = cut.listAll();
        // Then
        Assertions.assertThat(featuresDTO).hasSize(features.size());
        Assertions.assertThat(featuresDTO.get(0)).isNotNull();
        Assertions.assertThat(featuresDTO.get(0)).isEqualTo(new FeatureDTO(feature1Code, false));
        Assertions.assertThat(featuresDTO.get(1)).isEqualTo(new FeatureDTO(feature2Code, true));
    }

    @Test
    public void find_should_return_the_detailled_informations() throws NotFoundException {
        // Given
        String wantedFeatureCode = "my-feature";
        String wantedFeatureDesc = "Description of Feature";
        String wantedFeatureName = "Name of Feature";
        IFeature wantedFeature = given_a_feature(wantedFeatureCode, wantedFeatureDesc, wantedFeatureName);
        IFeature unwantedFeature = given_a_feature("nope", "Nope Desc", "Nope Name");
        List<IFeature> features = new ArrayList<>();
        features.add(unwantedFeature);
        features.add(wantedFeature);
        Mockito.doReturn(true).when(featureActivatorMock).getState(wantedFeatureCode);
        Mockito.doReturn(Optional.of(wantedFeature)).when(featureCollectionMock).get(wantedFeatureCode);
        // When
        DetailledFeatureDTO detailledFeatureDTO = cut.find(wantedFeatureCode);
        // Then
        Assertions.assertThat(detailledFeatureDTO).isNotNull();
        Assertions.assertThat(detailledFeatureDTO).isEqualTo(new DetailledFeatureDTO(wantedFeatureCode, true,
                wantedFeatureName, wantedFeatureDesc));
    }

    @Test(expected = NotFoundException.class)
    public void find_should_throw_not_found_if_code_doesnt_exists() throws NotFoundException {
        // Given
        String wantedFeatureCode = "not-existing";
        Mockito.doReturn(Optional.empty()).when(featureCollectionMock).get(wantedFeatureCode);
        // When
        cut.find(wantedFeatureCode);
        // Then
        // Test in annotation.
    }

    @Test
    public void retrieveStateOf_should_return_the_features_of_code() {
        // Given
        List<String> wantedCodes = new ArrayList<>();
        wantedCodes.add("my-feature");
        wantedCodes.add("test");
        wantedCodes.add("test-yep");
        Mockito.doReturn(Optional.of(given_a_feature("my-feature", "desc", "name"))).when(featureCollectionMock).get("my-feature");
        Mockito.doReturn(Optional.of(given_a_feature("test", "desc", "name"))).when(featureCollectionMock).get("test");
        Mockito.doReturn(Optional.of(given_a_feature("test-yep", "desc", "name"))).when(featureCollectionMock).get("test-yep");
        Mockito.doReturn(true).when(featureActivatorMock).getState("my-feature");
        Mockito.doReturn(false).when(featureActivatorMock).getState("test");
        Mockito.doReturn(true).when(featureActivatorMock).getState("test-yep");
        // When
        List<FeatureDTO> featuresDTO = cut.retrieveStateOf(wantedCodes);
        // Then
        Assertions.assertThat(featuresDTO).hasSize(3);
        Assertions.assertThat(featuresDTO.get(0)).isEqualTo(new FeatureDTO("my-feature", true));
        Assertions.assertThat(featuresDTO.get(1)).isEqualTo(new FeatureDTO("test", false));
        Assertions.assertThat(featuresDTO.get(2)).isEqualTo(new FeatureDTO("test-yep", true));
    }

    @Test
    public void retrieveStateOf_should_return_empty_list_on_empty_params() {
        // Given
        List<String> wantedCodes = new ArrayList<>();
        // When
        List<FeatureDTO> featuresDTO = cut.retrieveStateOf(wantedCodes);
        // Then
        Assertions.assertThat(featuresDTO).hasSize(0);
    }

    @Test
    public void retrieveStateOf_should_exclude_non_existing() {
        // Given
        List<String> wantedCodes = new ArrayList<>();
        wantedCodes.add("my-feature");
        wantedCodes.add("nope");
        wantedCodes.add("test-yep");
        Mockito.doReturn(Optional.of(given_a_feature("my-feature", "desc", "name"))).when(featureCollectionMock).get("my-feature");
        Mockito.doReturn(Optional.of(given_a_feature("test-yep", "desc", "name"))).when(featureCollectionMock).get("test-yep");
        Mockito.doReturn(true).when(featureActivatorMock).getState("my-feature");
        Mockito.doReturn(true).when(featureActivatorMock).getState("test-yep");
        // When
        List<FeatureDTO> featuresDTO = cut.retrieveStateOf(wantedCodes);
        // Then
        Assertions.assertThat(featuresDTO).hasSize(2);
        Assertions.assertThat(featuresDTO.get(0)).isEqualTo(new FeatureDTO("my-feature", true));
        Assertions.assertThat(featuresDTO.get(1)).isEqualTo(new FeatureDTO("test-yep", true));
    }

    @Test
    public void update_should_update_the_features() throws NotFoundException {
        // Given
        List<FeatureDTO> wantedUpdates = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        String feature3Code = "not-touched-one";
        wantedUpdates.add(new FeatureDTO(feature1Code, true));
        wantedUpdates.add(new FeatureDTO(feature2Code, true));
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);

        // When
        cut.update(wantedUpdates);

        // Then
        Mockito.verify(featureCollectionMock).get(feature1Code);
        Mockito.verify(featureCollectionMock).get(feature2Code);
        Mockito.verify(featureCollectionMock, Mockito.never()).get(feature3Code);
        Mockito.verify(featureActivatorMock).changeStateOf(feature1Code, true);
        Mockito.verify(featureActivatorMock).changeStateOf(feature2Code, true);
    }

    @Test(expected = NotFoundException.class)
    public void update_should_send_NotFound_if_one_not_exists() throws NotFoundException {
        // Given
        List<FeatureDTO> wantedUpdates = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        String feature3Code = "not-touched-one";
        wantedUpdates.add(new FeatureDTO(feature1Code, true));
        wantedUpdates.add(new FeatureDTO(feature2Code, true));
        wantedUpdates.add(new FeatureDTO(feature3Code, true));
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);
        Mockito.doReturn(Optional.empty()).when(featureCollectionMock).get(feature3Code);

        // When
        cut.update(wantedUpdates);
    }

    @Test
    public void update_should_not_update_all_if_one_not_exists() {
        // Given
        List<FeatureDTO> wantedUpdates = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        String feature3Code = "not-touched-one";
        wantedUpdates.add(new FeatureDTO(feature1Code, true));
        wantedUpdates.add(new FeatureDTO(feature2Code, true));
        wantedUpdates.add(new FeatureDTO(feature3Code, true));
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);
        Mockito.doReturn(Optional.empty()).when(featureCollectionMock).get(feature3Code);

        // When
        try {
            cut.update(wantedUpdates);
            Assertions.fail("a Not found exception was expected here ! ");
        } catch (NotFoundException ex) {
            // Normal behavior
        }

        // Then
        Mockito.verify(featureCollectionMock).get(feature1Code);
        Mockito.verify(featureCollectionMock).get(feature2Code);
        Mockito.verify(featureCollectionMock).get(feature3Code);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature1Code, true);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature2Code, true);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature3Code, true);
    }

    @Test
    public void reset_should_reset_features() throws NotFoundException {
        // Given
        List<String> wantedReset = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        wantedReset.add(feature1Code);
        wantedReset.add(feature2Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);
        Mockito.doReturn(false).when(featureActivatorMock).isEnabledByDefault(feature1Code);
        Mockito.doReturn(true).when(featureActivatorMock).isEnabledByDefault(feature2Code);

        // When
        cut.reset(wantedReset);

        // Then
        Mockito.verify(featureActivatorMock).isEnabledByDefault(feature1Code);
        Mockito.verify(featureActivatorMock).isEnabledByDefault(feature2Code);
        Mockito.verify(featureActivatorMock).changeStateOf(feature1Code, false);
        Mockito.verify(featureActivatorMock).changeStateOf(feature2Code, true);
    }

    @Test(expected = NotFoundException.class)
    public void reset_should_send_NotFound_if_one_not_exists() throws NotFoundException {
        // Given
        List<String> wantedReset = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        String feature3Code = "nope";
        wantedReset.add(feature1Code);
        wantedReset.add(feature2Code);
        wantedReset.add(feature3Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);
        Mockito.doReturn(Optional.empty()).when(featureCollectionMock).get(feature3Code);

        // When
        cut.reset(wantedReset);
    }

    @Test
    public void reset_should_not_reset_others_if_one_not_exists() {
        // Given
        List<String> wantedReset = new ArrayList<>();
        String feature1Code = "my-feature";
        String feature2Code = "second-feature";
        String feature3Code = "nope";
        wantedReset.add(feature1Code);
        wantedReset.add(feature2Code);
        wantedReset.add(feature3Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature1Code, "", ""))).when(featureCollectionMock).get(feature1Code);
        Mockito.doReturn(Optional.of(given_a_feature(feature2Code, "", ""))).when(featureCollectionMock).get(feature2Code);
        Mockito.doReturn(Optional.empty()).when(featureCollectionMock).get(feature3Code);

        // When
        try {
            cut.reset(wantedReset);
        } catch (NotFoundException ex) {
            // Do nothing. Normal behavior.
        }

        // Then
        Mockito.verify(featureActivatorMock, Mockito.never()).isEnabledByDefault(feature1Code);
        Mockito.verify(featureActivatorMock, Mockito.never()).isEnabledByDefault(feature2Code);
        Mockito.verify(featureActivatorMock, Mockito.never()).isEnabledByDefault(feature3Code);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature1Code, false);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature2Code, true);
        Mockito.verify(featureActivatorMock, Mockito.never()).changeStateOf(feature3Code, false);
    }

    private IFeature given_a_feature(String code, String desc, String name) {
        IFeature mock = Mockito.mock(IFeature.class);
        Mockito.doReturn(code).when(mock).getCode();
        Mockito.doReturn(desc).when(mock).getDescription();
        Mockito.doReturn(name).when(mock).getName();
        return mock;
    }

}
