package com.decathlon.ara.web.rest;


import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

@RestController
@RequestMapping(DebugResource.PATH)
@Profile("dev")
public class DebugResource {
    public static final String PATH=API_PATH + "/" + "debug";


    @GetMapping("/oauth")
    public String getConnectionInfos() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        Jwt credentials= (Jwt) authentication.getCredentials();
        return String.format(" name: %s, \nauthorities: %s, \nprincipal: %s, \ncredentials: %s, \ndetails: %s, \ncreds claims: %s",
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map( entry -> String.format("[ %s ]", entry.toString() ) )
                        .reduce( "", (String acc, String entry2) -> acc + entry2 ),
                principal,
                credentials,
                authentication.getDetails(),
                credentials.getClaims().entrySet().stream()
                        .map( entry -> String.format("[ %s ]", entry.toString() ) )
                        .reduce( "", (String acc, String entry2) -> acc + entry2 )
        );

    }


}
