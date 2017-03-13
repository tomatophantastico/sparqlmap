package org.aksw.sparqlmap.web.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property = "name")
public abstract class SchemaMixin {
  
  @JsonProperty abstract String getName();
}
