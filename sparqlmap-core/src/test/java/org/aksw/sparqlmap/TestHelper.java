package org.aksw.sparqlmap;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;

import org.aksw.sparqlmap.core.SparqlMap;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelper {
  
 

  
  
  private static Logger log = LoggerFactory.getLogger(TestHelper.class);
  
  public void executeAndCompareConstruct(SparqlMap sm, String sparqlConstruct, String resultmodelLocation) throws SQLException{
    Model expectedResult = ModelFactory.createDefaultModel();
    expectedResult.read(resultmodelLocation);
    Model result = sm.executeConstruct(sparqlConstruct);
    
    assertModelAreEqual(result, expectedResult);
  }
  
  static public void assertModelAreEqual( Model result, Model expectedresult) throws SQLException{
    
   
 
  
    StringBuffer models =new StringBuffer();
    
    models.append("Actual result is :\n");
    models.append("=============================");
    ByteArrayOutputStream actualResBos  = new ByteArrayOutputStream();
    RDFDataMgr.write(actualResBos, result,Lang.TURTLE);
    models.append(actualResBos);
    
    models.append("=======================\nExpected was: ");
    ByteArrayOutputStream expectedResBos  =new ByteArrayOutputStream();
    RDFDataMgr.write(expectedResBos, expectedresult,Lang.TURTLE);
    models.append(expectedResBos);
    models.append("=======================\nMissing in the sparqlmap result is: ");
    
    Model missingInActual = expectedresult.difference(result);
    ByteArrayOutputStream missingInActualResBos  =new ByteArrayOutputStream();
    RDFDataMgr.write(missingInActualResBos, missingInActual,Lang.TURTLE);
    models.append(missingInActualResBos);
    
    models.append("=======================\nThese triples were unexpected: ");
    Model missingInExpected = result.difference(expectedresult);
    ByteArrayOutputStream missingInExpectedResBos  =new ByteArrayOutputStream();
    RDFDataMgr.write(missingInExpectedResBos, missingInExpected,Lang.TURTLE);
    models.append(missingInExpectedResBos);
    models.append("=============================");

    
    //check if we have to deal with 
    
    assertTrue(models.toString(), result.isIsomorphicWith(expectedresult));
  
    
  }
  
 

  public static void assertResultSetsAreEqual(ResultSet result,
      ResultSet expectedRS) {
    result = ResultSetFactory.makeRewindable(result); 
    expectedRS = ResultSetFactory.makeRewindable(expectedRS);
    boolean isEqual  = ResultSetCompare.equalsByTerm(result, expectedRS);
    StringBuffer comparison = new StringBuffer();


      
    comparison.append("Actual result is :\n");
    comparison.append("=============================");
   
    comparison.append(ResultSetFormatter.asText(result));
 
    comparison.append("\nCount: "+ result.getRowNumber()+ "=======================\nExpected was: ");
   
    comparison.append(ResultSetFormatter.asText(expectedRS));
    comparison.append("\nCount:"+expectedRS.getRowNumber()+"=============================");
    
    
    log.debug(comparison.toString());
    assertTrue(comparison.toString(),isEqual);
    
  }

  public static void executeAndCompare(SparqlMap sm, String sparql,

      String tbdname, String queryname) throws SQLException {
    
      String tdbDir = "./build/tdbs/" + tbdname;
    
    
      
      File tdbDirFile = new File(tdbDir);
      Dataset refDs;
      if(!tdbDirFile.exists()){
        tdbDirFile.mkdirs();
        
        refDs = org.apache.jena.tdb.TDBFactory.createDataset(tdbDir);
        DatasetGraph refDsg = refDs.asDatasetGraph();
        
        DatasetGraph dump = sm.getDumpExecution().dumpDatasetGraph();
        Iterator<Quad> dumpIter = dump.find();
       
       
        while(dumpIter.hasNext()){
          Quad quad = dumpIter.next();
          refDsg.add(quad);
         
        }
        refDsg.close();
      }
      refDs = TDBFactory.createDataset(tdbDir);
       
      
      
      if(refDs.asDatasetGraph().isEmpty()){
        log.warn("Loaded empty dataset");
      }else{
        log.info("Loaded dataset of size:" + refDs.asDatasetGraph().size());
      }
      
      Query query = QueryFactory.create(sparql);
      
      if(query.isSelectType()){
        ResultSet expected = QueryExecutionFactory.create(query,refDs).execSelect();
        ResultSet acutal  = sm.executeSelect(sparql);
        
        assertResultSetsAreEqual(acutal, expected);
        
        
      }else if(query.isAskType()){
        boolean expected = QueryExecutionFactory.create(query,refDs).execAsk();
        boolean actual = sm.executeAsk(sparql);
        
        Assert.assertTrue(actual == expected);
        
        
      }else if (query.isConstructType()){
        //construct query
        assertModelAreEqual(
            sm.executeConstruct(sparql), 
            QueryExecutionFactory.create(query,refDs).execConstruct());
        
      }else {
        //must be describe
        assertModelAreEqual(
            sm.executeDescribe(sparql), 
            QueryExecutionFactory.create(query,refDs).execDescribe());
        
      }
  }
  
}
