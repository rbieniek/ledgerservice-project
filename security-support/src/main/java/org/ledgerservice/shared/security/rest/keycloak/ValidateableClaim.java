package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Collection;

public interface ValidateableClaim {
  boolean contains(String value);

  default boolean containsAny(Collection<String> values) {
    return values.stream()
        .map(value -> contains(value))
        .reduce((a,b) -> a || b)
        .orElse(false);
  }
}
