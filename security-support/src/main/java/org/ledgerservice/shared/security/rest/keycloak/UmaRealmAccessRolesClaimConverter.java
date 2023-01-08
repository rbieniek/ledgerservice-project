package org.ledgerservice.shared.security.rest.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class UmaRealmAccessRolesClaimConverter extends ClaimConverterBase implements
    Converter<Object, UmaRealmAccessRolesClaim> {

  private final String propertyPath;


  @Override
  public UmaRealmAccessRolesClaim convert(final Object source) {
    return UmaRealmAccessRolesClaim.builder()
        .roles(deconstructStructuredClaim(source, propertyPath))
        .build();
  }

  @Override
  public String getTargetClaimName() {
    return CustomClaimNames.CLAIM_REALM_ACCESS;
  }
}
