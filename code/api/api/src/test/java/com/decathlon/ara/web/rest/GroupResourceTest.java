package com.decathlon.ara.web.rest;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.decathlon.ara.service.GroupService;
import com.decathlon.ara.service.dto.group.GroupDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;

@ExtendWith(MockitoExtension.class)
class GroupResourceTest {
    
    @Mock
    private GroupService groupService;
    
    @InjectMocks
    private GroupResource groupResource;
    
    @Test
    void getAllShouldReturnServiceResult() {
        List<GroupDTO> serviceResult = List.of(new GroupDTO());
        Mockito.when(groupService.findAll()).thenReturn(serviceResult);
        List<GroupDTO> result = groupResource.getAll();
        Assertions.assertEquals(serviceResult, result);
    }
    
    @Test
    void getShouldThrowExceptionWhenServiceThrowAnException() throws NotFoundException {
        Mockito.when(groupService.findOne("groupName")).thenThrow(new NotFoundException(null, null));
        Assertions.assertThrows(NotFoundException.class, () -> groupResource.get("groupName"));
    }
    
    @Test
    void getShouldReturnServiceResultWhenServiceSucceed() throws NotFoundException {
        GroupDTO groupDTO = new GroupDTO();
        Mockito.when(groupService.findOne("groupName")).thenReturn(groupDTO);
        GroupDTO result = groupResource.get("groupName");
        Assertions.assertEquals(groupDTO, result);
    }
    
    @Test
    void createShouldThrowExceptionWhenServiceThrowAnException() throws NotUniqueException {
        GroupDTO groupDTO = new GroupDTO();
        Mockito.when(groupService.create(groupDTO)).thenThrow(new NotUniqueException(null, null, null, (String)null));
        Assertions.assertThrows(NotUniqueException.class, () -> groupResource.create(groupDTO));
    }
    
    @Test
    void createShouldReturnEntityResponseCreatedWithServiceResultAsBodyWhenServiceSucceed() throws NotUniqueException {
        GroupDTO groupDTO = new GroupDTO();
        Mockito.when(groupService.create(groupDTO)).thenReturn(groupDTO);
        ResponseEntity<GroupDTO> response = groupResource.create(groupDTO);
        Assertions.assertEquals(201, response.getStatusCodeValue());
        Assertions.assertEquals(groupDTO, response.getBody());
    }
    
    @Test
    void deleteShouldThrowExceptionWhenServiceThrowAnException() throws BadRequestException {
        Mockito.doThrow(new BadRequestException(null, null, null)).when(groupService).delete("groupName");
        Assertions.assertThrows(BadRequestException.class, () -> groupResource.delete("groupName"));
    }
    
    @Test
    void deleteShouldSucceedWhenServiceSucceed() throws BadRequestException {
        Mockito.doNothing().when(groupService).delete("groupName");
        Assertions.assertDoesNotThrow(() ->groupResource.delete("groupName"));
    }

}
