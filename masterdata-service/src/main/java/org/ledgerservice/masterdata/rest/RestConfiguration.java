package org.ledgerservice.masterdata.rest;

import org.ledgerservice.spring.support.LocalTestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = LocalTestConfiguration.class))
public class RestConfiguration {

}
