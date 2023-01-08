package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class ReactiveJwtKeycloakAuthenticationConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

  @Override
  public Flux<GrantedAuthority> convert(Jwt jwt) {
    return realmAccessRoles(jwt)
      .mergeWith(resourceAccessAccountRoles(jwt))
      .mergeWith(groups(jwt))
      .map(str -> StringUtils.upperCase(str))
      .map(str -> StringUtils.replace(str, "-", "_"))
      .doOnNext(str -> log.info("User {} extracted granted authority {}", jwt.getSubject(), str))
      .map(SimpleGrantedAuthority::new);
  }

  private Flux<String> realmAccessRoles(final Jwt jwt) {
    return Flux.fromStream(Optional
        .ofNullable(jwt.getClaims()
          .get("realm_access"))
        .filter(obj -> obj instanceof Map)
        .map(obj -> (Map) obj)
        .map(map -> map.get("roles"))
        .filter(obj -> obj instanceof Collection)
        .map(obj -> (Collection) obj)
        .orElse(Collections.emptyList())
        .stream())
      .map(obj -> new StringBuilder("REALM_ROLE_")
        .append(obj.toString())
        .toString());
  }

  private Flux<String> resourceAccessAccountRoles(final Jwt jwt) {
    return Flux.fromStream(Optional
        .ofNullable(jwt.getClaims()
          .get("resource_access"))
        .filter(obj -> obj instanceof Map)
        .map(obj -> (Map) obj)
        .map(map -> map.get("roles"))
        .filter(obj -> obj instanceof Collection)
        .map(obj -> (Collection) obj)
        .orElse(Collections.emptyList())
        .stream())
      .map(obj -> new StringBuilder("RESOURCE_ACCOUT_ROLE_")
        .append(obj.toString())
        .toString());
  }

  private Flux<String> groups(final Jwt jwt) {
    return Flux.fromStream(Optional
        .ofNullable(jwt.getClaims()
          .get("groups"))
        .filter(obj -> obj instanceof Collection)
        .map(obj -> (Collection) obj)
        .orElse(Collections.emptyList())
        .stream())
      .map(obj -> new StringBuilder("GROUP_")
        .append(obj.toString())
        .toString());
  }
}
