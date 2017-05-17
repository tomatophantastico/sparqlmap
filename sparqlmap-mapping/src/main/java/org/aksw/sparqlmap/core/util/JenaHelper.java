package org.aksw.sparqlmap.core.util;


import org.apache.jena.graph.Node;
import static org.apache.jena.riot.WebContent.* ;
import static org.apache.jena.sparql.resultset.ResultsFormat.*;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.resultset.ResultsFormat;

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
  
  public static ResultsFormat contentTypeToSelectResultFormats(ContentType ct){
    String cts = ct.getContentType();
    switch (cts) {
    case contentTypeResultsXML:
      return FMT_RS_XML;
    case contentTypeResultsJSON:
      return FMT_RS_JSON;
    case contentTypeResultsThrift:
      return FMT_RS_THRIFT;
    case contentTypeTextCSV:
      return FMT_RS_CSV;
    case contentTypeTextTSV:
      return FMT_RS_TSV;
    case contentTypeSSE:
      return FMT_RS_SSE;
    case contentTypeResultsBIO:
      return FMT_RS_BIO;
    default:
      return null;
    }
    
    
    
  }

}
