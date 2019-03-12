package com.decathlon.ara.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.decathlon.ara.Entities;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import com.decathlon.ara.service.CommunicationService;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@RestController
@RequestMapping(CommunicationResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommunicationResource {

    private static final String NAME = Entities.COMMUNICATION;
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";

    @NonNull
    private final CommunicationService service;

    @NonNull
    private final ProjectService projectService;

    /**
     * GET all entities.
     *
     * @param projectCode the code of the project in which to work
     * @return the ResponseEntity with status 200 (OK) and the list of entities in body
     */
    @GetMapping("")
    @Timed
    public ResponseEntity<List<CommunicationDTO>> getAll(@PathVariable String projectCode) {
        try {
            return ResponseEntity.ok().body(service.findAll(projectService.toId(projectCode)));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping("/{code}")
    @Timed
    public ResponseEntity<CommunicationDTO> getOneByCode(@PathVariable String projectCode, @PathVariable String code) {
        try {
            return ResponseEntity.ok().body(service.findOneByCode(projectService.toId(projectCode), code));
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

    @PutMapping("/{code}")
    @Timed
    public ResponseEntity<CommunicationDTO> update(@PathVariable String projectCode,
                                                   @PathVariable String code,
                                                   @Valid @RequestBody CommunicationDTO dtoToUpdate) {
        dtoToUpdate.setCode(code);
        try {
            CommunicationDTO updatedDto = service.update(projectService.toId(projectCode), dtoToUpdate);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.entityUpdated(Entities.COMMUNICATION, updatedDto.getCode()))
                    .body(updatedDto);
        } catch (NotFoundException e) {
            return ResponseUtil.handle(e);
        }
    }

}
