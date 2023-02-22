package com.decathlon.ara.security.service.member.user.account;

import com.decathlon.ara.security.configuration.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.ProviderSetupConfiguration;
import com.decathlon.ara.security.configuration.providers.setup.provider.ProviderConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private OAuth2ProvidersConfiguration providersConfiguration;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTO_whenAllFieldsAreNotNull() {
        // Given
        var loginUrl = "/login/url";
        var logoutUrl = "/logout/url";
        ReflectionTestUtils.setField(authenticationService, "loginStartingUrl", loginUrl);
        ReflectionTestUtils.setField(authenticationService, "logoutProcessingUrl", logoutUrl);

        var providerSetup1 = mock(ProviderSetupConfiguration.class);
        var provider1 = mock(ProviderConfiguration.class);
        var providerSetup2 = mock(ProviderSetupConfiguration.class);
        var provider2 = mock(ProviderConfiguration.class);
        var providerSetup3 = mock(ProviderSetupConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var setup = List.of(providerSetup1, providerSetup2, providerSetup3);

        // When
        when(providersConfiguration.getSetup()).thenReturn(setup);
        when(providerSetup1.getProvider()).thenReturn(provider1);
        when(provider1.getDisplayValue()).thenReturn("Provider1");
        when(provider1.getType()).thenReturn("custom");
        when(provider1.getRegistration()).thenReturn("registration1");
        when(providerSetup2.getProvider()).thenReturn(provider2);
        when(provider2.getDisplayValue()).thenReturn("Provider2");
        when(provider2.getType()).thenReturn("google");
        when(provider2.getRegistration()).thenReturn("registration2");
        when(providerSetup3.getProvider()).thenReturn(provider3);
        when(provider3.getDisplayValue()).thenReturn("Provider3");
        when(provider3.getType()).thenReturn("github");
        when(provider3.getRegistration()).thenReturn("registration3");

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration)
                .extracting(
                        "loginUrl",
                        "logoutUrl"
                )
                .contains(
                        loginUrl,
                        logoutUrl
                );
        assertThat(authenticationConfiguration.getProviders())
                .extracting(
                        "displayValue",
                        "type",
                        "name"
                )
                .contains(
                        tuple(
                                "Provider1",
                                "custom",
                                "registration1"
                        ),
                        tuple(
                                "Provider2",
                                "google",
                                "registration2"
                        ),
                        tuple(
                                "Provider3",
                                "github",
                                "registration3"
                        )
                );
    }

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTOAndIgnoreNullProviders_whenSomeProvidersAreNull() {
        // Given
        var loginUrl = "/login/url";
        var logoutUrl = "/logout/url";
        ReflectionTestUtils.setField(authenticationService, "loginStartingUrl", loginUrl);
        ReflectionTestUtils.setField(authenticationService, "logoutProcessingUrl", logoutUrl);

        var providerSetup1 = mock(ProviderSetupConfiguration.class);
        var provider1 = mock(ProviderConfiguration.class);
        var providerSetup2 = mock(ProviderSetupConfiguration.class);
        var providerSetup3 = mock(ProviderSetupConfiguration.class);
        var provider3 = mock(ProviderConfiguration.class);
        var setup = List.of(providerSetup1, providerSetup2, providerSetup3);

        // When
        when(providersConfiguration.getSetup()).thenReturn(setup);
        when(providerSetup1.getProvider()).thenReturn(provider1);
        when(provider1.getDisplayValue()).thenReturn("Provider1");
        when(provider1.getType()).thenReturn("custom");
        when(provider1.getRegistration()).thenReturn("registration1");
        when(providerSetup2.getProvider()).thenReturn(null);
        when(providerSetup3.getProvider()).thenReturn(provider3);
        when(provider3.getDisplayValue()).thenReturn("Provider3");
        when(provider3.getType()).thenReturn("github");
        when(provider3.getRegistration()).thenReturn("registration3");

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration)
                .extracting(
                        "loginUrl",
                        "logoutUrl"
                )
                .contains(
                        loginUrl,
                        logoutUrl
                );
        assertThat(authenticationConfiguration.getProviders())
                .extracting(
                        "displayValue",
                        "type",
                        "name"
                )
                .contains(
                        tuple(
                                "Provider1",
                                "custom",
                                "registration1"
                        ),
                        tuple(
                                "Provider3",
                                "github",
                                "registration3"
                        )
                );
    }

    @Test
    void getAuthenticationConfiguration_returnAuthenticationProvidersDTO_whenSetupIsNull() {
        // Given

        // When
        when(providersConfiguration.getSetup()).thenReturn(null);

        // Then
        var authenticationConfiguration = authenticationService.getAuthenticationConfiguration();
        assertThat(authenticationConfiguration.getProviders())
                .isNotNull()
                .isEmpty();
    }
}
