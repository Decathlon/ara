package com.decathlon.ara.security.service.member.user.account.login.strategy.select;


import com.decathlon.ara.security.configuration.providers.OAuth2ProvidersConfiguration;
import com.decathlon.ara.security.service.member.user.account.login.strategy.BasicUserAccountStrategy;
import com.decathlon.ara.security.service.member.user.account.login.strategy.GithubUserAccountStrategy;
import com.decathlon.ara.security.service.member.user.account.login.strategy.GoogleUserAccountStrategy;
import com.decathlon.ara.security.service.member.user.account.login.strategy.UserAccountStrategy;
import org.springframework.stereotype.Component;

@Component
public class UserStrategySelector {

    private final GoogleUserAccountStrategy googleUserAccountStrategy;

    private final GithubUserAccountStrategy githubUserAccountStrategy;

    private final OAuth2ProvidersConfiguration providersConfiguration;

    public UserStrategySelector(
            GoogleUserAccountStrategy googleUserAccountStrategy,
            GithubUserAccountStrategy githubUserAccountStrategy,
            OAuth2ProvidersConfiguration providersConfiguration
    ) {
        this.googleUserAccountStrategy = googleUserAccountStrategy;
        this.githubUserAccountStrategy = githubUserAccountStrategy;
        this.providersConfiguration = providersConfiguration;
    }

    /**
     * Select a {@link UserAccountStrategy} depending on the providerName.
     * @param providerName the provider name.
     * @return the {@link UserAccountStrategy}
     */
    public UserAccountStrategy selectUserStrategyFromProviderName(String providerName) {
        if ("google".equalsIgnoreCase(providerName)) {
            return googleUserAccountStrategy;
        }
        if ("github".equalsIgnoreCase(providerName)) {
            return githubUserAccountStrategy;
        }

        var customProviderAttributes = providersConfiguration.getUsersCustomAttributesFromProviderName(providerName);
        return new BasicUserAccountStrategy(customProviderAttributes);
    }
}
