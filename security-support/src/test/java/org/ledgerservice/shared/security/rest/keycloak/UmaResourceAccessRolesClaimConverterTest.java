package org.ledgerservice.shared.security.rest.keycloak;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.ledgerservice.shared.security.rest.keycloak.UmaResourceAccessRolesClaim.AccountRolesClaim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UmaResourceAccessRolesClaimConverterTest {

  private UmaResourceAccessRolesClaimConverter converter;

  @BeforeEach
  public void before() {
    converter = new UmaResourceAccessRolesClaimConverter(CustomClaimNames.CLAIM_RESOURCE_ACCESS);
  }

  @Test
  public void shouldReturnEmptyClaimsForEmptyMap() {
    assertThat(converter.convert(emptyMap()))
        .isEqualTo(UmaResourceAccessRolesClaim.builder()
            .account(AccountRolesClaim.builder()
                .roles(emptySet())
                .build())
            .build());
  }

  @Test
  public void shouldReturnClaimsForPopulatedMap() {
    assertThat(converter.convert(singletonMap("resource_access",
            singletonMap("account",
                singletonMap("roles", singletonList("value"))))))
        .isEqualTo(UmaResourceAccessRolesClaim.builder()
            .account(AccountRolesClaim.builder()
                .roles(singleton("value"))
                .build())
            .build());
  }
}
