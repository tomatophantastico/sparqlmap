package org.aksw.sparqlmap.core.othertest;

import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.metamodel.DataContext;
import org.eobjects.metamodel.access.AccessDataContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class Pfarrerbuchtest {
  
  private static String testcaselocation = "../sparqlmap-test/src/main/resources/pfarrerbuch/";

  
  private static SparqlMap sm;
  
  @Before
  public void setup(){
    DataContext dc = new AccessDataContext(testcaselocation+"dataset.accdb");
    
    sm = SparqlMapBuilder.newSparqlMap("http://pfarrerbuch/base/").connectTo(dc).mappedBy(testcaselocation + "mapping.ttl").create();
  }
  
  @Test
  public void testMapping(){
    sm.execute("SELECT ?name {?s ?p ?name} order by ?s limit 1").execSelect().forEachRemaining(r -> 
    Assert.assertEquals("Abel, Heinrich Friedrich", r.get("name").asLiteral().getString()));
    
  }
  
  
  
  
  
  

}
