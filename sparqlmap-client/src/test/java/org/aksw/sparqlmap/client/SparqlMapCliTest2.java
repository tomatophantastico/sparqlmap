package org.aksw.sparqlmap.client;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Same as in SparqlMapCliTest but setting and unsetting of System.out seems to be a bit problematic
 * @author joerg
 *
 */
public class SparqlMapCliTest2 {
  
  @Test
  public void testMapping(){
    


    CliTestWrapper wrapper = new CliTestWrapper();

    List<String> params = Lists.newArrayList(SparqlMapCliTest.PARAMS);
    params.add("--action=directmapping");
    wrapper.test(params);


    Assert.assertTrue(wrapper.outputAsModel(Lang.TTL).containsResource(ResourceFactory.createResource("http://example.com/mappingtest/instance/Person/1")));
    
    

  }

}
