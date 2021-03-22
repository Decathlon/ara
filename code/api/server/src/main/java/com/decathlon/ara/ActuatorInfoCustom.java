package com.decathlon.ara;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActuatorInfoCustom implements InfoContributor {

    private final RestTemplate restTemplate;

    @Value("${ara.clientBaseUrl:}")
    private String clientUrl;

    @Override
    public void contribute(Info.Builder builder) {
        try {
            var aboutInfoWU = restTemplate.getForObject(clientUrl + "static/version/about.json", AboutInfo.class);
            builder.withDetail("web-ui", aboutInfoWU);
        } catch (Exception e) {
            log.warn("Info endpoint hit but a problem occurs when fetching web-ui info", e);
        }
    }

    @Data
    private static class AboutInfo {
        String version;
        String sha;
    }
}
