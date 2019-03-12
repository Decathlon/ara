package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.enumeration.CommunicationType;
import com.decathlon.ara.repository.CommunicationRepository;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.CommunicationMapper;
import com.decathlon.ara.service.util.ObjectUtil;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommunicationService {

    @NonNull
    private final CommunicationRepository repository;

    @NonNull
    private final CommunicationMapper mapper;

    /**
     * Get all the entities.
     *
     * @param projectId the ID of the project in which to work
     * @return the list of entities ordered by code
     */
    @Transactional(readOnly = true)
    public List<CommunicationDTO> findAll(long projectId) {
        return mapper.toDto(repository.findAllByProjectIdOrderByCode(projectId));
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
        return mapper.toDto(communication);
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
        ObjectUtil.trimStringValues(dtoToUpdate);

        Communication databaseEntity = repository.findByProjectIdAndCode(projectId, dtoToUpdate.getCode());
        if (databaseEntity == null) {
            throw new NotFoundException(Messages.NOT_FOUND_COMMUNICATION, Entities.COMMUNICATION);
        }

        // Only the type and messages are modifiable by users: code and names are application-provided
        databaseEntity.setType(dtoToUpdate.getType());
        databaseEntity.setMessage(dtoToUpdate.getMessage());

        return mapper.toDto(repository.save(databaseEntity));
    }

    void initializeProject(Project project) {
        project.addCommunication(new Communication()
                .withProject(project)
                .withCode(Communication.EXECUTIONS)
                .withName("Executions: Top message")
                .withType(CommunicationType.TEXT)
                .withMessage(null));
        project.addCommunication(new Communication()
                .withProject(project)
                .withCode(Communication.SCENARIO_WRITING_HELPS)
                .withName("Scenario-Writing Helps")
                .withType(CommunicationType.HTML)
                .withMessage("Please configure information in Project List > MANAGE PROJECT > COMMUNICATIONS."));
        project.addCommunication(new Communication()
                .withProject(project)
                .withCode(Communication.HOW_TO_ADD_SCENARIO)
                .withName("Scenario-Writing Helps: Where to edit or add a scenario?")
                .withType(CommunicationType.HTML)
                .withMessage("Please configure information in Project List > MANAGE PROJECT > COMMUNICATIONS."));

    }

}
