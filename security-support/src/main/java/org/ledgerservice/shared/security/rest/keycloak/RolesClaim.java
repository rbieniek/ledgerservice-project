package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolesClaim implements ValidateableClaim {

  private Set<String> groups;

  @Override
  public boolean contains(final String value) {
    return groups.contains(value);
  }
}
