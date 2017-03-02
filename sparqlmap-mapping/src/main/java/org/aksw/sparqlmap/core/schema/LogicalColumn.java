package org.aksw.sparqlmap.core.schema;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(builderMethodName = "colBuilder")
@EqualsAndHashCode(exclude = "table")
public class LogicalColumn {
  
  private LogicalTable table;
 
  private String name;
  
  
  private Optional<String> xsdDataType; 
  

  
  public static LogicalColumnBuilder builder(LogicalTable ltab){
    return colBuilder().table(ltab);
  }
  
}
