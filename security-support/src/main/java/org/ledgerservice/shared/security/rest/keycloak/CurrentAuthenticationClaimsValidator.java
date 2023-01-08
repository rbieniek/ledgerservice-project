package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class CurrentAuthenticationClaimsValidator {

  public boolean matchesGroup(final String groupName) {
    return matchesGroup(Collections.singletonList(groupName));
  }

  public boolean matchesGroup(final String... groupNames) {
    return matchesGroup(Arrays.stream(groupNames).toList());
  }

  public boolean matchesGroup(final List<String> groupNames) {
    return Optional.ofNullable(SecurityContextHolder.getContext())
        .map(context -> context.getAuthentication())
        .filter(auth -> auth instanceof OidcUser)
        .map(auth -> ((OidcUser) auth).getClaims())
        .map(claims -> claims.get(CustomClaimNames.CLAIM_GROUPS))
        .filter(claim -> claim instanceof ValidateableClaim)
        .map(claim -> ((ValidateableClaim) claim).containsAny(groupNames))
        .orElse(false);
  }
}
