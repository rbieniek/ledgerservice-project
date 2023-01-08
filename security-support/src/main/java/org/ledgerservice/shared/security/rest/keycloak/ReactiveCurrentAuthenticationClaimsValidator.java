package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;

public class ReactiveCurrentAuthenticationClaimsValidator {

  public Mono<Boolean> matchesGroup(final String groupName) {
    return matchesGroup(Collections.singletonList(groupName));
  }

  public Mono<Boolean> matchesGroup(final String... groupNames) {
    return matchesGroup(Arrays.stream(groupNames).toList());
  }

  public Mono<Boolean> matchesGroup(final Collection<String> groupNames) {
    return matchesGroup(ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication),
        groupNames);
  }

  public Mono<Boolean> matchesGroup(final Mono<Authentication> authentication, final String groupName) {
    return matchesGroup(authentication, Collections.singletonList(groupName));
  }

  public Mono<Boolean> matchesGroup(final Mono<Authentication> authentication, final String... groupNames) {
    return matchesGroup(authentication, Arrays.stream(groupNames).toList());
  }

  public Mono<Boolean> matchesGroup(final Mono<Authentication> authentication, final Collection<String> groupNames) {
    return matchesClaim(authentication,
        groupNames, CustomClaimNames.CLAIM_GROUPS);
  }

  private Mono<Boolean> matchesClaim(final Mono<Authentication> authentication,
      final Collection<String> groupNames,
      final String claimName) {
    return authentication
        .map(auth -> auth.getPrincipal())
        .filter(auth -> auth instanceof OidcUser)
        .map(auth -> ((OidcUser) auth).getClaims())
        .map(claims -> claims.get(claimName))
        .filter(claimValue -> claimValue instanceof ValidateableClaim)
        .map(claimValue -> ((ValidateableClaim) claimValue).containsAny(groupNames))
        .switchIfEmpty(Mono.just(false));
  }
}
