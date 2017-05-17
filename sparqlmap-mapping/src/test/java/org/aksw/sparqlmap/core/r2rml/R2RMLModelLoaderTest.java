package org.aksw.sparqlmap.core.r2rml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class R2RMLModelLoaderTest {
  
  static Model r2rml;
  static Model smap;
  static Model bsbm_all;
  static Model bsbm;
  static Model r2rml_test_9;
  
  {
    r2rml = ModelFactory.createDefaultModel();
    r2rml.read(ClassLoader.getSystemResourceAsStream("./vocabularies/r2rml.ttl"),null,"TTL");
    
    smap = ModelFactory.createDefaultModel();
    smap.read(ClassLoader.getSystemResourceAsStream("./vocabularies/smap.ttl"),null,"TTL");
    
    bsbm_all = ModelFactory.createDefaultModel();
    RDFDataMgr.read(bsbm_all, ClassLoader.getSystemResourceAsStream("./query-test/bsbm-all/mapping.ttl"), null, Lang.TURTLE);

    bsbm = ModelFactory.createDefaultModel();
    RDFDataMgr.read(bsbm, ClassLoader.getSystemResourceAsStream("./query-test/bsbm/mapping.ttl"), null, Lang.TURTLE);

    r2rml_test_9 = ModelFactory.createDefaultModel();
    RDFDataMgr.read(r2rml_test_9, ClassLoader.getSystemResourceAsStream("./query-test/r2rml_tc009/mapping.ttl"), null, Lang.TURTLE);

    
  }
  
  
  

  @Test
  public void testLoaderWithBSBMFull() {
    
    String bsbmPrefix = "http://aksw.org/Projects/sparqlmap/mappings/bsbm-test/";
    
   

    
    R2RMLMapping mapping =  R2RMLModelLoader.loadModel(bsbm_all, r2rml, smap, "http://localhost/default/");
    
    assertTrue( mapping.getQuadMaps().stream().map(QuadMap::getTriplesMapUri).distinct().count() ==13);
    
    //check some maps out
    
    Collection<QuadMap> pt_hierarchies = mapping.getQuadMaps().stream().filter(qm -> qm.getTriplesMapUri().equals(bsbmPrefix + "ProductTypeHierarchy_ref")).collect(Collectors.toList()); 
    assertNotNull(pt_hierarchies);
    assertTrue(pt_hierarchies.size()==1);
    QuadMap pt_hierarchy = pt_hierarchies.iterator().next();
    assertTrue(pt_hierarchy.getLogicalTable().getTablename().equals("producttype"));
    
    //checks if the prefixing worked
    assertTrue(((TermMapTemplate)pt_hierarchy.getSubject()).getTemplate().get(0).getPrefix().startsWith("http://"));
    assertTrue(((TermMapConstant)pt_hierarchy.getPredicate()).getConstantIRI().endsWith("subProductType"));
    assertNotNull(((TermMapReferencing)pt_hierarchy.getObject()).getParent());
    
    

    
    
  }

}
