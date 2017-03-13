package org.aksw.sparqlmap.core.othertest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.junit.Test;

public class DataCubeTest {
  
  
  CsvConfiguration csvConf = new CsvConfiguration(3,"UTF-8",';','"','\\',false,false);

  String testcaselocation = "../sparqlmap-test/src/main/resources/datacube/";

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
  
  
  @Test
  public void testCsvStructure(){
    
    
    
    DataContext csvContext = new CsvDataContext(new File("../sparqlmap-test/src/main/resources/datacube/dataset.csv"), csvConf);
    
    Arrays.stream(csvContext.getDefaultSchema().getTables()).forEach(table->
      Arrays.stream(table.getColumns()).forEach(col -> System.out.println(col.getName()))
        );
    
  }
  
  
  
}
