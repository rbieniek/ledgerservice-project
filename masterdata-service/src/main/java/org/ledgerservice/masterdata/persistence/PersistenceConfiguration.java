package org.ledgerservice.masterdata.persistence;

import org.ledgerservice.masterdata.persistence.mongo.MongoPersistenceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MongoPersistenceConfiguration.class})
public class PersistenceConfiguration {

}
