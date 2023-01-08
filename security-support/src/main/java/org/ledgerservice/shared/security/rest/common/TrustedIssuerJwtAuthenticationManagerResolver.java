package org.ledgerservice.shared.security.rest.common;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class TrustedIssuerJwtAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<String> {

  private Predicate<String> trustedIssuers = trustedIssuer -> false;

  private Map<String, Mono<ReactiveAuthenticationManager>> authenticationManagers = new ConcurrentHashMap<>();
  private Map<String, ReactiveAuthenticationManagerSpec> authenticationManagerSpec = new ConcurrentHashMap<>();

  public void addTrustedIssuer(final String trustedIssuer,
    final Customizer<ReactiveAuthenticationManagerSpec> customizer) {
    final var spec = new ReactiveAuthenticationManagerSpec();

    if (StringUtils.isBlank(trustedIssuer)) {
      throw new IllegalArgumentException("Empty trusted issuer not allowed");
    }

    if (authenticationManagerSpec.containsKey(trustedIssuer)) {
      throw new IllegalStateException(String.format("Trusted issuer '%s' already configured", trustedIssuer));
    }

    customizer.customize(spec);

    authenticationManagerSpec.put(trustedIssuer, spec);

    log.info("Added customizer for trusted issuer '{}'", trustedIssuer);

    trustedIssuers = Set.copyOf(authenticationManagerSpec.keySet())::contains;
  }

  @Override
  public Mono<ReactiveAuthenticationManager> resolve(final String issuer) {
    log.info("Retrieving authentation manager for issuer {}", issuer);

    if (StringUtils.isBlank(issuer)) {
      return Mono.empty();
    }

    if (!this.trustedIssuers.test(issuer)) {
      log.info("Issuer {} not matching any trusted issuer", issuer);

      return Mono.empty();
    }

    // @formatter:off
    return this.authenticationManagers.computeIfAbsent(issuer,
      (k) -> Mono.<ReactiveAuthenticationManager>fromCallable(() -> new JwtReactiveAuthenticationManager(
          ReactiveJwtDecoders.fromIssuerLocation(k)))
        .map(manager -> authenticationManagerSpec.get(k).configure(manager))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnNext(manger -> log.info("Created reactie authentication manager for trusted issuer {}", k))
        .cache((manager) -> Duration.ofMillis(Long.MAX_VALUE), (ex) -> Duration.ZERO, () -> Duration.ZERO)
    );
    // @formatter:on
  }

  public static class ReactiveAuthenticationManagerSpec {

    private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter;

    public ReactiveAuthenticationManagerSpec jwtAuthenticationConverter(
      final Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter) {

      this.jwtAuthenticationConverter = jwtAuthenticationConverter;

      return this;
    }

    protected ReactiveAuthenticationManager configure(
      final ReactiveAuthenticationManager reactiveAuthenticationManager) {

      if (reactiveAuthenticationManager instanceof JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager) {
        if (jwtAuthenticationConverter != null) {

          jwtReactiveAuthenticationManager.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        }
      }

      return reactiveAuthenticationManager;
    }
  }
}
