package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.CollectionUtils;

public class RemappingClaimTypeConverter implements Converter<Map<String, Object>, Map<String, Object>> {

  private final Map<String, Converter<Object, ?>> claimTypeConverters;

  public RemappingClaimTypeConverter(final Map<String, Converter<Object, ?>> claimTypeConverters) {
    this.claimTypeConverters = Collections.unmodifiableMap(new LinkedHashMap<>(claimTypeConverters));
  }

  @Override
  public Map<String, Object> convert(final Map<String, Object> claims) {
    if (CollectionUtils.isEmpty(claims)) {
      return claims;
    }
    Map<String, Object> result = new HashMap<>(claims);
    this.claimTypeConverters.forEach((claimName, typeConverter) -> {
      if (claims.containsKey(claimName)) {
        Object claim = claims.get(claimName);
        Object mappedClaim = typeConverter.convert(claim);
        if (mappedClaim != null) {
          if (typeConverter instanceof ClaimConverterBase remappingConvert) {
            result.put(remappingConvert.getTargetClaimName(), mappedClaim);
          } else {
            result.put(claimName, mappedClaim);
          }
        }
      }
    });
    return result;
  }
}
