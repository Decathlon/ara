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

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.enumeration.CommunicationType;
import com.decathlon.ara.repository.CommunicationRepository;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.GenericMapper;

@Service
@Transactional
public class CommunicationService {

    private final CommunicationRepository repository;

    private final GenericMapper mapper;

    public CommunicationService(CommunicationRepository repository, GenericMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities ordered by code
     */
    @Transactional(readOnly = true)
    public List<CommunicationDTO> findAll(long projectId) {
        return mapper.mapCollection(repository.findAllByProjectIdOrderByCode(projectId), CommunicationDTO.class);
    }

    /**
     * Get a specific entity by code.
     *
     * @param projectId the ID of the project in which to work
     * @param code      the code of the communication to search for
     * @return the found communication
     * @throws NotFoundException if the communication cannot be found
     */
    public CommunicationDTO findOneByCode(long projectId, String code) throws NotFoundException {
        Communication communication = repository.findByProjectIdAndCode(projectId, code);

        if (communication == null) {
            throw new NotFoundException(Messages.NOT_FOUND_COMMUNICATION, Entities.COMMUNICATION);
        }
        return mapper.map(communication, CommunicationDTO.class);
    }

    /**
     * Update type and message of a given communication (other properties are not modifiable).
     *
     * @param projectId   the ID of the project in which to work
     * @param dtoToUpdate communication to change type and message
     * @return the updated communication
     * @throws NotFoundException if the communication cannot be found
     */
    public CommunicationDTO update(long projectId, CommunicationDTO dtoToUpdate) throws NotFoundException {
        Communication databaseEntity = repository.findByProjectIdAndCode(projectId, dtoToUpdate.getCode());
        if (databaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_COMMUNICATION, Entities.COMMUNICATION);
        }

        // Only the type and messages are modifiable by users: code and names are application-provided
        databaseEntity.setType(dtoToUpdate.getType());
        databaseEntity.setMessage(dtoToUpdate.getMessage());

        return mapper.map(repository.save(databaseEntity), CommunicationDTO.class);
    }

    void initializeProject(Project project) {
        project.addCommunication(new Communication(project, Communication.EXECUTIONS, "Executions: Top message", CommunicationType.TEXT, null));
        project.addCommunication(new Communication(project, Communication.SCENARIO_WRITING_HELPS, "Scenario-Writing Helps", CommunicationType.HTML, "Please configure information in Project List > MANAGE PROJECT > COMMUNICATIONS."));
        project.addCommunication(new Communication(project, Communication.HOW_TO_ADD_SCENARIO, "Scenario-Writing Helps: Where to edit or add a scenario?", CommunicationType.HTML, "Please configure information in Project List > MANAGE PROJECT > COMMUNICATIONS."));

    }

}
