package org.aksw.sparqlmap.web.dto;

import org.aksw.sparqlmap.core.schema.LogicalTable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public abstract class ColumnMixin {
  
  @JsonIgnore public abstract LogicalTable getTable();


}
