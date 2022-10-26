package fi.vm.yti.security.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.vm.yti.security.NewUser;
import fi.vm.yti.security.Organization;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.ShibbolethAuthenticationDetails;
import fi.vm.yti.security.User;
import fi.vm.yti.security.YtiUser;
import fi.vm.yti.security.util.RoleUtil;
import static fi.vm.yti.security.config.RestTemplateConfig.httpClient;
import static org.springframework.util.StringUtils.isEmpty;

public class YtiAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final RestTemplate restTemplate;
    private final String groupmanagementUrl;

    YtiAuthenticationUserDetailsService(final String groupmanagementUrl) {
        this.groupmanagementUrl = groupmanagementUrl;
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient()));
    }

    @Override
    public UserDetails loadUserDetails(final PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        final ShibbolethAuthenticationDetails shibbolethDetails = (ShibbolethAuthenticationDetails) token.getDetails();
        final NewUser newUser = new NewUser();

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromHttpUrl(this.groupmanagementUrl)
            .path("/private-api/user");
        newUser.email = shibbolethDetails.getEmail();

        if (!isEmpty(shibbolethDetails.getFirstName()) && !isEmpty(shibbolethDetails.getLastName())) {
            newUser.firstName = shibbolethDetails.getFirstName();
            newUser.lastName = shibbolethDetails.getLastName();
        }

        if (!isEmpty(shibbolethDetails.getId())) {
            newUser.id = UUID.fromString(shibbolethDetails.getId());
        }

        final String getUserUri = uriBuilder.build().toUriString();
        final HttpEntity<NewUser> request = new HttpEntity<>(newUser);
        final ResponseEntity<User> response = this.restTemplate.postForEntity(getUserUri, request, User.class);
        final User user = response.getBody();
        final Map<UUID, Set<Role>> rolesInOrganizations = new HashMap<>();

        for (final Organization organization : user.organization) {
            final Set<Role> roles = organization.role.stream()
                .filter(RoleUtil::isRoleMappableToEnum)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
            rolesInOrganizations.put(organization.uuid, roles);
        }

        return new YtiUser(user.email, user.firstName, user.lastName, user.id, user.superuser, user.newlyCreated, user.tokenCreatedAt, user.tokenInvalidationAt, rolesInOrganizations, user.containerUri, user.tokenRole);
    }
}
