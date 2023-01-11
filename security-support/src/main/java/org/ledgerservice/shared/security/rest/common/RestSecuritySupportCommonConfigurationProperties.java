package org.ledgerservice.shared.security.rest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = RestSecuritySupportCommonConfigurationProperties.PREFIX)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class RestSecuritySupportCommonConfigurationProperties {
  public static final String PREFIX = "app.rest.security.common";

  @NotNull
  @NotEmpty
  private String requiredRole;

  private Set<@NotBlank String> multiTenancyJwksUri;

  @NotNull
  @NotEmpty
  private String apiPath;
}
