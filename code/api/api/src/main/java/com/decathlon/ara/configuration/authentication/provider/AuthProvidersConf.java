package com.decathlon.ara.configuration.authentication.provider;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("oauth2providers")
public class AuthProvidersConf {

    private List<Oauth2ProvidersInfos> conf;

    public List<Oauth2ProvidersInfos> getConf() {
        return conf;
    }

    public void setConf(List<Oauth2ProvidersInfos> conf) {
        this.conf = conf;
    }

    public static class Oauth2ProvidersInfos {

        private String displayName;
        private String providerType;
        private String code;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProviderType() {
            return providerType;
        }

        public void setProviderType(String providerType) {
            this.providerType = providerType;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
