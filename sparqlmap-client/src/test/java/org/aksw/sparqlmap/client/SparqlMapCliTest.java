package org.aksw.sparqlmap.client;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlmap.ResultHelper;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
  
  

  @Test
  public void testGenerateDirectMapping(){
    
    CliTestWrapper wrapper = new CliTestWrapper();
    
    List<String> params = Lists.newArrayList(PARAMS);
    params.add("--action=directmapping");
    wrapper.test(params);


    ResultHelper.assertModelAreEqual(wrapper.outputAsModel(Lang.TTL), RDFDataMgr.loadModel(TEST_LOCATION + "dm.ttl"));

  }
  
 
  
  

}
