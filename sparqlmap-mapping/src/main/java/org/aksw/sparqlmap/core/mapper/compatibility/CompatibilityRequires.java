package org.aksw.sparqlmap.core.mapper.compatibility;

import org.aksw.sparqlmap.core.schema.LogicalColumn;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompatibilityRequires {
  
  
  private LogicalColumn column;
  
  private String value;
  private LogicalColumn valueColumn;

}
