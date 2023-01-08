package org.ledgerservice.shared.security.rest.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrReactiveAuthorizationManager<T> implements ReactiveAuthorizationManager<T> {
  private final Flux<ReactiveAuthorizationManager<T>> managers;

  @Override
  public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, T object) {
    final Flux<Authentication> authentications = authentication.repeat();
    return managers
        .flatMap(manager -> manager.check(authentications.next(), object))
        .reduce((a,b) -> new AuthorizationDecision(a.isGranted() || b.isGranted()));
  }
}
