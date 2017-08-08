package org.aksw.sparqlmap.client;

import com.aol.cyclops.data.MutableInt;
import com.google.common.collect.Lists;
import org.aksw.sparqlmap.cli.SparqlMapStarter;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Same as in SparqlMapCliTest but setting and unsetting of System.out seems to be a bit problematic
 * @author joerg
 *
 */
public class SparqlMapCliWeb {
  
  @Test
  public void testWebServer(){
    

    Executor exec = Executors.newSingleThreadExecutor();
    
    exec.execute(new Runnable() {
      @Override
      public void run() {
        List<String> params = Lists.newArrayList(SparqlMapCliTest.PARAMS);
        params.add("--action=web");
        params.add("-f=" + SparqlMapCliTest.TEST_LOCATION + "mapping.ttl");
        SparqlMapStarter.main(params.toArray(new String[0]));
      }
    });
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    
    Boolean askresult = null;
    Model constructResult = null;
    MutableInt selectCount = new MutableInt(-1);
    Model describeResult = null;
    Exception invalidQuery = null;
    
    
    
    String sparqlService = "http://localhost:8090/api/ROOT/sparql";
    
    for(int i = 0; i<10;i++){
      try {
        if (selectCount.getAsInt() < 0){
          QueryExecutionFactory.sparqlService(sparqlService, "SELECT (count(?s) as ?count) {?s ?p ?o}").execSelect().forEachRemaining(row -> selectCount.set(row.get("count").asLiteral().getInt()));
        }
        if (askresult == null){
          askresult= QueryExecutionFactory.sparqlService(sparqlService, "ASK {?s ?p ?o}").execAsk();
        }
        if(describeResult == null){
          describeResult = QueryExecutionFactory.sparqlService(sparqlService, "DESCRIBE ?s {?s a ?class}").execDescribe();
        }
        if(constructResult == null){
          constructResult = QueryExecutionFactory.sparqlService(sparqlService, "Construct {?s ?p ?o} WHERE {?s ?p ?o}").execConstruct();
        }
        if(invalidQuery == null){
          try{
            new URL(sparqlService + "?query=" + URLEncoder.encode("Select * { invalidpattern}","UTF-8")).openConnection().getContent().toString();
          }catch (Exception e) {
            invalidQuery = e;
          }
        }
        
        
        

        break;
      } catch (Exception e1) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    Assert.assertNotNull(askresult);
    Assert.assertTrue(askresult);
    Assert.assertEquals(3, selectCount.getAsInt());
    Assert.assertEquals(3, describeResult.size());
    Assert.assertEquals(3, constructResult.size());
    Assert.assertTrue(invalidQuery.getMessage().contains("500"));


    


    

  }

}
