package org.aksw.sparqlmap.web.dto;

import org.aksw.sparqlmap.core.schema.LogicalSchema;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property = "path")
public abstract class TableMixin {
  
  
  @JsonIgnore public abstract LogicalSchema getSchema();

}
