package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UmaRealmAccessRolesClaim implements ValidateableClaim {
  private Set<String> roles;

  @Override
  public boolean contains(final String value) {
    return roles.contains(value);
  }
}
