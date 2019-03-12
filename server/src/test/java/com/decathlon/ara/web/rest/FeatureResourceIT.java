package com.decathlon.ara.web.rest;

import com.decathlon.ara.features.FeatureActivator;
import com.decathlon.ara.features.FeatureCollection;
import com.decathlon.ara.features.IFeature;
import com.decathlon.ara.service.dto.feature.DetailledFeatureDTO;
import com.decathlon.ara.service.dto.feature.FeatureDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TransactionalSpringIntegrationTest
public class FeatureResourceIT {

    @Autowired
    private FeatureResource cut;

    @MockBean
    private FeatureCollection mockCollection;

    @Autowired
    private FeatureActivator featureActivator;

    @Before
    public void setup() {
        // TODO Create fake Features classes.
        FakeFeature feature1 = new FakeFeature("my-feature", "My Feature", "Feature 1");
        FakeFeature feature2 = new FakeFeature("my-feature-in-test", "My Feature In Test", "Feature 2");
        FakeFeature feature3 = new FakeFeature("an-old-feature", "An Old Feature.", "An old flipping, to be removed");
        List<IFeature> features = new ArrayList<>();
        features.add(feature1);
        features.add(feature2);
        features.add(feature3);
        Mockito.doReturn(features).when(mockCollection).list();
        Mockito.doReturn(Optional.of(feature1)).when(mockCollection).get("my-feature");
        Mockito.doReturn(Optional.of(feature2)).when(mockCollection).get("my-feature-in-test");
        Mockito.doReturn(Optional.of(feature3)).when(mockCollection).get("an-old-feature");
        this.featureActivator.load(mockCollection);
    }

    @Test
    public void test_should_manipulate_the_feature_flipping() {
        FeatureDTO feature1 = new FeatureDTO("my-feature", false);
        FeatureDTO feature2 = new FeatureDTO("my-feature-in-test", true);
        FeatureDTO feature3 = new FeatureDTO("an-old-feature", true);

        // Get all features.
        ResponseEntity<List<FeatureDTO>> allFeaturesResponse = cut.list();
        assertThatIsOk(allFeaturesResponse);
        List<FeatureDTO> allFeatures = allFeaturesResponse.getBody();
        Assertions.assertThat(allFeatures).containsExactly(feature1, feature2, feature3);

        // Check Detail of a feature.
        DetailledFeatureDTO expectingDetailFeature3 = new DetailledFeatureDTO("an-old-feature", true,
                "An Old Feature.", "An old flipping, to be removed");
        ResponseEntity<DetailledFeatureDTO> detailledFeature3Response = cut.describe("an-old-feature");
        assertThatIsOk(detailledFeature3Response);
        Assertions.assertThat(detailledFeature3Response.getBody()).isEqualTo(expectingDetailFeature3);

        // Update an existing feature.
        ResponseEntity<DetailledFeatureDTO> updatedFeatureResponse = cut.update(feature2.getCode(), !feature2.isEnabled());
        assertThatIsOk(updatedFeatureResponse);
        DetailledFeatureDTO updatedFeature = updatedFeatureResponse.getBody();
        Assertions.assertThat(updatedFeature).isNotNull();
        Assertions.assertThat(updatedFeature.getCode()).isEqualTo(feature2.getCode());
        Assertions.assertThat(updatedFeature.isEnabled()).isEqualTo(!feature2.isEnabled());

        // Get the minimum state.
        ResponseEntity<FeatureDTO> stateFeatureResponse = cut.stateOf(feature2.getCode());
        assertThatIsOk(stateFeatureResponse);
        FeatureDTO stateFeature = stateFeatureResponse.getBody();
        Assertions.assertThat(stateFeature).isNotNull();
        Assertions.assertThat(stateFeature.getCode()).isEqualTo(feature2.getCode());
        Assertions.assertThat(stateFeature.isEnabled()).isEqualTo(!feature2.isEnabled());

        // Get the default status of a feature.
        ResponseEntity<FeatureDTO> defaultStatusResponse = cut.retrieveDefaultSetting(feature2.getCode());
        assertThatIsOk(defaultStatusResponse);
        FeatureDTO defaultStatus = defaultStatusResponse.getBody();
        Assertions.assertThat(defaultStatus).isNotNull();
        Assertions.assertThat(defaultStatus.getCode()).isEqualTo(feature2.getCode());
        Assertions.assertThat(defaultStatus.isEnabled()).isEqualTo(feature2.isEnabled());
        ResponseEntity<DetailledFeatureDTO> actualValue = cut.describe(feature2.getCode());
        Assertions.assertThat(actualValue.getBody()).isNotNull();
        Assertions.assertThat(actualValue.getBody().isEnabled()).isEqualTo(!feature2.isEnabled());

        // Reset it
        ResponseEntity<DetailledFeatureDTO> resettedFeatureResponse = cut.reset(feature2.getCode());
        assertThatIsOk(resettedFeatureResponse);
        DetailledFeatureDTO resettedFeature = resettedFeatureResponse.getBody();
        Assertions.assertThat(resettedFeature).isNotNull();
        Assertions.assertThat(resettedFeature.getCode()).isEqualTo(feature2.getCode());
        Assertions.assertThat(resettedFeature.isEnabled()).isEqualTo(feature2.isEnabled());

        // Update a list of features
        List<FeatureDTO> listToUpdate = new ArrayList<>();
        FeatureDTO newFeature1 = new FeatureDTO(feature1.getCode(), !feature1.isEnabled());
        FeatureDTO newFeature2 = new FeatureDTO(feature2.getCode(), !feature2.isEnabled());
        listToUpdate.add(newFeature1);
        listToUpdate.add(newFeature2);
        ResponseEntity<List<FeatureDTO>> listUpdatedResponse = cut.updateAll(listToUpdate);
        assertThatIsOk(listUpdatedResponse);
        List<FeatureDTO> listUpdated = listUpdatedResponse.getBody();
        Assertions.assertThat(listUpdated).isNotNull();
        Assertions.assertThat(listUpdated).containsExactly(newFeature1, newFeature2);

        // Reset them
        List<String> listToReset = new ArrayList<>();
        listToReset.add(newFeature1.getCode());
        listToReset.add(newFeature2.getCode());
        ResponseEntity<List<FeatureDTO>> listResettedResponse = cut.resetAll(listToReset);
        assertThatIsOk(listResettedResponse);
        List<FeatureDTO> listResetted = listResettedResponse.getBody();
        Assertions.assertThat(listResetted).isNotNull();
        Assertions.assertThat(listResetted).containsExactly(feature1, feature2);
    }

    @Test
    public void test_should_send_error_when_default_of_not_existing_one() {
        ResponseEntity<FeatureDTO> defaultSettingResponse = cut.retrieveDefaultSetting("nope");
        Assertions.assertThat(defaultSettingResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(defaultSettingResponse.getHeaders()).containsKey("X-ara-error");
        Assertions.assertThat(defaultSettingResponse.getHeaders().get("X-ara-error")).hasSize(1);
        Assertions.assertThat(defaultSettingResponse.getHeaders().get("X-ara-error").get(0)).isEqualTo("error.not_found");
        Assertions.assertThat(defaultSettingResponse.getHeaders()).containsKey("X-ara-message");
        Assertions.assertThat(defaultSettingResponse.getHeaders().get("X-ara-message")).hasSize(1);
        Assertions.assertThat(defaultSettingResponse.getHeaders().get("X-ara-message").get(0)).isEqualTo("The feature 'nope' doesn't exists.");
    }

    @Test
    public void test_should_not_update_feature_if_not_exists() {
        // Try to update a not existing feature
        ResponseEntity<DetailledFeatureDTO> failedUpdateResponse = cut.update("nope", false);
        Assertions.assertThat(failedUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(failedUpdateResponse.getHeaders()).containsKey("X-ara-error");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-error")).hasSize(1);
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-error").get(0)).isEqualTo("error.not_found");
        Assertions.assertThat(failedUpdateResponse.getHeaders()).containsKey("X-ara-message");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-message")).hasSize(1);
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-message").get(0)).isEqualTo("Unknown feature(s) 'nope'.");

        // Update all should do nothing if one feature doesn't exists in the list.
        List<FeatureDTO> listToUpdate = new ArrayList<>();
        FeatureDTO newFeature1 = new FeatureDTO("my-feature", true);
        FeatureDTO newFeature2 = new FeatureDTO("nope", false);
        listToUpdate.add(newFeature1);
        listToUpdate.add(newFeature2);
        ResponseEntity<List<FeatureDTO>> listUpdatedResponse = cut.updateAll(listToUpdate);
        Assertions.assertThat(listUpdatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(listUpdatedResponse.getHeaders()).containsKey("X-ara-error");
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-error")).hasSize(1);
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-error").get(0)).isEqualTo("error.not_found");
        Assertions.assertThat(listUpdatedResponse.getHeaders()).containsKey("X-ara-message");
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-message")).hasSize(1);
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-message").get(0)).isEqualTo("Unknown feature(s) 'nope'.");
        ResponseEntity<DetailledFeatureDTO> untouchedFeature = cut.describe("my-feature");
        Assertions.assertThat(untouchedFeature.getBody()).isNotNull();
        Assertions.assertThat(untouchedFeature.getBody().isEnabled()).isFalse();
    }

    @Test
    public void test_should_not_reset_feature_if_not_exists() {
        // Try to reset a not existing feature
        ResponseEntity<DetailledFeatureDTO> failedUpdateResponse = cut.reset("nope");
        Assertions.assertThat(failedUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(failedUpdateResponse.getHeaders()).containsKey("X-ara-error");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-error")).hasSize(1);
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-error").get(0)).isEqualTo("error.not_found");
        Assertions.assertThat(failedUpdateResponse.getHeaders()).containsKey("X-ara-message");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-message")).hasSize(1);
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-message").get(0)).isEqualTo("Unknown feature(s) 'nope'.");

        // Reset all should do nothing if one feature doesn't exists in the list.
        List<String> listToReset = new ArrayList<>();
        cut.update("my-feature", true);
        listToReset.add("my-feature");
        listToReset.add("nope");
        ResponseEntity<List<FeatureDTO>> listUpdatedResponse = cut.resetAll(listToReset);
        Assertions.assertThat(listUpdatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(listUpdatedResponse.getHeaders()).containsKey("X-ara-error");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-error")).hasSize(1);
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-error").get(0)).isEqualTo("error.not_found");
        Assertions.assertThat(listUpdatedResponse.getHeaders()).containsKey("X-ara-message");
        Assertions.assertThat(failedUpdateResponse.getHeaders().get("X-ara-message")).hasSize(1);
        Assertions.assertThat(listUpdatedResponse.getHeaders().get("X-ara-message").get(0)).isEqualTo("Unknown feature(s) 'nope'.");
        ResponseEntity<DetailledFeatureDTO> untouchedFeature = cut.describe("my-feature");
        Assertions.assertThat(untouchedFeature.getBody()).isNotNull();
        Assertions.assertThat(untouchedFeature.getBody().isEnabled()).isTrue();

    }

    private static void assertThatIsOk(ResponseEntity<?> entity) {
        Assertions.assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private static class FakeFeature implements IFeature {

        private String code;
        private String name;
        private String description;

        FakeFeature(String code, String name, String desc) {
            this.code = code;
            this.name = name;
            this.description = desc;
        }

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }
}
