package org.ledgerservice.shared.security.rest.keycloak;

import de.porsche.oso.dp.spring.support.LocalTestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = LocalTestConfiguration.class))
@ConfigurationPropertiesScan
public class KeycloakClaimsConfiguration {

  @Configuration
  @ConditionalOnWebApplication(type = Type.SERVLET)
  public static class WebMvcConfiguration {
    @Bean(name = "userClaimsValidator")
    public CurrentAuthenticationClaimsValidator currentAuthenticationClaimsValidator() {
      return new CurrentAuthenticationClaimsValidator();
    }
  }

  @Configuration
  @ConditionalOnWebApplication(type = Type.REACTIVE)
  public static class ReactiveWebConfiguration {
    @Bean(name = "userClaimsValidator")
    public ReactiveCurrentAuthenticationClaimsValidator currentAuthenticationClaimsValidator() {
      return new ReactiveCurrentAuthenticationClaimsValidator();
    }
  }
}
