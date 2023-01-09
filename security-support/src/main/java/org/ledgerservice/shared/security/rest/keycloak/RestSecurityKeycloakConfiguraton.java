package org.ledgerservice.shared.security.rest.keycloak;

import org.ledgerservice.spring.support.LocalTestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = LocalTestConfiguration.class))
public class RestSecurityKeycloakConfiguraton {

}
