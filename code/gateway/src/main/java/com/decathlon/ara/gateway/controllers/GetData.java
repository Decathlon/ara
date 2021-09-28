package com.decathlon.ara.gateway.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/debug")
public class GetData {

    @GetMapping("/oauth")
    public String getConnectionInfos(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OAuth2User user) {


        return String.format("name: %s, authorities: %s, attributes: %s",
                    user.getName(),
                    user.getAuthorities(),
                    user.getAttributes().entrySet()
                            .stream()
                            .map( entry -> String.format("[ %s ]", entry.toString() ) )
                            .reduce( "", (String acc, String entry2) -> acc + entry2 )
                    );

    }
}
