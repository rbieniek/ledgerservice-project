package org.ledgerservice.shared.security.rest;

import org.ledgerservice.shared.security.rest.common.RestSecuritySupportCommonConfiguration;
import org.ledgerservice.shared.security.rest.keycloak.RestSecurityKeycloakConfiguraton;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RestSecurityKeycloakConfiguraton.class, RestSecuritySupportCommonConfiguration.class})
public class SecuritySupportConfiguration {
}
