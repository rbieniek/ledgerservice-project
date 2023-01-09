package org.ledgerservice.shared.security.rest.common;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.rest.security.common")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class RestSecuritySupportCommonConfigurationProperties {
  @NotNull
  @NotEmpty
  private String requiredRole;

  @NotNull
  @NotEmpty
  private Set<@NotBlank String> multiTenancyJwksUri;
}
