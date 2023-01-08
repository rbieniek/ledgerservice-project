package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public abstract class ClaimConverterBase {

  public abstract String getTargetClaimName();

  protected Set<String> deconstructStructuredClaim(final Object source, final String pathName) {
    final List<String> parts = new LinkedList<>(Arrays
        .stream(StringUtils.split(pathName, '.'))
        .filter(path -> StringUtils.isNotBlank(path))
        .toList());

    if(parts.isEmpty()) {
      return Optional.ofNullable(source)
          .filter(value -> value instanceof Collection)
          .map(value -> ((Collection<Object>) value)
              .stream()
              .map(entry -> entry.toString())
              .collect(Collectors.toSet()))
          .orElse(Collections.<String>emptySet());
    }

    return deconstructInternal(source, parts);
  }

  private Set<String> deconstructInternal(final Object source, final List<String> path) {
    final String part = path.remove(0);

    return Optional.ofNullable(source)
        .filter(obj -> obj instanceof Map)
        .map(obj -> (Map<String, Object>) obj)
        .map(map -> map.get(part))
        .map(obj -> {
          if (path.isEmpty()) {
            return Optional.ofNullable(obj)
                .filter(value -> value instanceof Collection)
                .map(value -> ((Collection<Object>) value)
                    .stream()
                    .map(entry -> entry.toString())
                    .collect(Collectors.toSet()))
                .orElse(Collections.<String>emptySet());
          } else {
            return Optional.ofNullable(obj)
                .filter(value -> value instanceof Map)
                .map(value -> deconstructInternal(value, path))
                .orElse(Collections.<String>emptySet());
          }
        })
        .orElseGet(() -> Collections.<String>emptySet());
  }
}
