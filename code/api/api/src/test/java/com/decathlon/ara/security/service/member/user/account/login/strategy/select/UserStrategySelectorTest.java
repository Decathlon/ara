package com.decathlon.ara.security.service.member.user.account.login.strategy.select;

import com.decathlon.ara.security.configuration.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.service.member.user.account.login.strategy.BasicUserAccountStrategy;
import com.decathlon.ara.security.service.member.user.account.login.strategy.GithubUserAccountStrategy;
import com.decathlon.ara.security.service.member.user.account.login.strategy.GoogleUserAccountStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStrategySelectorTest {

    @Mock
    private GoogleUserAccountStrategy googleStrategy;

    @Mock
    private GithubUserAccountStrategy githubStrategy;

    @Mock
    private OAuth2ProvidersConfiguration oauth2ProvidersConfiguration;

    @InjectMocks
    private UserStrategySelector selector;

    @ParameterizedTest
    @ValueSource(strings = {"google", "Google", "GOOGLE", "gOoGlE", "GoOgLe"})
    void selectUserStrategyFromProviderName_returnGoogleUserAccountStrategy_whenProviderNameIsGoogle(String providerName) {
        // Given

        // When

        // Then
        var strategy = selector.selectUserStrategyFromProviderName(providerName);
        assertThat(strategy).isSameAs(googleStrategy);
    }

    @ParameterizedTest
    @ValueSource(strings = {"github", "Github", "GITHUB", "gItHuB", "GiThUb"})
    void selectUserStrategyFromProviderName_returnGithubUserAccountStrategy_whenProviderNameIsGithub(String providerName) {
        // Given

        // When

        // Then
        var strategy = selector.selectUserStrategyFromProviderName(providerName);
        assertThat(strategy).isSameAs(githubStrategy);
    }

    @Test
    void selectUserStrategyFromProviderName_returnBasicUserAccountStrategy_whenProviderNameIsAnyOtherName() {
        // Given
        var providerName = "another-provider-name";
        var customProviderAttributes = mock(Map.class);

        // When
        when(oauth2ProvidersConfiguration.getUsersCustomAttributesFromProviderName(providerName)).thenReturn(customProviderAttributes);

        // Then
        var strategy = selector.selectUserStrategyFromProviderName(providerName);
        assertThat(strategy).isExactlyInstanceOf(BasicUserAccountStrategy.class);
        assertThat(((BasicUserAccountStrategy) strategy).getCustomProviderAttributes()).isSameAs(customProviderAttributes);
    }
}
