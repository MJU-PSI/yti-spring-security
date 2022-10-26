package fi.vm.yti.security.config;

import org.keycloak.representations.AccessToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.vm.yti.security.NewUser;
import fi.vm.yti.security.Organization;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.User;
import fi.vm.yti.security.YtiUser;
import fi.vm.yti.security.util.RoleUtil;

import static fi.vm.yti.security.config.RestTemplateConfig.httpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class KeycloakUserDetailsService implements UserDetailsService {

  private final RestTemplate restTemplate;
  private final String groupmanagementUrl;
  private String firstName;
  private String lastName;
  private String username;

  KeycloakUserDetailsService(final String groupmanagementUrl) {
        this.groupmanagementUrl = groupmanagementUrl;
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient()));
    }

  public UserDetails loadUser(AccessToken accessToken){

    if (accessToken == null) {
      return null;
    }

    this.firstName = accessToken.getGivenName();
    this.lastName = accessToken.getFamilyName();
    this.username = accessToken.getEmail();

    return loadUserByUsername(this.username);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final NewUser newUser = new NewUser();

    final UriComponentsBuilder uriBuilder = UriComponentsBuilder
        .fromHttpUrl(this.groupmanagementUrl)
        .path("/private-api/user");

    newUser.email = this.username;
    newUser.firstName = this.firstName;
    newUser.lastName = this.lastName;

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

    return new YtiUser(user.email, user.firstName, user.lastName, user.id, user.superuser, user.newlyCreated,
        user.tokenCreatedAt, user.tokenInvalidationAt, rolesInOrganizations, user.containerUri, user.tokenRole);
  }
}