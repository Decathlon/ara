package com.decathlon.ara.authentication;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.decathlon.ara.security.dto.user.scope.UserAccountScopeRole;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.web.rest.authentication.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.decathlon.ara.web.rest.util.HeaderUtil.MESSAGE;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private UserResource userResource;

    @Test
    void getCurrentUserAccount_returnBadRequestResponse_whenUserNotFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);

        // When
        when(userAccountService.getCurrentUserAccount(authentication)).thenReturn(Optional.empty());

        // Then
        var response = userResource.getCurrentUserAccount(authentication);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getCurrentUserAccount_returnRequestResponseContainingUser_whenUserFound() {
        // Given
        var authentication = mock(OAuth2AuthenticationToken.class);
        var userAccount = mock(UserAccount.class);

        // When
        when(userAccountService.getCurrentUserAccount(authentication)).thenReturn(Optional.of(userAccount));

        // Then
        var response = userResource.getCurrentUserAccount(authentication);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(userAccount);
    }

    @Test
    void updateUserScope_returnBadRequestResponse_whenRoleIsNull() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";
        var scope = mock(UserAccountScope.class);

        // When
        when(scope.getRole()).thenReturn(null);

        // Then
        var response = userResource.updateUserScope(userLogin, projectCode, scope);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        var expectedHeaders = Map.ofEntries(entry(MESSAGE, List.of("The role you have provided in the body cannot be null")));
        assertThat(response.getHeaders()).containsExactlyInAnyOrderEntriesOf(expectedHeaders);

        verify(userAccountService, never()).updateUserProjectScope(anyString(), anyString(), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void updateUserScope_returnBadRequestResponse_whenProjectCodeIsBlank(String projectCode) throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var scope = mock(UserAccountScope.class);
        var role = mock(UserAccountScopeRole.class);

        // When
        when(scope.getRole()).thenReturn(role);
        when(scope.getProject()).thenReturn(projectCode);

        // Then
        var response = userResource.updateUserScope(userLogin, projectCode, scope);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        var expectedHeaders = Map.ofEntries(entry(MESSAGE, List.of("The project code you have provided in the body cannot be left blank")));
        assertThat(response.getHeaders()).containsExactlyInAnyOrderEntriesOf(expectedHeaders);

        verify(userAccountService, never()).updateUserProjectScope(anyString(), anyString(), any());
    }

    @Test
    void updateUserScope_returnBadRequestResponse_whenRequestBodyProjectCodeIsDifferentFromPathVariableProjectCode() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var pathVariableProjectCode = "path-variable-project-code";
        var scope = mock(UserAccountScope.class);
        var role = mock(UserAccountScopeRole.class);
        var requestBodyProjectCode = "request-body-project-code";

        // When
        when(scope.getRole()).thenReturn(role);
        when(scope.getProject()).thenReturn(requestBodyProjectCode);

        // Then
        var response = userResource.updateUserScope(userLogin, pathVariableProjectCode, scope);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        var expectedHeaders = Map.ofEntries(entry(MESSAGE, List.of("The project code you have provided in the body (request-body-project-code) cannot be different from the project code in the url (path-variable-project-code)")));
        assertThat(response.getHeaders()).containsExactlyInAnyOrderEntriesOf(expectedHeaders);

        verify(userAccountService, never()).updateUserProjectScope(anyString(), anyString(), any());
    }

    @Test
    void updateUserScope_returnOkResponse_whenAllParametersProvidedAreOK() throws ForbiddenException {
        // Given
        var userLogin = "user-login";
        var projectCode = "project-code";
        var scope = mock(UserAccountScope.class);
        var role = mock(UserAccountScopeRole.class);

        // When
        when(scope.getRole()).thenReturn(role);
        when(scope.getProject()).thenReturn(projectCode);

        // Then
        var response = userResource.updateUserScope(userLogin, projectCode, scope);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        assertThat(response.getHeaders()).isEmpty();

        verify(userAccountService).updateUserProjectScope(userLogin, projectCode, role);
    }
}
