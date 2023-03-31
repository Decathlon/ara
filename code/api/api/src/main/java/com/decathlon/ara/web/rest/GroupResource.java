package com.decathlon.ara.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.service.GroupService;
import com.decathlon.ara.service.dto.group.GroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.decathlon.ara.web.rest.util.RestConstants;

@RestController
@RequestMapping(GroupResource.PATH)
public class GroupResource {

    static final String PATH = RestConstants.API_PATH + "/groups";

    private GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupDTO> getAll() {
        return groupService.findAll();
    }

    @GetMapping("/{groupName}")
    public GroupDTO get(@PathVariable String groupName) throws NotFoundException {
        return groupService.findOne(groupName);
    }

    @PostMapping
    public ResponseEntity<GroupDTO> create(@Valid @RequestBody GroupDTO groupDto) throws NotUniqueException {
        GroupDTO group = groupService.create(groupDto);
        return ResponseEntity.created(HeaderUtil.uri(PATH + "/" + group.getName())).body(group);
    }

    @DeleteMapping("/{groupName}")
    public void delete(@PathVariable String groupName) throws BadRequestException {
        groupService.delete(groupName);
    }
}
