package fi.vm.yti.security.config;

import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;


public class KeycloakUserDetailsAuthenticationToken extends KeycloakAuthenticationToken {

  private UserDetails userDetails;

  public KeycloakUserDetailsAuthenticationToken(UserDetails userDetails, OidcKeycloakAccount account,
      Collection<? extends GrantedAuthority> authorities) {
    super(account, false, authorities);
    Assert.notNull(userDetails, "UserDetails required");
    this.userDetails = userDetails;
  }

  @Override
  public Object getPrincipal() {
    return userDetails;
  }

}