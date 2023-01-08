package org.ledgerservice.shared.security.rest.keycloak;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UmaRealmAccessRolesClaimConverterTest {

  private UmaRealmAccessRolesClaimConverter converter;

  @BeforeEach
  public void before() {
    converter = new UmaRealmAccessRolesClaimConverter(CustomClaimNames.CLAIM_REALM_ACCESS);
  }

  @Test
  public void shouldReturnEmptyClaimsForEmptyMap() {
    assertThat(converter.convert(emptyMap()))
        .isEqualTo(UmaRealmAccessRolesClaim.builder()
            .roles(emptySet())
            .build());
  }

  @Test
  public void shouldReturnClaimsForPopulatedMap() {
    assertThat(converter.convert(singletonMap("realm_access",
            singletonMap("roles", singletonList("value")))))
        .isEqualTo(UmaRealmAccessRolesClaim.builder()
            .roles(singleton("value"))
            .build());
  }
}
