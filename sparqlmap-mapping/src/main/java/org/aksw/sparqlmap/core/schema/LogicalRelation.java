package org.aksw.sparqlmap.core.schema;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class LogicalRelation {
  
  private LogicalTable primary;
  private LogicalTable foreign;
  
  /**
   * contains pairs of colums, which form the key condition of an evaluating equi-join
   */
  @Singular private Collection<LogicalColumn[]> ons;

}
