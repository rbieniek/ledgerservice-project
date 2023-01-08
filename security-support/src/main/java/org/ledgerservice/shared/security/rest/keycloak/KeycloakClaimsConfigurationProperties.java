package org.ledgerservice.shared.security.rest.keycloak;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.keycloak.claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class KeycloakClaimsConfigurationProperties {

  private Map<String, @Valid SupportedClaims> clients;

  @NotNull
  @Valid
  private PropertyPath propertyPaths = PropertyPath.builder()
    .roles(CustomClaimNames.CLAIM_ROLES)
    .groups(CustomClaimNames.CLAIM_GROUPS)
    .realmAccess(CustomClaimNames.CLAIM_REALM_ACCESS)
    .resourceAccess(CustomClaimNames.CLAIM_RESOURCE_ACCESS)
    .build();

  @NotNull
  @Valid
  @Builder.Default
  private SupportedClaims defaultClaims = SupportedClaims.builder()
    .roles(true)
    .groups(true)
    .realmAccess(true)
    .resourceAccess(true)
    .build();

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SupportedClaims {

    @Builder.Default
    private boolean roles = true;

    @Builder.Default
    private boolean groups = true;

    @Builder.Default
    private boolean realmAccess = true;

    @Builder.Default
    private boolean resourceAccess = true;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PropertyPath {

    @NotNull
    @NotEmpty
    private String roles;

    @NotNull
    @NotEmpty
    private String groups;

    @NotNull
    @NotEmpty
    private String realmAccess;

    @NotNull
    @NotEmpty
    private String resourceAccess;

  }
}
