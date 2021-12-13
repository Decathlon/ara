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

package com.decathlon.ara.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.mapper.GenericMapper;

@ExtendWith(MockitoExtension.class)
class SeverityServiceTest {

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private GenericMapper mapper;

    @InjectMocks
    private SeverityService cut;

    @Test
    void getSeveritiesWithAll_ShouldCallRepositoryFindAllByOrderByPosition_WhenCalled() {
        // GIVEN
        long aProjectId = 42;

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        verify(severityRepository, only()).findAllByProjectIdOrderByPosition(aProjectId);
    }

    @Test
    void getSeveritiesWithAll_ShouldAddTheAllSpecialSeverity_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<Severity> severities = new ArrayList<>();
        severities.add(new Severity());
        when(severityRepository.findAllByProjectIdOrderByPosition(aProjectId)).thenReturn(severities);

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        assertThat(severities.get(1)).isSameAs(Severity.ALL);
    }

    @Test
    void getSeveritiesWithAll_ShouldCallMapperWithResultOfRepositoryFindAllByOrderByPosition_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<Severity> severities = new ArrayList<>();
        when(severityRepository.findAllByProjectIdOrderByPosition(aProjectId)).thenReturn(severities);

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        verify(mapper, times(1)).mapCollection(severities, SeverityDTO.class);
    }

    @Test
    void getSeveritiesWithAll_ShouldReturnMapperResult_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<SeverityDTO> severityDTOs = Collections.singletonList(new SeverityDTO());
        when(mapper.mapCollection(anyCollection(), eq(SeverityDTO.class))).thenReturn(severityDTOs);

        // WHEN / THEN
        assertThat(cut.getSeveritiesWithAll(aProjectId)).isSameAs(severityDTOs);
    }

    @Test
    void getDefaultSeverityCode_ShouldReturnTheDefaultOne_WhenOneIsDefinedAsDefault() {
        // GIVEN
        List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO("a", null, null, null, null, false),
                new SeverityDTO("b", null, null, null, null, true));

        // WHEN
        final String defaultSeverityCode = cut.getDefaultSeverityCode(severities);

        // THEN
        assertThat(defaultSeverityCode).isEqualTo("b");
    }

    @Test
    void getDefaultSeverityCode_ShouldReturnNull_WhenNoSeverityIsDefinedAsDefault() {
        // GIVEN
        List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO("a", null, null, null, null, false),
                new SeverityDTO("b", null, null, null, null, false));

        // WHEN
        final String defaultSeverityCode = cut.getDefaultSeverityCode(severities);

        // THEN
        assertThat(defaultSeverityCode).isNull();
    }

}
