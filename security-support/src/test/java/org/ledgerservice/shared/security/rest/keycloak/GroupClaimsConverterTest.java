package org.ledgerservice.shared.security.rest.keycloak;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupClaimsConverterTest {

  private GroupsClaimConverter converter;

  @BeforeEach
  public void before() {
    converter = new GroupsClaimConverter(CustomClaimNames.CLAIM_GROUPS);
  }

  @Test
  public void shouldReturnEmptyClaimsForEmptyMap() {
    assertThat(converter.convert(emptyMap()))
        .isEqualTo(GroupsClaim.builder()
            .groups(emptySet())
            .build());
  }

  @Test
  public void shouldReturnClaimsForPopulatedMap() {
    assertThat(converter.convert(singletonMap("groups",
            singletonList("value"))))
        .isEqualTo(GroupsClaim.builder()
            .groups(singleton("value"))
            .build());
  }
}
