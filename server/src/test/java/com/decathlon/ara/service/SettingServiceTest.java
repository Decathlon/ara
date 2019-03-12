package com.decathlon.ara.service;

import com.decathlon.ara.domain.Setting;
import com.decathlon.ara.ci.fetcher.FileSystemFetcher;
import com.decathlon.ara.repository.SettingRepository;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.dto.setting.SettingGroupDTO;
import com.decathlon.ara.service.dto.setting.SettingOptionDTO;
import com.decathlon.ara.service.dto.setting.SettingType;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettingServiceTest {

    private static final long A_PROJECT_ID = 42;

    @Mock
    private SettingRepository repository;

    @Mock
    private SettingProviderService settingProviderService;

    @Spy
    @InjectMocks
    private SettingService cut;

    @Test
    public void getTree_ShouldReturnSettingDefinitionsAndValues_WhenCalledForAProject() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        doReturn(values).when(cut).getValues(A_PROJECT_ID);
        final List<SettingGroupDTO> definitions = Collections.singletonList(new SettingGroupDTO());
        when(settingProviderService.getDefinitions(eq(A_PROJECT_ID), same(values))).thenReturn(definitions);
        doNothing().when(cut).populateValues(same(definitions), same(values));

        // WHEN
        final List<SettingGroupDTO> tree = cut.getTree(A_PROJECT_ID);

        // THEN
        assertThat(tree).isSameAs(definitions);
        verify(cut, times(1)).populateValues(same(definitions), same(values));
    }

    @Test
    public void get_ShouldReturnValue_WhenValuePresentInDatabase() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        values.put("code", "value");
        doReturn(values).when(cut).getValues(A_PROJECT_ID);

        // WHEN
        final String value = cut.get(A_PROJECT_ID, "code");

        // THEN
        assertThat(value).isEqualTo("value");
    }

    @Test
    public void get_ShouldReturnDefaultValue_WhenValueIsEmptyInDatabase() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        values.put("code", "");
        doReturn(values).when(cut).getValues(A_PROJECT_ID);
        when(settingProviderService.getDefinitions(eq(A_PROJECT_ID), same(values))).thenReturn(
                Collections.singletonList(new SettingGroupDTO()
                        .withSettings(Collections.singletonList(
                                new SettingDTO()
                                        .withCode("code")
                                        .withDefaultValue("default-value")))));

        // WHEN
        final String value = cut.get(A_PROJECT_ID, "code");

        // THEN
        assertThat(value).isEqualTo("default-value");
    }

    @Test
    public void get_ShouldReturnDefaultValue_WhenValueIsAbsentInDatabase() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        doReturn(values).when(cut).getValues(A_PROJECT_ID);
        when(settingProviderService.getDefinitions(eq(A_PROJECT_ID), same(values))).thenReturn(
                Collections.singletonList(new SettingGroupDTO()
                        .withSettings(Collections.singletonList(
                                new SettingDTO()
                                        .withCode("code")
                                        .withDefaultValue("default-value")))));

        // WHEN
        final String value = cut.get(A_PROJECT_ID, "code");

        // THEN
        assertThat(value).isEqualTo("default-value");
    }

    @Test
    public void update_ShouldFailAsNotFound_WhenSettingCodeIsUnknown() {
        // GIVEN
        doReturn(Optional.empty()).when(cut).getSettingDefinition(A_PROJECT_ID, "code");

        // WHEN
        try {
            cut.update(A_PROJECT_ID, "code", "any");

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("This setting is not allowed for this project.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("not_found");
        }
    }

    @Test(expected = BadRequestException.class)
    public void update_ShouldFail_WhenCalledWithAKnownSettingButWithAValueNotPassingValidation() throws BadRequestException {
        // GIVEN
        SettingDTO settingDefinition = new SettingDTO();
        doReturn(Optional.of(settingDefinition)).when(cut).getSettingDefinition(A_PROJECT_ID, "code");
        doThrow(new BadRequestException("any", "any", "any"))
                .when(cut).validateNewValue(eq("value"), same(settingDefinition));

        // WHEN
        cut.update(A_PROJECT_ID, "code", "value");
    }

    @Test
    public void update_ShouldUpdateSetting_WhenSettingExistingInDatabase() throws BadRequestException {
        // GIVEN
        SettingDTO settingDefinition = new SettingDTO();
        doReturn(Optional.of(settingDefinition)).when(cut).getSettingDefinition(A_PROJECT_ID, "code");
        doNothing().when(cut).validateNewValue(eq("value"), same(settingDefinition));
        Setting setting = new Setting();
        when(repository.findByProjectIdAndCode(A_PROJECT_ID, "code")).thenReturn(setting);
        doReturn(null).when(repository).save(same(setting));

        // WHEN
        cut.update(A_PROJECT_ID, "code", "value");

        // THEN
        verify(repository, times(1)).save(same(setting));
    }

    @Test
    public void update_ShouldInsertSetting_WhenSettingNonexistentInDatabase() throws BadRequestException {
        // GIVEN
        SettingDTO settingDefinition = new SettingDTO();
        doReturn(Optional.of(settingDefinition)).when(cut).getSettingDefinition(A_PROJECT_ID, "code");
        doNothing().when(cut).validateNewValue(eq("value"), same(settingDefinition));
        when(repository.findByProjectIdAndCode(A_PROJECT_ID, "code")).thenReturn(null);
        ArgumentCaptor<Setting> argument = ArgumentCaptor.forClass(Setting.class);
        doReturn(null).when(repository).save(argument.capture());

        // WHEN
        cut.update(A_PROJECT_ID, "code", "value");

        // THEN
        assertThat(argument.getValue().getProjectId()).isEqualTo(A_PROJECT_ID);
        assertThat(argument.getValue().getCode()).isEqualTo("code");
        assertThat(argument.getValue().getValue()).isEqualTo("value");
    }

    @Test
    public void update_ShouldPutNewValueInCache_WhenUpsertFinished() throws BadRequestException {
        // GIVEN
        doReturn(Optional.of(new SettingDTO())).when(cut).getSettingDefinition(A_PROJECT_ID, "code");
        doNothing().when(cut).validateNewValue(any(), any());
        when(repository.findByProjectIdAndCode(A_PROJECT_ID, "code")).thenReturn(null);
        ArgumentCaptor<Setting> argument = ArgumentCaptor.forClass(Setting.class);
        doReturn(null).when(repository).save(argument.capture());
        Map<String, String> cache = new HashMap<>();
        doReturn(cache).when(cut).getValues(A_PROJECT_ID);

        // WHEN
        cut.update(A_PROJECT_ID, "code", "value");

        // THEN
        assertThat(cache.get("code")).isEqualTo("value");
    }

    @Test
    public void getValues_ShouldReturnValuesFromRepository_WhenCalledForAProject() {
        // GIVEN
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("key", "value");
        when(repository.getProjectSettings(A_PROJECT_ID)).thenReturn(expectedValues);

        // WHEN
        final Map<String, String> actualValues = cut.getValues(A_PROJECT_ID);

        // THEN
        assertThat(actualValues).containsOnlyKeys("key");
    }

    @Test
    public void getValues_ShouldCallRepositoryOnlyOnce_WhenCalledForAProject() {
        // GIVEN
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("key", "value");
        when(repository.getProjectSettings(A_PROJECT_ID)).thenReturn(expectedValues);

        // WHEN
        cut.getValues(A_PROJECT_ID);
        final Map<String, String> actualValues = cut.getValues(A_PROJECT_ID);

        // THEN
        assertThat(actualValues).containsOnlyKeys("key");
        verify(repository, times(1)).getProjectSettings(A_PROJECT_ID);
    }

    @Test
    public void validateNewValue_ShouldPass_WhenEmptyValueButNotRequiredSetting() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(false)
                .withType(SettingType.PASSWORD)
                .withValidate(value -> "should-not-happen");

        // WHEN
        cut.validateNewValue("", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldFailAsBadRequest_WhenRequiredSettingIsEmpty() {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.STRING)
                .withDefaultValue(null);

        // WHEN
        try {
            cut.validateNewValue("", setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("This setting is required and has no default value.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("required");
        }
    }

    @Test
    public void validateNewValue_ShouldPass_WhenRequiredSettingIsEmptyButHasDefaultValue() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.STRING)
                .withDefaultValue("default");

        // WHEN
        cut.validateNewValue("", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldPass_WhenValueIsInAllowedSelectOptions() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.SELECT)
                .withOptions(Collections.singletonList(
                        new SettingOptionDTO("value", "label")));

        // WHEN
        cut.validateNewValue("value", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldFailAsNotFound_WhenValueIsNotInAllowedSelectOptions() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.SELECT)
                .withOptions(Collections.singletonList(
                        new SettingOptionDTO("value", "label")));

        // WHEN
        try {
            cut.validateNewValue("not-allowed-value", setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("The option is not supported by this setting.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("not_found");
        }
    }

    @Test
    public void validateNewValue_ShouldPass_WhenValueIsTrueForBooleanOptions() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.BOOLEAN);

        // WHEN
        cut.validateNewValue("true", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldPass_WhenValueIsFalseForBooleanOptions() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.BOOLEAN);

        // WHEN
        cut.validateNewValue("false", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldFailAsNotFound_WhenValueIsNotTrueNorFalseForBooleanOptions() {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.BOOLEAN);

        // WHEN
        try {
            cut.validateNewValue("not-a-boolean", setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("This setting must be a boolean.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("validation");
        }
    }

    @Test
    public void validateNewValue_ShouldPass_WhenValueIsAnIntegerForIntOptions() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.INT);

        // WHEN
        cut.validateNewValue("42", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldFailAsBadRequest_WhenValueIsAlphabeticForIntOptions() {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.INT);

        // WHEN
        try {
            cut.validateNewValue("not-an-integer", setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("This setting must be an integer number.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("validation");
        }
    }

    @Test
    public void validateNewValue_ShouldFailAsBadRequest_WhenValueIsOutsideIntegerRangeForIntOptions() {
        // GIVEN
        final String maxIntegerPlusOne = "2147483648";
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.INT);

        // WHEN
        try {
            cut.validateNewValue(maxIntegerPlusOne, setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("This setting must be an integer number.");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("validation");
        }
    }

    @Test
    public void validateNewValue_ShouldPass_WhenCustomValidatorReturnsNull() throws BadRequestException {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.TEXTAREA)
                .withValidate(value -> "value".equals(value) ? null : "should-not-happen");

        // WHEN
        cut.validateNewValue("value", setting);

        // THEN
        // No exception is thrown
    }

    @Test
    public void validateNewValue_ShouldFailAsBadRequest_WhenCustomValidatorReturnsAnErrorMessageOptions() {
        // GIVEN
        final SettingDTO setting = new SettingDTO()
                .withRequired(true)
                .withType(SettingType.STRING)
                .withValidate(value -> "value".equals(value) ? "custom-error" : "should-not-happen");

        // WHEN
        try {
            cut.validateNewValue("value", setting);

            // THEN
            fail("Should have thrown an exception");
        } catch (BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("custom-error");
            assertThat(e.getResourceName()).isEqualTo("setting");
            assertThat(e.getErrorKey()).isEqualTo("validation");
        }
    }

    @Test
    public void getSettingDefinition_ShouldReturnSetting_WhenAskedForAnExistingSetting() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        doReturn(values).when(cut).getValues(A_PROJECT_ID);
        final SettingDTO setting = new SettingDTO().withCode("code");
        when(settingProviderService.getDefinitions(eq(A_PROJECT_ID), same(values))).thenReturn(
                Collections.singletonList(new SettingGroupDTO()
                        .withSettings(Arrays.asList(
                                new SettingDTO().withCode("anotherCode"),
                                setting))));

        // WHEN
        final Optional<SettingDTO> foundSetting = cut.getSettingDefinition(A_PROJECT_ID, "code");

        // THEN
        assertThat(foundSetting.orElse(null)).isSameAs(setting);
    }

    @Test
    public void getSettingDefinition_ShouldReturnEmpty_WhenAskedForAnNonexistentSetting() {
        // GIVEN
        Map<String, String> values = new HashMap<>();
        doReturn(values).when(cut).getValues(A_PROJECT_ID);
        final SettingDTO setting = new SettingDTO().withCode("code");
        when(settingProviderService.getDefinitions(eq(A_PROJECT_ID), same(values))).thenReturn(
                Collections.singletonList(new SettingGroupDTO()
                        .withSettings(Collections.singletonList(setting))));

        // WHEN
        final Optional<SettingDTO> foundSetting = cut.getSettingDefinition(A_PROJECT_ID, "nonexistent");

        // THEN
        assertThat(foundSetting).isEmpty();
    }

    @Test
    public void populateValues_ShouldSetValuesForProvidedSettings_WhenProvidedWithSomeValues() {
        // GIVEN
        final List<SettingGroupDTO> groups = Arrays.asList(
                new SettingGroupDTO()
                        .withSettings(Arrays.asList(
                                new SettingDTO().withCode("code1"),
                                new SettingDTO().withCode("code2"))),
                new SettingGroupDTO()
                        .withSettings(Arrays.asList(
                                new SettingDTO().withCode("code3"),
                                new SettingDTO().withCode("code4"))));
        final Map<String, String> values = new HashMap<>();
        values.put("code1", "value1");
        values.put("code2", "value2");
        values.put("code3", "value3");
        values.put("another", "value");

        // WHEN
        cut.populateValues(groups, values);

        // THEN
        assertThat(groups.get(0).getSettings().get(0).getValue()).isEqualTo("value1");
        assertThat(groups.get(0).getSettings().get(1).getValue()).isEqualTo("value2");
        assertThat(groups.get(1).getSettings().get(0).getValue()).isEqualTo("value3");
        assertThat(groups.get(1).getSettings().get(1).getValue()).isNull();
    }

    @Test
    public void populateValues_ShouldSetDefaultValues_WhenValuesNotProvided() {
        // GIVEN
        final List<SettingGroupDTO> groups = Collections.singletonList(
                new SettingGroupDTO()
                        .withSettings(Collections.singletonList(
                                new SettingDTO()
                                        .withCode("code1")
                                        .withDefaultValue("defaultValue"))));
        final Map<String, String> values = new HashMap<>();
        values.put("another", "value");

        // WHEN
        cut.populateValues(groups, values);

        // THEN
        assertThat(groups.get(0).getSettings().get(0).getValue()).isEqualTo("defaultValue");
    }

    @Test
    public void populateValues_ShouldHideValue_WhenTypeIsPasswordWithSomeValue() {
        // GIVEN
        final List<SettingGroupDTO> groups = Collections.singletonList(
                new SettingGroupDTO()
                        .withSettings(Collections.singletonList(
                                new SettingDTO()
                                        .withCode("code1")
                                        .withType(SettingType.PASSWORD)
                                        .withValue("initialValue"))));
        final Map<String, String> values = new HashMap<>();
        values.put("code1", "secretPassword");

        // WHEN
        cut.populateValues(groups, values);

        // THEN
        assertThat(groups.get(0).getSettings().get(0).getValue()).isEqualTo("\u2022\u2022\u2022\u2022\u2022");
    }

    @Test
    public void populateValues_ShouldSetNullValue_WhenTypeIsPasswordWithoutAnyValue() {
        // GIVEN
        final List<SettingGroupDTO> groups = Collections.singletonList(
                new SettingGroupDTO()
                        .withSettings(Collections.singletonList(
                                new SettingDTO()
                                        .withCode("code1")
                                        .withType(SettingType.PASSWORD)
                                        .withValue("initialValue"))));
        final Map<String, String> values = new HashMap<>();
        values.put("code1", null);

        // WHEN
        cut.populateValues(groups, values);

        // THEN
        assertThat(groups.get(0).getSettings().get(0).getValue()).isNull(); // Not stars!
    }

    @Test
    public void useFileSystemIndexer_shouldReturnTrue_WhenProjectUseFSIndexer() {
        // GIVEN
        doReturn(FileSystemFetcher.FILESYSTEM).when(this.cut).get(A_PROJECT_ID, Settings.EXECUTION_INDEXER);

        // WHEN
        boolean result = this.cut.useFileSystemIndexer(A_PROJECT_ID);

        // THEN
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void useFileSystemIndexer_shouldReturnFalse_WhenProjectDoNotUseFSIndexer() {
        // GIVEN
        doReturn("not-a-filesystem-indexer").when(this.cut).get(A_PROJECT_ID, Settings.EXECUTION_INDEXER);

        // WHEN
        boolean result = this.cut.useFileSystemIndexer(A_PROJECT_ID);

        // THEN
        Assertions.assertThat(result).isFalse();
    }

}
