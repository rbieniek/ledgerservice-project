package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UmaResourceAccessRolesClaim implements ValidateableClaim {

  private AccountRolesClaim account;

  @Override
  public boolean contains(final String value) {
    return Optional.ofNullable(account)
        .map(v -> v.contains(value))
        .orElse(false);
  }

  @Data
  @Builder
  public static class AccountRolesClaim implements ValidateableClaim {

    private Set<String> roles;

    @Override
    public boolean contains(final String value) {
      return roles.contains(value);
    }
  }
}

