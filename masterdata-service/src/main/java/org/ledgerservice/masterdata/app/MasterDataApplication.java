package org.ledgerservice.masterdata.app;

import org.ledgerservice.masterdata.persistence.PersistenceConfiguration;
import org.ledgerservice.masterdata.rest.RestConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({PersistenceConfiguration.class, RestConfiguration.class})
public class MasterDataApplication {

  public static void main(String[] args) {
    SpringApplication.run(MasterDataApplication.class, args);
  }

}
