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

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.request.FunctionalityPosition;
import com.decathlon.ara.service.dto.request.MoveFunctionalitiesDTO;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.service.mapper.FunctionalityMapper;
import com.decathlon.ara.service.mapper.FunctionalityWithChildrenMapper;
import com.decathlon.ara.service.mapper.ScenarioMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FunctionalityServiceTest {

    @Mock
    private FunctionalityRepository repository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private ProjectService projectService;

    @Mock
    private FunctionalityMapper mapper;

    @Mock
    private FunctionalityWithChildrenMapper mapperWithChildren;

    @Mock
    private ScenarioMapper scenarioMapper;

    @InjectMocks
    private FunctionalityService functionalityService;

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenNoReferenceIdAndRelativePositionIsNotLastChild() throws BadRequestException {
        // Given

        Long projectId = 1L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(null);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = NotFoundException.class)
    public void create_throwNotFoundException_whenReferenceIdNotNullButNotFound() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(null);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenReferenceIdFoundAndPositionIsLastChildAndParentIdIsFunctionality() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.LAST_CHILD;

        Functionality referenceFunctionality = mock(Functionality.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getType()).thenReturn(FunctionalityType.FUNCTIONALITY);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityTypeDoesNotExist() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "unknownType";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityHasNoName() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(null);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = NotUniqueException.class)
    public void create_throwNotUniqueException_whenFunctionalityNameAlreadyExists() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long anotherFunctionalityId = 100L;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(anotherFunctionalityId);
        when(existingFunctionality.getType()).thenReturn(FunctionalityType.FUNCTIONALITY);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityIsAnInvalidFolder() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FOLDER";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        String folderComment = "some comment about this folder";

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getComment()).thenReturn(folderComment);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityHasNoTeam() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(null);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityHasNoSeverity() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(null);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityHasNoCountryCodes() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "severity";

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(null);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityHasStartedAndIsNotAutomatable() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "severity";
        String countryCodes = "countryCodes";

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(countryCodes);
        when(functionality.getStarted()).thenReturn(true);
        when(functionality.getNotAutomatable()).thenReturn(true);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = NotFoundException.class)
    public void create_throwNotFoundException_whenACountryCodeNotFound() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "severity";
        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        String unknownCode = "unknownCode";
        String countryCodes = code1 + "," + code2 + "," + unknownCode;

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(countryCodes);
        when(functionality.getStarted()).thenReturn(true);
        when(functionality.getNotAutomatable()).thenReturn(false);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        when(countryRepository.findCodesByProjectId(projectId)).thenReturn(Arrays.asList(code1, code2, code3));

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalityTeamIsNotAssignable() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "severity";
        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        String countryCodes = code1 + "," + code2 + "," + code3;

        TeamDTO teamDTO = mock(TeamDTO.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(countryCodes);
        when(functionality.getStarted()).thenReturn(true);
        when(functionality.getNotAutomatable()).thenReturn(false);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        when(countryRepository.findCodesByProjectId(projectId)).thenReturn(Arrays.asList(code1, code2, code3));

        when(teamService.findOne(projectId, teamId)).thenReturn(teamDTO);
        when(teamDTO.isAssignableToFunctionalities()).thenReturn(false);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void create_throwBadRequestException_whenFunctionalitySeverityDoesNotExist() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "severity";
        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        String countryCodes = code1 + "," + code2 + "," + code3;

        TeamDTO teamDTO = mock(TeamDTO.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(countryCodes);
        when(functionality.getStarted()).thenReturn(true);
        when(functionality.getNotAutomatable()).thenReturn(false);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        when(countryRepository.findCodesByProjectId(projectId)).thenReturn(Arrays.asList(code1, code2, code3));

        when(teamService.findOne(projectId, teamId)).thenReturn(teamDTO);
        when(teamDTO.isAssignableToFunctionalities()).thenReturn(true);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository, never()).save(any(Functionality.class));
    }

    @Test
    public void create_saveFunctionality_whenNoError() throws BadRequestException {
        // Given

        Long projectId = 1L;

        Long referenceId = 10L;

        FunctionalityDTO functionality = mock(FunctionalityDTO.class);
        String functionalityType = "FUNCTIONALITY";
        NewFunctionalityDTO newFunctionalityDTO = mock(NewFunctionalityDTO.class);
        FunctionalityPosition position = FunctionalityPosition.BELOW;

        Functionality referenceFunctionality = mock(Functionality.class);

        Long parentId = 11L;

        Functionality functionalitySibling1 = mock(Functionality.class);
        Functionality functionalitySibling2 = mock(Functionality.class);
        Functionality functionalitySibling3 = mock(Functionality.class);

        Long siblingId2 = 20L;
        Long siblingId3 = 30L;

        String functionalityName = "functionalityName";

        Functionality existingFunctionality = mock(Functionality.class);

        Long teamId = 2L;
        String severity = "MEDIUM";
        String code1 = "code1";
        String code2 = "code2";
        String code3 = "code3";
        String countryCodes = code1 + "," + code2 + "," + code3;

        TeamDTO teamDTO = mock(TeamDTO.class);

        Functionality mappedFunctionality = mock(Functionality.class);

        // When
        when(newFunctionalityDTO.getFunctionality()).thenReturn(functionality);
        when(newFunctionalityDTO.getReferenceId()).thenReturn(referenceId);
        when(newFunctionalityDTO.getRelativePosition()).thenReturn(position);

        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(referenceFunctionality.getId()).thenReturn(referenceId);
        when(referenceFunctionality.getParentId()).thenReturn(parentId);

        when(repository.findAllByProjectIdAndParentIdOrderByOrder(projectId, parentId)).thenReturn(Arrays.asList(
                functionalitySibling2,
                functionalitySibling3,
                functionalitySibling1
        ));
        when(functionalitySibling1.getId()).thenReturn(referenceId);
        when(functionalitySibling2.getId()).thenReturn(siblingId2);
        when(functionalitySibling3.getId()).thenReturn(siblingId3);

        when(functionality.getType()).thenReturn(functionalityType);
        when(functionality.getName()).thenReturn(functionalityName);
        when(functionality.getParentId()).thenReturn(parentId);
        when(functionality.getId()).thenReturn(referenceId);
        when(functionality.getTeamId()).thenReturn(teamId);
        when(functionality.getSeverity()).thenReturn(severity);
        when(functionality.getCountryCodes()).thenReturn(countryCodes);
        when(functionality.getStarted()).thenReturn(true);
        when(functionality.getNotAutomatable()).thenReturn(false);

        when(repository.findByProjectIdAndNameAndParentId(projectId, functionalityName, parentId)).thenReturn(existingFunctionality);
        when(existingFunctionality.getId()).thenReturn(referenceId);

        when(countryRepository.findCodesByProjectId(projectId)).thenReturn(Arrays.asList(code1, code2, code3));

        when(teamService.findOne(projectId, teamId)).thenReturn(teamDTO);
        when(teamDTO.isAssignableToFunctionalities()).thenReturn(true);

        when(mapper.toEntity(functionality)).thenReturn(mappedFunctionality);

        // Then
        functionalityService.create(projectId, newFunctionalityDTO);
        verify(repository).save(any(Functionality.class));
    }

    @Test(expected = BadRequestException.class)
    public void moveList_throwBadRequestException_whenMoveRequestIsNull() throws BadRequestException {
        // Given
        Long projectId = 1L;

        // When

        // Then
        functionalityService.moveList(projectId, null);
    }

    @Test(expected = BadRequestException.class)
    public void moveList_throwBadRequestException_whenReferenceIdIsNull() throws BadRequestException {
        // Given
        Long projectId = 1L;

        MoveFunctionalitiesDTO moveDetails = mock(MoveFunctionalitiesDTO.class);

        // When
        when(moveDetails.getReferenceId()).thenReturn(null);

        // Then
        functionalityService.moveList(projectId, moveDetails);
    }

    @Test(expected = BadRequestException.class)
    public void moveList_throwBadRequestException_whenSourceIdsIsNull() throws BadRequestException {
        // Given
        Long projectId = 1L;

        MoveFunctionalitiesDTO moveDetails = mock(MoveFunctionalitiesDTO.class);
        Long referenceId = 100L;

        // When
        when(moveDetails.getReferenceId()).thenReturn(referenceId);
        when(moveDetails.getSourceIds()).thenReturn(null);

        // Then
        functionalityService.moveList(projectId, moveDetails);
    }

    @Test(expected = BadRequestException.class)
    public void moveList_throwBadRequestException_whenSourceIdsIsEmpty() throws BadRequestException {
        // Given
        Long projectId = 1L;

        MoveFunctionalitiesDTO moveDetails = mock(MoveFunctionalitiesDTO.class);
        Long referenceId = 100L;

        // When
        when(moveDetails.getReferenceId()).thenReturn(referenceId);
        when(moveDetails.getSourceIds()).thenReturn(new ArrayList<>());

        // Then
        functionalityService.moveList(projectId, moveDetails);
    }

    @Test(expected = NotFoundException.class)
    public void moveList_throwNotFoundException_whenReferenceFunctionalityIsNotFound() throws BadRequestException {
        // Given
        Long projectId = 1L;

        MoveFunctionalitiesDTO moveDetails = mock(MoveFunctionalitiesDTO.class);

        Long referenceId = 100L;

        Long sourceId1 = 11L;
        Long sourceId2 = 12L;
        Long sourceId3 = 13L;

        // When
        when(moveDetails.getReferenceId()).thenReturn(referenceId);
        when(moveDetails.getSourceIds()).thenReturn(Arrays.asList(sourceId1, sourceId2, sourceId3));
        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(null);

        // Then
        functionalityService.moveList(projectId, moveDetails);
    }

    @Test(expected = NotFoundException.class)
    public void moveList_throwNotFoundException_whenSomeOfTheSourceFunctionalitiesAreNotFound() throws BadRequestException {
        // Given
        Long projectId = 1L;

        MoveFunctionalitiesDTO moveDetails = mock(MoveFunctionalitiesDTO.class);

        Long referenceId = 100L;
        Functionality referenceFunctionality = mock(Functionality.class);

        Long sourceId1 = 11L;
        Long sourceId2 = 12L;
        Long sourceId3 = 13L;
        Functionality sourceFunctionality1 = mock(Functionality.class);
        Functionality sourceFunctionality2 = mock(Functionality.class);

        // When
        when(moveDetails.getReferenceId()).thenReturn(referenceId);
        when(moveDetails.getSourceIds()).thenReturn(Arrays.asList(sourceId1, sourceId2, sourceId3));
        when(repository.findByProjectIdAndId(projectId, referenceId)).thenReturn(referenceFunctionality);
        when(repository.findByProjectIdAndIdIn(projectId, Arrays.asList(sourceId1, sourceId2, sourceId3))).thenReturn(
                Arrays.asList(sourceFunctionality1, sourceFunctionality2)
        );

        // Then
        functionalityService.moveList(projectId, moveDetails);
    }
}
