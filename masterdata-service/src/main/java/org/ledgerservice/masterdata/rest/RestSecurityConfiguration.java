package org.ledgerservice.masterdata.rest;


import org.ledgerservice.shared.security.rest.SecuritySupportConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@Configuration
@Import(SecuritySupportConfiguration.class)
public class RestSecurityConfiguration {

}
