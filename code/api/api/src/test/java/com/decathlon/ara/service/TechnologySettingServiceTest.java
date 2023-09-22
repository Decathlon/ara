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

package com.decathlon.ara.service;

import com.decathlon.ara.domain.TechnologySetting;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.TechnologySettingRepository;
import com.decathlon.ara.scenario.cucumber.settings.CucumberSettings;
import com.decathlon.ara.scenario.postman.settings.PostmanSettings;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.dto.setting.SettingType;
import com.decathlon.ara.service.dto.setting.TechnologySettingGroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnologySettingServiceTest {

    @Mock
    private TechnologySettingRepository technologySettingRepository;

    @Mock
    private SettingService settingService;

    @InjectMocks
    private TechnologySettingService technologySettingService;

    @Test
    void getAllGroups_returnAllGroups() {
        // Given
        Long projectId = 1L;

        TechnologySetting technologySetting1 = mock(TechnologySetting.class);
        TechnologySetting technologySetting2 = mock(TechnologySetting.class);
        TechnologySetting technologySetting3 = mock(TechnologySetting.class);

        // When
        when(technologySettingRepository.findByProjectId(projectId)).thenReturn(
                Arrays.asList(technologySetting1, technologySetting2, technologySetting3)
        );
        when(technologySetting1.getCode()).thenReturn(CucumberSettings.REPORT_PATH.getCode());
        when(technologySetting1.getValue()).thenReturn("/new/cucumber/path");
        when(technologySetting2.getCode()).thenReturn(PostmanSettings.REPORTS_PATH.getCode());
        when(technologySetting2.getValue()).thenReturn("/updated/newmann/value");
        when(technologySetting3.getCode()).thenReturn("unknown-code");

        // Then
        List<TechnologySettingGroupDTO> technologySettingGroups = technologySettingService.getAllGroups(projectId);
        assertThat(technologySettingGroups).isNotEmpty();
        assertThat(technologySettingGroups)
                .extracting("technology")
                .contains(
                        Technology.CUCUMBER,
                        Technology.POSTMAN
                );

        TechnologySettingGroupDTO cucumberGroup = technologySettingGroups.stream()
                .filter(s -> Technology.CUCUMBER.equals(s.getTechnology()))
                .findFirst()
                .get();
        assertThat(cucumberGroup.getName()).isEqualTo("Cucumber");
        assertThat(cucumberGroup.getSettings())
                .hasSize(2)
                .extracting(
                        "code",
                        "name",
                        "type",
                        "required",
                        "help",
                        "defaultValue",
                        "value"
                )
                .containsOnly(
                        tuple(
                                "report.path",
                                "Cucumber report path",
                                SettingType.STRING,
                                true,
                                "Cucumber reports are extracted from this path. Eg. \"/report.json\", appended to the run's job folder.",
                                "/report.json",
                                "/new/cucumber/path"
                        ),
                        tuple(
                                "step.definitions.path",
                                "Cucumber step definitions path",
                                SettingType.STRING,
                                true,
                                "Cucumber step definitions are extracted from this path. Eg. \" /stepDefinitions.json\", appended to the run's job folder. If not provided, the cycle-definitions will not be downloaded",
                                "/stepDefinitions.json",
                                "/stepDefinitions.json"
                        )
                );

        TechnologySettingGroupDTO postmanGroup = technologySettingGroups.stream()
                .filter(s -> Technology.POSTMAN.equals(s.getTechnology()))
                .findFirst()
                .get();
        assertThat(postmanGroup.getName()).isEqualTo("Postman");
        assertThat(postmanGroup.getSettings())
                .hasSize(2)
                .extracting(
                        "code",
                        "name",
                        "type",
                        "required",
                        "help",
                        "defaultValue",
                        "value"
                )
                .containsOnly(
                        tuple(
                                "reports.path",
                                "Newman reports path",
                                SettingType.STRING,
                                true,
                                "Newman reports are extracted from this path. Eg. \"/reports\", appended to the run's jobUrl.",
                                "/reports",
                                "/updated/newmann/value"
                        ),
                        tuple(
                                "result.file.name",
                                "Newman result file name",
                                SettingType.STRING,
                                true,
                                "Newman result is extracted from this file (located in the reports folder). Eg. \"/result.txt\". It shows whether the scenarios were successful or not",
                                "result.txt",
                                "result.txt"
                        )
                );
    }

    @Test
    void getSettingValue_returnValue_whenFound() {
        // Given
        Long projectId = 1L;

        TechnologySetting savedSetting = mock(TechnologySetting.class);

        // When
        when(technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, CucumberSettings.REPORT_PATH.getCode(), Technology.CUCUMBER)).thenReturn(Optional.of(savedSetting));
        when(savedSetting.getValue()).thenReturn("/new/cucumber/path");

        // Then
        Optional<String> value = technologySettingService.getSettingValue(projectId, CucumberSettings.REPORT_PATH);
        assertThat(value).isPresent();
        assertThat(value).hasValue("/new/cucumber/path");
    }

    @Test
    void getSettingValue_returnDefaultValue_whenNotFound() {
        // Given
        Long projectId = 1L;

        TechnologySetting savedSetting = mock(TechnologySetting.class);

        // When
        when(technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, CucumberSettings.REPORT_PATH.getCode(), Technology.CUCUMBER)).thenReturn(Optional.empty());

        // Then
        Optional<String> value = technologySettingService.getSettingValue(projectId, CucumberSettings.REPORT_PATH);
        assertThat(value).isPresent();
        assertThat(value).hasValue(CucumberSettings.REPORT_PATH.getDefaultValue());
    }

    @Test
    void update_throwNotFoundException_whenCodeIsUnknown() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String unknownCode = "unknown.code";
        Technology technology = Technology.CUCUMBER;

        String newValue = "someValue";

        // When

        // Then
        verify(technologySettingRepository, never()).save(any(TechnologySetting.class));
        verify(settingService, never()).validateNewValue(anyString(), any(SettingDTO.class));
        assertThrows(NotFoundException.class, () -> technologySettingService.update(projectId, unknownCode, technology, newValue));
    }

    @Test
    void update_throwBadRequestException_whenValueIsInvalidated() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String code = "report.path";
        Technology technology = Technology.CUCUMBER;

        String incorrectValue = "someIncorrectValue";

        // When
        doThrow(new BadRequestException("something went bad", "resource", "error_key")).when(settingService).validateNewValue(anyString(), any(SettingDTO.class));

        // Then
        verify(technologySettingRepository, never()).save(any(TechnologySetting.class));
        assertThrows(BadRequestException.class, () -> technologySettingService.update(projectId, code, technology, incorrectValue));
    }

    @Test
    void update_insertNewSetting_whenSettingNotFound() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String code = "report.path";
        Technology technology = Technology.CUCUMBER;

        String newValue = "someValue";

        // When
        when(technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, code, technology)).thenReturn(Optional.empty());

        // Then
        technologySettingService.update(projectId, code, technology, newValue);

        ArgumentCaptor<String> newValueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SettingDTO> settingArgumentCaptor = ArgumentCaptor.forClass(SettingDTO.class);
        verify(settingService).validateNewValue(newValueArgumentCaptor.capture(), settingArgumentCaptor.capture());
        assertThat(newValueArgumentCaptor.getValue()).isEqualTo(newValue);
        assertThat(settingArgumentCaptor.getValue())
                .extracting(
                        "code",
                        "name",
                        "type",
                        "required",
                        "help",
                        "defaultValue",
                        "value"
                )
                .contains(
                        "report.path",
                        "Cucumber report path",
                        SettingType.STRING,
                        true,
                        "Cucumber reports are extracted from this path. Eg. \"/report.json\", appended to the run's job folder.",
                        "/report.json",
                        "/report.json"
                );

        ArgumentCaptor<TechnologySetting> technologySettingArgumentCaptor = ArgumentCaptor.forClass(TechnologySetting.class);
        verify(technologySettingRepository).save(technologySettingArgumentCaptor.capture());
        assertThat(technologySettingArgumentCaptor.getValue())
                .extracting(
                        "id",
                        "projectId",
                        "code",
                        "technology",
                        "value"
                )
                .contains(
                        null,
                        projectId,
                        code,
                        technology,
                        newValue
                );
    }

    @Test
    void update_updateSetting_whenSettingFound() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String code = "report.path";
        Technology technology = Technology.CUCUMBER;

        String newValue = "someValue";

        TechnologySetting savedSetting = mock(TechnologySetting.class);

        // When
        when(technologySettingRepository.findByProjectIdAndCodeAndTechnology(projectId, code, technology)).thenReturn(Optional.of(savedSetting));

        // Then
        technologySettingService.update(projectId, code, technology, newValue);

        ArgumentCaptor<String> newValueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SettingDTO> settingArgumentCaptor = ArgumentCaptor.forClass(SettingDTO.class);
        verify(settingService).validateNewValue(newValueArgumentCaptor.capture(), settingArgumentCaptor.capture());
        assertThat(newValueArgumentCaptor.getValue()).isEqualTo(newValue);
        assertThat(settingArgumentCaptor.getValue())
                .extracting(
                        "code",
                        "name",
                        "type",
                        "required",
                        "help",
                        "defaultValue",
                        "value"
                )
                .contains(
                        "report.path",
                        "Cucumber report path",
                        SettingType.STRING,
                        true,
                        "Cucumber reports are extracted from this path. Eg. \"/report.json\", appended to the run's job folder.",
                        "/report.json",
                        "/report.json"
                );

        verify(technologySettingRepository).save(savedSetting);
        verify(savedSetting).setValue(newValue);
    }
}

