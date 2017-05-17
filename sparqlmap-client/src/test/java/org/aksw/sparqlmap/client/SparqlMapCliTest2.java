package org.aksw.sparqlmap.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.aksw.sparqlmap.cli.SparqlMapStarter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

/**
 * Same as in SparqlMapCliTest but setting and unsetting of System.out seems to be a bit problematic
 * @author joerg
 *
 */
public class SparqlMapCliTest2 {
  
  @Test
  public void testMapping(){
    
    PrintStream sysout = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream dmout = new PrintStream(baos);
    System.setOut(dmout);
    
    String[] params = SparqlMapCliTest.params("--action=dump", "-f=" + SparqlMapCliTest.TEST_LOCATION + "mapping.ttl");
    SparqlMapStarter.main(params);
    dmout.flush();
    System.setOut(sysout);

    Model dump = ModelFactory.createDefaultModel();
    
    RDFDataMgr.read(dump, new ByteArrayInputStream(baos.toByteArray()), Lang.TTL);
    
    Assert.assertTrue(dump.containsResource(ResourceFactory.createResource("http://example.com/mappingtest/instance/Person/1")));
    
    

  }

}
