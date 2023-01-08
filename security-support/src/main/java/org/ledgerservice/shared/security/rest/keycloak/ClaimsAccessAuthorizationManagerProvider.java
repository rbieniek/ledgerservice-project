package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ClaimsAccessAuthorizationManagerProvider {

  public ReactiveAuthorizationManager<AuthorizationContext> groups(String... groupNames) {
    return groups(Arrays.stream(groupNames).collect(Collectors.toSet()));
  }

  public ReactiveAuthorizationManager<AuthorizationContext> groups(final Set<String> groupNames) {
    return new OrReactiveAuthorizationManager(Flux
        .fromStream(groupNames.stream())
        .map(name -> group(name)));
  }

  public ClaimsAccessAuthorizationManager group(final String groupName) {
    return new ClaimsAccessAuthorizationManager(groupName, CustomClaimNames.CLAIM_GROUPS);
  }
}
