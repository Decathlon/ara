package com.decathlon.ara.web.rest.authentication;

import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.service.UserService;
import com.decathlon.ara.service.dto.user.RoleDTO;
import com.decathlon.ara.service.dto.user.UserDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private OidcUser oidcUser;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;

    @Test
    void getAllShouldReturnServiceResult() {
        List<UserDTO> serviceResult = List.of(new UserDTO());
        Mockito.when(userService.findAll()).thenReturn(serviceResult);
        List<UserDTO> result = userResource.getAll();
        Assertions.assertEquals(serviceResult, result);
    }

    @Test
    void getUserDetailsShouldThrowExceptionWhenServiceThrowAnException() throws NotFoundException {
        Mockito.when(userService.findOne("userName")).thenThrow(new NotFoundException(null, null));
        Assertions.assertThrows(NotFoundException.class, () -> userResource.getUserDetails("userName"));
    }

    @Test
    void getUserDetailsShouldReturnServiceResultWhenServiceSucceed() throws NotFoundException {
        UserDTO userDTO = new UserDTO();
        Mockito.when(userService.findOne("userName")).thenReturn(userDTO);
        UserDTO result = userResource.getUserDetails("userName");
        Assertions.assertEquals(userDTO, result);
    }

    @Test
    void addUserRoleShouldThrowExceptionWhenServiceThrowAnException() throws BadRequestException {
        try (MockedStatic<ServletUriComponentsBuilder> servletUriComponentsBuilderStaticMock = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder servletUriComponentsBuilder = Mockito.mock(ServletUriComponentsBuilder.class, Mockito.RETURNS_SELF);
            UriComponents uriComponents = Mockito.mock(UriComponents.class);
            Mockito.when(uriComponents.toUri()).thenReturn(Mockito.mock(URI.class));
            Mockito.when(servletUriComponentsBuilder.build()).thenReturn(uriComponents);
            servletUriComponentsBuilderStaticMock.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(servletUriComponentsBuilder);
            RoleDTO roleDTO = new RoleDTO(UserSecurityRole.ADMIN);
            Mockito.when(userService.addRole("userName", UserSecurityRole.ADMIN)).thenThrow(new BadRequestException(null, null, null));
            Assertions.assertThrows(BadRequestException.class, () -> userResource.addUserRole("userName", roleDTO));
        }
    }

    @Test
    void addUserRoleShouldReturnResponseCreatedWithServiceResultAsBodyWhenServiceSucceed() throws BadRequestException {
        try (MockedStatic<ServletUriComponentsBuilder> servletUriComponentsBuilderStaticMock = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder servletUriComponentsBuilder = Mockito.mock(ServletUriComponentsBuilder.class, Mockito.RETURNS_SELF);
            UriComponents uriComponents = Mockito.mock(UriComponents.class);
            Mockito.when(uriComponents.toUri()).thenReturn(Mockito.mock(URI.class));
            Mockito.when(servletUriComponentsBuilder.build()).thenReturn(uriComponents);
            servletUriComponentsBuilderStaticMock.when(ServletUriComponentsBuilder::fromCurrentRequestUri).thenReturn(servletUriComponentsBuilder);
            RoleDTO roleDTO = new RoleDTO(UserSecurityRole.ADMIN);
            RoleDTO serviceResult = new RoleDTO(UserSecurityRole.ADMIN);
            Mockito.when(userService.addRole("userName", UserSecurityRole.ADMIN)).thenReturn(serviceResult);
            ResponseEntity<RoleDTO> response = userResource.addUserRole("userName", roleDTO);
            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertEquals(serviceResult, response.getBody());
        }
    }

    @Test
    void deleteUserRoleShouldThrowExceptionWhenServiceThrowAnException() throws NotFoundException {
        Mockito.doThrow(new NotFoundException(null, null)).when(userService).deleteRole("userName", UserSecurityRole.ADMIN);
        Assertions.assertThrows(NotFoundException.class, () -> userResource.deleteUserRole("userName", UserSecurityRole.ADMIN));
    }

    @Test
    void deleteUserRoleShouldSucceedWhenServiceSucceed() throws NotFoundException {
        Mockito.doNothing().when(userService).deleteRole("userName", UserSecurityRole.ADMIN);
        Assertions.assertDoesNotThrow(() -> userResource.deleteUserRole("userName", UserSecurityRole.ADMIN));
    }

    @Test
    void whenOIDCUser_userDetails_should_not_be_null() {
        when(oidcUser.getSubject()).thenReturn("oidc_id");
        var userDetails = userResource.getUserDetails(oidcUser);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oidc_id", userDetails.getId());
    }

    @Test
    void whenOAauth2User_userDetails_should_not_be_null() {
        when(oauth2User.getAttribute("id")).thenReturn("oauth2_id");
        var userDetails = userResource.getUserDetails(oauth2User);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oauth2_id", userDetails.getId());
    }

    @Test
    void whenOAauth2UserAndOIDCUser_userDetails_should_be_generated_from_oidc() {
        when(oidcUser.getFullName()).thenReturn("oidc_name");
        var userDetails = userResource.getUserDetails(oidcUser);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("oidc_name", userDetails.getName());
    }

}
