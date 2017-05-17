package org.aksw.sparqlmap.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.cli.SparqlMapCli;
import org.aksw.sparqlmap.cli.SparqlMapStarter;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;
/**
 * This test checks if the parameters are processed correctly and the output is as expected.
 * Not so much about correctness, but more about usage
 * 
 * @author joerg
 *
 */
public class SparqlMapCliTest {
  
  public static String TEST_LOCATION = "../sparqlmap-test/src/main/resources/clienttest/";
  
  public static List<String> PARAMS = Collections.unmodifiableList(Lists.newArrayList(
      "--ds.url=" +TEST_LOCATION  + "dataset.csv",
      "--ds.separatorChar=+",
      "--ds.columnNameLineNumber=2",
      "-bi=http://localhost/baseiri/",
      "--ds.quoteChar=%",
      "--dmBaseUriPrefix=http://localhost/baseiri/",
      "--ds.type=CSV"
      ));
  
  
  public static String[] params(String...params){
    List<String> result = Lists.newArrayList(params);
    
    result.addAll(PARAMS);
    return result.toArray(new String[0]);
    
  }
  
  @Test
  public void testGenerateDirectMapping(){
    
    PrintStream sysout = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream dmout = new PrintStream(baos);
    System.setOut(dmout);
    
    
    SparqlMapStarter.main(params("--action=directmapping"));
    dmout.flush();
    System.setOut(sysout);

    Model dm_model = ModelFactory.createDefaultModel();
    
    RDFDataMgr.read(dm_model, new ByteArrayInputStream(baos.toByteArray()), Lang.TTL);

    TestHelper.assertModelAreEqual(dm_model, RDFDataMgr.loadModel(TEST_LOCATION + "dm.ttl"));
  }
  
 
  
  

}
