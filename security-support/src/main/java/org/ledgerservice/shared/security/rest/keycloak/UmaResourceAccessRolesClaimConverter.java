package org.ledgerservice.shared.security.rest.keycloak;

import org.ledgerservice.shared.security.rest.keycloak.UmaResourceAccessRolesClaim.AccountRolesClaim;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class UmaResourceAccessRolesClaimConverter extends ClaimConverterBase implements
    Converter<Object, UmaResourceAccessRolesClaim> {

  private final String propertyPath;


  @Override
  public UmaResourceAccessRolesClaim convert(final Object source) {
    return UmaResourceAccessRolesClaim.builder()
        .account(AccountRolesClaim.builder()
            .roles(deconstructStructuredClaim(source, propertyPath))
            .build())
        .build();
  }

  @Override
  public String getTargetClaimName() {
    return CustomClaimNames.CLAIM_RESOURCE_ACCESS;
  }
}
