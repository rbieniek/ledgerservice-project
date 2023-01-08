package org.ledgerservice.shared.security.rest.keycloak;

import org.ledgerservice.shared.security.rest.keycloak.KeycloakClaimsConfigurationProperties.SupportedClaims;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomClaimTypesConverterFactory implements
    Function<ClientRegistration, Converter<Map<String, Object>, Map<String, Object>>>,
    InitializingBean {

  private final KeycloakClaimsConfigurationProperties configurationProperties;

  private Map<String, Converter<Object, ?>> defaultMap;
  private Map<String, Converter<Map<String, Object>, Map<String, Object>>> clients = new ConcurrentHashMap<>();
  private ClaimTypeConverter defaultClaimsTypeConverter;

  public void registerClientRegistration(final ClientRegistration clientRegistration) {
    final String registrationId = clientRegistration.getRegistrationId();

    log.info("Add client {} with registration {}",
        clientRegistration.getClientSecret(),
        registrationId);

    clients.put(clientRegistration.getRegistrationId(),
        buildConverterMap(Optional.ofNullable(configurationProperties
                .getClients())
            .orElse(Collections.emptyMap())
                .getOrDefault(registrationId, configurationProperties
                    .getDefaultClaims())));
  }


  @Override
  public Converter<Map<String, Object>, Map<String, Object>> apply(final ClientRegistration clientRegistration) {
    log.info("Retrieving converters for client {} with registration {}, having custom setup {}",
        clientRegistration.getClientSecret(),
        clientRegistration.getRegistrationId(),
        clients.keySet().contains(clientRegistration.getRegistrationId()));

    return clients.getOrDefault(clientRegistration.getRegistrationId(),
        defaultClaimsTypeConverter);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    defaultMap = ReactiveOidcIdTokenDecoderFactory.createDefaultClaimTypeConverters();
    defaultClaimsTypeConverter = new ClaimTypeConverter(defaultMap);
  }

  private RemappingClaimTypeConverter buildConverterMap(final SupportedClaims claims) {
    final Map<String, Converter<Object, ?>> customsMap = new ConcurrentHashMap<>(defaultMap);

    if (claims.isRealmAccess()) {
      customsMap.put(firstPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getRealmAccess()),
          new UmaRealmAccessRolesClaimConverter(remainingPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getRealmAccess())));
    }

    if (claims.isResourceAccess()) {
      customsMap.put(firstPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getResourceAccess()),
          new UmaResourceAccessRolesClaimConverter(remainingPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getResourceAccess())));
    }

    if(claims.isGroups()) {
      customsMap.put(firstPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getGroups()),
          new GroupsClaimConverter(remainingPropertyPathPart(configurationProperties
              .getPropertyPaths()
              .getGroups())));
    }

    if(claims.isRoles()) {
      customsMap.put(firstPropertyPathPart(configurationProperties
          .getPropertyPaths()
          .getRoles()),
        new RolesClaimConverter(remainingPropertyPathPart(configurationProperties
          .getPropertyPaths()
          .getGroups())));
    }

    return new RemappingClaimTypeConverter(customsMap);
  }

  private String firstPropertyPathPart(final String propertyPath) {
    return StringUtils.split(propertyPath, '.')[0];
  }

  private String remainingPropertyPathPart(final String propertyPath) {
    final List<String> parts = new LinkedList<>(Arrays
        .stream(StringUtils
            .split(propertyPath, '.'))
        .collect(Collectors.toList()));

    parts.remove(0);

    return parts.stream()
        .collect(Collectors.joining("."));
  }
}
