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

package com.decathlon.ara.scenario.generic.bean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class GenericExecutedScenarioReportTest {

    @Test
    void getFunctionalitiesName_returnEmptyString_whenCartographyEmptyAndNoName() {
        // Given
        GenericExecutedScenarioReport report = new GenericExecutedScenarioReport();

        // When

        // Then
        String functionalitiesName = report.getFunctionalitiesName();
        assertThat(functionalitiesName).isNotNull().isEmpty();
    }

    @Test
    void getFunctionalitiesName_returnName_whenCartographyEmptyAndNameGiven() {
        // Given
        String name = "My functionality name";

        GenericExecutedScenarioReport report = genericExecutedScenarioReport(name, null, null, null);

        // When

        // Then
        String functionalitiesName = report.getFunctionalitiesName();
        assertThat(functionalitiesName).isEqualTo(name);
    }

    @Test
    void getFunctionalitiesName_returnFunctionalities_whenCartographyNotEmptyAndNoName() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, List.of(12L, 38L, 71L), null);

        // When

        // Then
        String functionalitiesName = report.getFunctionalitiesName();
        assertThat(functionalitiesName).isEqualTo("Functionality 12, 38, 71");
    }

    @Test
    void getFunctionalitiesName_returnFunctionalitiesAndName_whenCartographyNotEmptyAndNameGiven() {
        // Given
        String name = "My functionality name";

        GenericExecutedScenarioReport report = genericExecutedScenarioReport(name, null, List.of(12L, 38L, 71L), null);

        // When

        // Then
        String functionalitiesName = report.getFunctionalitiesName();
        assertThat(functionalitiesName).isEqualTo("Functionality 12, 38, 71: " + name);
    }

    @Test
    void getTagsAsString_returnEmptyString_whenNoTags() {
        // Given
        GenericExecutedScenarioReport report = new GenericExecutedScenarioReport();

        // When

        // Then
        String tagsAsString = report.getTagsAsString();
        assertThat(tagsAsString).isNotNull().isEmpty();
    }

    @Test
    void getTagsAsString_returnTagsAsString_whenSingleTagGiven() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("tag"));

        // When

        // Then
        String tagsAsString = report.getTagsAsString();
        assertThat(tagsAsString).isEqualTo("@tag");
    }

    @Test
    void getTagsAsString_returnTagsAsString_whenSeveralTagsGiven() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("tag1", "tag2", "tag3"));

        // When

        // Then
        String tagsAsString = report.getTagsAsString();
        assertThat(tagsAsString).isEqualTo("@tag1 @tag2 @tag3");
    }

    @Test
    void getCountryCodes_returnEmptyList_whenNoTagsAndNoFeature() {
        // Given
        GenericExecutedScenarioReport report = new GenericExecutedScenarioReport();

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).isNotNull().isEmpty();
    }

    @Test
    void getCountryCodes_returnEmptyList_whenNoTagsAndNoFeatureTags() {
        // Given
        GenericExecutedScenarioFeature feature = new GenericExecutedScenarioFeature();
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, null);

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).isNotNull().isEmpty();
    }

    @Test
    void getCountryCodes_returnEmptyList_whenNoTagsAndNoFeatureCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("not-a-country-tag", "anything-but-a-country-tag"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, null);

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).isNotNull().isEmpty();
    }

    @Test
    void getCountryCodes_returnCountryCodes_whenNoTagsAndFeatureCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("country-fr", "country-de", "x", "another-tag"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, null);

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(2).containsExactlyInAnyOrder("fr", "de");
    }

    @Test
    void getCountryCodes_returnDistinctCountryCodes_whenNoTagsAndFeatureCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("country-fr", "country-de", "country-de", "country-fr", "country-nl"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, null);

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(3).containsExactlyInAnyOrder("fr", "de", "nl");
    }

    @Test
    void getCountryCodes_returnEmptyList_whenNoCountryCodeTagsFoundAndNoFeature() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("not-a-country-tag", "anything-but-a-country-tag"));

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).isNotNull().isEmpty();
    }

    @Test
    void getCountryCodes_returnCountryCodes_whenTagsCountryCodesFoundAndNoFeature() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("country-fr", "country-de", "x", "another-tag"));

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(2).containsExactlyInAnyOrder("fr", "de");
    }

    @Test
    void getCountryCodes_returnDistinctCountryCodes_whenTagsCountryCodesFoundAndNoFeature() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("country-fr", "country-de", "country-de", "country-fr", "country-nl"));

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(3).containsExactlyInAnyOrder("fr", "de", "nl");
    }

    @Test
    void getCountryCodes_returnCountryCodes_whenTagsAndFeatureCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("country-es", "country-it", "y", "some-tag", "country-be"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, List.of("country-fr", "country-de", "x", "another-tag"));

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(5).containsExactlyInAnyOrder("fr", "de", "es", "it", "be");
    }

    @Test
    void getCountryCodes_returnDistinctCountryCodes_whenTagsAndFeatureCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("country-es", "country-it", "y", "country-nl", "some-tag", "country-be"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, List.of("country-fr", "country-nl", "country-de", "x", "another-tag"));

        // When

        // Then
        List<String> countryCodes = report.getCountryCodes();
        assertThat(countryCodes).hasSize(6).containsExactlyInAnyOrder("fr", "de", "es", "it", "be", "nl");
    }

    @Test
    void getCountryCodesAsString_returnEmptyString_whenNoCountryCode() {
        // Given
        GenericExecutedScenarioReport report = new GenericExecutedScenarioReport();

        // When

        // Then
        String countryCodesAsString = report.getCountryCodesAsString();
        assertThat(countryCodesAsString).isEqualTo("all");
    }

    @Test
    void getCountryCodesAsString_returnSingleCountryCode_whenOneCountryCodeFound() {
        // Given
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, null, null, List.of("country-fr", "x"));

        // When

        // Then
        String countryCodesAsString = report.getCountryCodesAsString();
        assertThat(countryCodesAsString).isEqualTo("fr");
    }

    @Test
    void getCountryCodesAsString_returnCountryCodes_whenSeveralCountryCodesFound() {
        // Given
        GenericExecutedScenarioFeature feature = genericExecutedScenarioFeature(List.of("country-es", "country-it", "y", "country-nl", "some-tag", "country-be"));
        GenericExecutedScenarioReport report = genericExecutedScenarioReport(null, feature, null, List.of("country-fr", "country-nl", "country-de", "x", "another-tag"));

        // When

        // Then
        String countryCodesAsString = report.getCountryCodesAsString();
        assertThat(countryCodesAsString).isEqualTo("fr,nl,de,es,it,be");
    }

    private GenericExecutedScenarioReport genericExecutedScenarioReport(String name, GenericExecutedScenarioFeature feature, List<Long> cartography, List<String> tags) {
        GenericExecutedScenarioReport genericExecutedScenarioReport = new GenericExecutedScenarioReport();
        TestUtil.setField(genericExecutedScenarioReport, "name", name);
        TestUtil.setField(genericExecutedScenarioReport, "feature", feature);
        TestUtil.setField(genericExecutedScenarioReport, "cartography", cartography);
        TestUtil.setField(genericExecutedScenarioReport, "tags", tags);
        return genericExecutedScenarioReport;
    }

    private GenericExecutedScenarioFeature genericExecutedScenarioFeature(List<String> tags) {
        GenericExecutedScenarioFeature genericExecutedScenarioFeature = new GenericExecutedScenarioFeature();
        TestUtil.setField(genericExecutedScenarioFeature, "tags", tags);
        return genericExecutedScenarioFeature;
    }
}
