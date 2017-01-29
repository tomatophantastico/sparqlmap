package org.aksw.sparqlmap.core.util;


import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public class JenaHelper {
  
  public static Node getField(Quad quad, QuadPosition field){
    switch (field) {
    case graph:
      return quad.getGraph();
    case subject:
      return quad.getSubject();
    case predicate:
      return quad.getPredicate();
    case object:
      return quad.getObject();
    default:
      return null;
    }
    
  }

}
