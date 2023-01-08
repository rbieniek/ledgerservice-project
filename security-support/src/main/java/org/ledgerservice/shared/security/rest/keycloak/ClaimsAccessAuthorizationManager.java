package org.ledgerservice.shared.security.rest.keycloak;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ClaimsAccessAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
  private final String claimValue;
  private final String claimName;

  @Override
  public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, final AuthorizationContext context) {
    return authentication
        .map(auth -> auth.getPrincipal())
        .filter(obj -> obj instanceof OidcUser)
        .flatMapMany(obj -> Flux.fromStream(((OidcUser)obj).getClaims()
            .entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(),
                entry.getValue()))))
        .filter(pair -> StringUtils.equals(pair.getKey(), claimName)
            && pair.getValue() instanceof ValidateableClaim)
        .map(pair ->  ((ValidateableClaim)pair.getValue()).contains(claimValue))
        .switchIfEmpty(Flux.just(false))
        .map(granted -> new AuthorizationDecision(granted))
        .next();
  }
}
