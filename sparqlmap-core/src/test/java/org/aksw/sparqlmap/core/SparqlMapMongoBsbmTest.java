package org.aksw.sparqlmap.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.metamodel.mongodb.mongo3.MongoDbDataContext;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;


public class SparqlMapMongoBsbmTest {
  
  
  private SparqlMap getSparqlMap(){
 

    MongoClient mc = new MongoClient("192.168.99.100:27018");

    MongoDatabase mdb = mc.getDatabase("bsbm");

    // assertTrue(Lists.newArrayList(mdb.listCollectionNames().iterator()).size()>0);

    MongoDbDataContext dc = new MongoDbDataContext(mdb);
    
    
    SparqlMap sm = SparqlMapBuilder.newSparqlMap(null).connectTo(dc)
        .mappedBy("./src/test/resources/mongo-test/bsbm/mapping.ttl").create();
    
    return sm;

  }
  
  
  
  
  @Test
  public void dumpTest(){
    
    
    SparqlMap sm = getSparqlMap();

    DatasetGraph dsg = sm.getDumpExecution().dumpDatasetGraph();

    assertTrue(dsg.size() > 0);

  }
  
  
  @Test
  public void   queryPerson(){
    ResultSet rs = getSparqlMap().execute("Select * {  ?person <http://xmlns.com/foaf/0.1/name> ?name }").execSelect();
    int i = 0;
    while(rs.hasNext()){
      i++;
      assertNotNull(rs.next().get("name"));
    }
    
    assertTrue("More than zero results", i>0);
  

    
  }
  

}
