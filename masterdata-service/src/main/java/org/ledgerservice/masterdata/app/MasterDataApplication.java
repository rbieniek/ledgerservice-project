package org.ledgerservice.masterdata.app;

import org.ledgerservice.masterdata.persistence.PersistenceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({PersistenceConfiguration.class})
public class MasterDataApplication {

  public static void main(String[] args) {
    SpringApplication.run(MasterDataApplication.class, args);
  }

}
