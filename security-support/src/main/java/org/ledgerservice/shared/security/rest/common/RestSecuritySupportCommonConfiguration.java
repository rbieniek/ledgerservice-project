package org.ledgerservice.shared.security.rest.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ledgerservice.shared.security.rest.keycloak.ReactiveJwtKeycloakAuthenticationConverter;
import org.ledgerservice.spring.support.LocalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = LocalTestConfiguration.class))
@Slf4j
public class RestSecuritySupportCommonConfiguration {

  public static final int CUSTOM_CHAIN_FIRST_ORDER_VALUE = 10;

  @Bean
  @Autowired
  @Order(1)
  public SecurityWebFilterChain actuatorSecurity(final ServerHttpSecurity http) {
    log.info("Settiing up actuator security");

    http.securityMatcher(EndpointRequest.toAnyEndpoint())
      .csrf(CsrfSpec::disable)
      .authorizeExchange()
      .anyExchange()
      .permitAll();

    return http.build();
  }

  @Bean
  @Autowired
  @Order(2)
  public SecurityWebFilterChain openApiSecurity(final ServerHttpSecurity http) {
    log.info("Setting up OpenAPI security");

    http.securityMatcher(new PathPatternParserServerWebExchangeMatcher("/v3/**"))
      .csrf(CsrfSpec::disable)
      .authorizeExchange()
      .anyExchange()
      .permitAll();

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(prefix = RestSecuritySupportCommonConfigurationProperties.PREFIX, name = "multi-tenancy-jwks-uri.0")
  @Autowired
  @Order(3)
  public SecurityWebFilterChain apiEndpointMultiTenantSecurity(final ServerHttpSecurity http,
    final ReactiveJwtKeycloakAuthenticationConverter authenticationConverter,
    final RestSecuritySupportCommonConfigurationProperties configurationProperties) {
    log.info("Setting up API ReST endpoint multi-tenant security");

    final TrustedIssuerJwtAuthenticationManagerResolver authenticationManagerResolver
      = new TrustedIssuerJwtAuthenticationManagerResolver();

    configurationProperties.getMultiTenancyJwksUri()
      .forEach(trustedIssuer -> authenticationManagerResolver.addTrustedIssuer(trustedIssuer,
        spec -> spec.jwtAuthenticationConverter(grantedAuthoritiesExtractor(authenticationConverter))));

    // @formatter:off
    http
      .csrf(CsrfSpec::disable)
      .authorizeExchange(exchanges -> exchanges
        .pathMatchers(configurationProperties.getApiPath())
        .hasAuthority(roleToRealmRole(configurationProperties.getRequiredRole()))
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .authenticationManagerResolver(
          new JwtIssuerReactiveAuthenticationManagerResolver(authenticationManagerResolver))
      );
    // @formatter:on

    return http.build();
  }

  /*
  spring.security.oauth2.resourceserver.jwt.issuerUri
   */
  @Bean
  @ConditionalOnProperty(value = "spring.security.oauth2.resourceserver.jwt.issuer-uri")
  @Autowired
  @Order(4)
  public SecurityWebFilterChain apiEndpointSingleTenantSecurity(final ServerHttpSecurity http,
    final ReactiveJwtKeycloakAuthenticationConverter grantedAuthorityConverter,
    final RestSecuritySupportCommonConfigurationProperties configurationProperties) {
    log.info("Setting up API ReST endpoint single-tenant security");

    // @formatter:off
    http
      .csrf(CsrfSpec::disable)
      .authorizeExchange(exchanges -> exchanges
        .pathMatchers(configurationProperties.getApiPath())
        .hasAuthority(roleToRealmRole(configurationProperties.getRequiredRole()))
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor(grantedAuthorityConverter))));
    // @formatter:on

    return http.build();
  }

  @Bean
  @Autowired
  @Order(Ordered.LOWEST_PRECEDENCE)
  public SecurityWebFilterChain defaultSecurity(final ServerHttpSecurity http) {
    log.info("Setting up default server security");

    http.csrf(CsrfSpec::disable)
      .authorizeExchange()
      .anyExchange()
      .denyAll();

    return http.build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor(
    final ReactiveJwtKeycloakAuthenticationConverter authenticationConverter) {
    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authenticationConverter);

    return jwtAuthenticationConverter;
  }

  private String roleToRealmRole(final String role) {
    final StringBuilder roleBuilder = new StringBuilder("REALM_ROLE_");

    roleBuilder.append(StringUtils.upperCase(StringUtils.replace(role, "-", "_")));

    return roleBuilder.toString();
  }
}
