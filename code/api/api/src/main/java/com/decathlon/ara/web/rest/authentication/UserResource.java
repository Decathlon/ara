package com.decathlon.ara.web.rest.authentication;

import static com.decathlon.ara.web.rest.util.RestConstants.API_PATH;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;
import com.decathlon.ara.service.UserService;
import com.decathlon.ara.service.dto.authentication.AuthenticationUserDetailsDTO;
import com.decathlon.ara.service.dto.user.RoleDTO;
import com.decathlon.ara.service.dto.user.UserDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

@RestController
@RequestMapping(UserResource.PATH)
public class UserResource {

    static final String PATH = API_PATH + "/users";

    private UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{userName}")
    public UserDTO getUserDetails(@PathVariable String userName) throws NotFoundException {
        return userService.findOne(userName);
    }

    @PostMapping("/{userName}")
    public ResponseEntity<RoleDTO> addUserRole(@PathVariable String userName, @Valid @RequestBody RoleDTO role) throws BadRequestException {
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequestUri().scheme(null).host(null).port(null).path("/" + role.getRole()).build().toUri()).body(userService.addRole(userName, role.getRole()));
    }

    @DeleteMapping("/{userName}/{role}")
    public void deleteUserRole(@PathVariable String userName, @PathVariable UserSecurityRole role) throws NotFoundException {
        userService.deleteRole(userName, role);
    }

    @GetMapping("/current/details")
    public AuthenticationUserDetailsDTO getUserDetails(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return new AuthenticationUserDetailsDTO(
                    oidcUser.getSubject(),
                    oidcUser.getFullName(),
                    oidcUser.getName(),
                    oidcUser.getEmail(),
                    oidcUser.getPicture());
        }

        return new AuthenticationUserDetailsDTO(
                safeStringAttribute(oauth2User, "id"),
                safeStringAttribute(oauth2User, "name"),
                safeStringAttribute(oauth2User, "login"),
                safeStringAttribute(oauth2User, "email"),
                safeStringAttribute(oauth2User, "avatar_url"));
    }

    private String safeStringAttribute(OAuth2User oauth2User, String attribute) {
        return Optional.ofNullable(oauth2User.getAttribute(attribute)).orElse("").toString();
    }

}
