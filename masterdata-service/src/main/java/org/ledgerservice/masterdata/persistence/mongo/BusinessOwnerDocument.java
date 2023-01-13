package org.ledgerservice.masterdata.persistence.mongo;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessOwnerDocument {
  @Id
  private String id;

  private String name;

  private Set<String> readAccessGroups;

  private Set<String> writeAccessGroups;
}
