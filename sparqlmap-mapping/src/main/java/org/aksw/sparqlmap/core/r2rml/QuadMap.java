package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.aksw.sparqlmap.core.util.QuadPosition;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class QuadMap {
  
  private String triplesMapUri;
  private TermMap subject;
  private TermMap predicate;
  private TermMap object;
  private TermMap graph;
  private LogicalTable logicalTable;
  
  public TermMap get(QuadPosition pos){
    switch (pos) {
    case graph:
      return getGraph();
    case subject:
      return getSubject();
    case predicate:
      return getPredicate();
    case object:
      return getObject();
    default:
      return null;
    }
    
  }
  
  
  public List<TermMap> getTermMaps(){
    return Lists.newArrayList(graph,subject,predicate,object);
  }

  public Collection<LogicalColumn> getCols(){
    return Stream.of(QuadPosition.values())
        .map(pos -> get(pos))
        .flatMap(tm -> tm.getColumns().stream())
        .collect(Collectors.toList());
  }
  
  
  public static QuadMap NULLQUADMAP = QuadMap.builder().graph(TermMap.NULLTERMMAP).subject(TermMap.NULLTERMMAP).predicate(TermMap.NULLTERMMAP).object(TermMap.NULLTERMMAP).triplesMapUri(SMAP.NULLQUADMAPSTRING).logicalTable(LogicalTable.NULLTABLE).build(); 
  
  
}
