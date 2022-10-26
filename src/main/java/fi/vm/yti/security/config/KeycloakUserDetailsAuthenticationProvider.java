package fi.vm.yti.security.config;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.util.Assert;

public class KeycloakUserDetailsAuthenticationProvider extends KeycloakAuthenticationProvider {

  private UserDetailsService userDetailsService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);
    UserDetails userDetails;

    if (token == null) {
      return null;
    }

    AccessToken accessToken = this.getAccessToken(token);
    userDetails = ((KeycloakUserDetailsService)userDetailsService).loadUser(accessToken);

    return new KeycloakUserDetailsAuthenticationToken(userDetails, token.getAccount(), token.getAuthorities());
  }

  protected AccessToken getAccessToken(KeycloakAuthenticationToken token) {

    Assert.notNull(token, "KeycloakAuthenticationToken required");
    Assert.notNull(token.getAccount(), "KeycloakAuthenticationToken.getAccount() cannot be return null");
    OidcKeycloakAccount account = token.getAccount();
    KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>)account.getPrincipal();

    AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
    
    return accessToken;
  }

  @Autowired
  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }
}