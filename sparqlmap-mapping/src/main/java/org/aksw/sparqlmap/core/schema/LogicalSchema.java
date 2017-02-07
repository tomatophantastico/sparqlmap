package org.aksw.sparqlmap.core.schema;

import java.util.Collection;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(builderMethodName = "lschemabuilder")
@EqualsAndHashCode(exclude = {"tables", "relations"})
public class LogicalSchema {
  private String name;
  
  private Collection<LogicalTable> tables;
  
  private Collection<LogicalRelation> relations;
  
  
  public static LogicalSchemaBuilder builder(String name){
    return lschemabuilder().name(name);
  }
  
  public static final LogicalSchema NULLSCHEMA = builder(null).build();
  public static final LogicalSchema DEFAULTSCHEMA = builder("").build();


  public Object getPath() {
    return name;
  }
  
  
  
  
}
