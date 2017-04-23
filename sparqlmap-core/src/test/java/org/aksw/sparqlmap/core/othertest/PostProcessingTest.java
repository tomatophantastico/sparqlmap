package org.aksw.sparqlmap.core.othertest;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;

import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.junit.Test;

public class PostProcessingTest {
 
    
    CsvConfiguration csvConf = new CsvConfiguration(1,"UTF-8",',','"','\\',false,false);

    String testcaselocation = "../sparqlmap-test/src/main/resources/conditional/";

    @Test
    public void testDump() throws SQLException{
      
      
      
      DataContext csvContext = new CsvDataContext(new File(testcaselocation,"dataset.csv"), csvConf);
      
      
      SparqlMap sm = SparqlMapBuilder.newSparqlMap(null).connectTo(csvContext).mappedBy(testcaselocation+"mapping.ttl").create();
      Model result = ModelFactory.createModelForGraph(sm.getDumpExecution().dumpDatasetGraph().getDefaultGraph());
      try(FileOutputStream fos = new FileOutputStream(testcaselocation + "result.ttl")){
        RDFDataMgr.write(fos, result, Lang.NTRIPLES);
      } catch (Exception e) {
        e.printStackTrace();
      }
      Model exptected  = RDFDataMgr.loadModel(testcaselocation + "expected.ttl");
      TestHelper.assertModelAreEqual(RDFDataMgr.loadModel(testcaselocation + "result.ttl"),exptected);
      
      
      
      
      
      
    }
    
  }



