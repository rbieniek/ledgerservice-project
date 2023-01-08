package org.ledgerservice.shared.security.rest.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class RolesClaimConverter extends ClaimConverterBase implements Converter<Object, GroupsClaim> {
  private final String propertyPath;

  @Override
  public GroupsClaim convert(final Object source) {
    final GroupsClaim groups = GroupsClaim.builder()
        .groups(deconstructStructuredClaim(source, propertyPath))
        .build();

    return groups;
  }

  @Override
  public String getTargetClaimName() {
    return CustomClaimNames.CLAIM_GROUPS;
  }
}
