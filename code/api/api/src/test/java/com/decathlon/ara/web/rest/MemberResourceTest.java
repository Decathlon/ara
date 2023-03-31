package com.decathlon.ara.web.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.service.MemberService;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

abstract class MemberResourceTest<C, M extends Member, R extends MemberRelationship<C,M>> {
    
    abstract MemberService<C, M, R> getMemberService();
    abstract MemberResource<C, M, R> getMemberResource();
    abstract Map<String, String> getParametersMap();
    
    @Test
    void getAllShouldReturnServiceResult() {
        List<MemberDTO> serviceResult = List.of(new MemberDTO());
        Mockito.when(getMemberService().findAll("identifier")).thenReturn(serviceResult);
        List<MemberDTO> result = getMemberResource().getAll(getParametersMap());
        Assertions.assertEquals(serviceResult, result);
    }
    
    @Test
    void getShouldThrowExceptionWhenServiceThrowAnException() throws NotFoundException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        Mockito.when(getMemberService().findOne(identifier, "memberName")).thenThrow(new NotFoundException(null, null));
        Assertions.assertThrows(NotFoundException.class, () -> getMemberResource().get(parametersMap, "memberName"));
    }
    
    @Test
    void getShouldReturnServiceResultWhenServiceSucceed() throws NotFoundException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        MemberDTO serviceResult = new MemberDTO();
        Mockito.when(getMemberService().findOne(identifier, "memberName")).thenReturn(serviceResult);
        MemberDTO result = getMemberResource().get(parametersMap, "memberName");
        Assertions.assertEquals(serviceResult, result);
    }
    
    @Test
    void addMemberShouldThrowExceptionWhenServiceThrowAnException() throws BadRequestException {
        try (MockedStatic<ServletUriComponentsBuilder> servletUriComponentsBuilderStaticMock = Mockito.mockStatic(ServletUriComponentsBuilder.class)){
            ServletUriComponentsBuilder servletUriComponentsBuilder = Mockito.mock(ServletUriComponentsBuilder.class, Mockito.RETURNS_SELF);
            UriComponents uriComponents = Mockito.mock(UriComponents.class);
            Mockito.when(uriComponents.toUri()).thenReturn(Mockito.mock(URI.class));
            Mockito.when(servletUriComponentsBuilder.build()).thenReturn(uriComponents);
            servletUriComponentsBuilderStaticMock.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(servletUriComponentsBuilder);
            Map<String, String> parametersMap = getParametersMap();
            String identifier = getMemberResource().getIdentifier(parametersMap);
            MemberDTO memberDTO = new MemberDTO();
            Mockito.when(getMemberService().addMember(identifier, memberDTO)).thenThrow(new BadRequestException(null, null, null));
            Assertions.assertThrows(BadRequestException.class, () -> getMemberResource().addMember(parametersMap, memberDTO));
        }
    }
    
    @Test
    void addMemberShouldReturnResponseCreatedWithServiceResultAsBodyWhenServiceSucceed() throws BadRequestException {
        try (MockedStatic<ServletUriComponentsBuilder> servletUriComponentsBuilderStaticMock = Mockito.mockStatic(ServletUriComponentsBuilder.class)){
            ServletUriComponentsBuilder servletUriComponentsBuilder = Mockito.mock(ServletUriComponentsBuilder.class, Mockito.RETURNS_SELF);
            UriComponents uriComponents = Mockito.mock(UriComponents.class);
            Mockito.when(uriComponents.toUri()).thenReturn(Mockito.mock(URI.class));
            Mockito.when(servletUriComponentsBuilder.build()).thenReturn(uriComponents);
            servletUriComponentsBuilderStaticMock.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(servletUriComponentsBuilder);
            Map<String, String> parametersMap = getParametersMap();
            String identifier = getMemberResource().getIdentifier(parametersMap);
            MemberDTO memberDTO = new MemberDTO();
            MemberDTO serviceResult = new MemberDTO();
            Mockito.when(getMemberService().addMember(identifier, memberDTO)).thenReturn(serviceResult);
            ResponseEntity<MemberDTO> response = getMemberResource().addMember(parametersMap, memberDTO);
            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertEquals(serviceResult, response.getBody());
        }
    }
    
    @Test
    void updateMemberRoleShouldThrowExceptionWhenServiceThrowAnException() throws BadRequestException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        MemberDTO memberDTO = new MemberDTO("memberName", MemberRole.ADMIN);
        Mockito.when(getMemberService().updateMemberRole(identifier, memberDTO.getName(), memberDTO.getRole())).thenThrow(new BadRequestException(null, null, null));
        Assertions.assertThrows(BadRequestException.class, () -> getMemberResource().updateMemberRole(parametersMap, memberDTO.getName(), memberDTO));
    }
    
    @Test
    void updateMemberRoleShouldReturnResponseCreatedWithServiceResultAsBodyWhenServiceSucceed() throws BadRequestException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        MemberDTO memberDTO = new MemberDTO("memberName", MemberRole.ADMIN);
        MemberDTO serviceResult = new MemberDTO();
        Mockito.when(getMemberService().updateMemberRole(identifier, memberDTO.getName(), memberDTO.getRole())).thenReturn(serviceResult);
        MemberDTO result = getMemberResource().updateMemberRole(parametersMap, memberDTO.getName(), memberDTO);
        Assertions.assertEquals(serviceResult, result);
    }
    
    @Test
    void deleteMemberShouldThrowExceptionWhenServiceThrowAnException() throws BadRequestException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        Mockito.doThrow(new BadRequestException(null, null, null)).when(getMemberService()).deleteMember(identifier, "memberName");
        Assertions.assertThrows(BadRequestException.class, () -> getMemberResource().deleteMember(parametersMap, "memberName"));
    }
    
    @Test
    void deleteMemberShouldReturnResponseCreatedWithServiceResultAsBodyWhenServiceSucceed() throws BadRequestException {
        Map<String, String> parametersMap = getParametersMap();
        String identifier = getMemberResource().getIdentifier(parametersMap);
        Mockito.doNothing().when(getMemberService()).deleteMember(identifier, "memberName");
        Assertions.assertDoesNotThrow(() -> getMemberResource().deleteMember(parametersMap, "memberName"));
    }

}
