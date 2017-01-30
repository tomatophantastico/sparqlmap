package org.aksw.sparqlmap.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;


@RunWith(value = Parameterized.class)
public class SparqlMapMongoTest {
  
  SparqlMap sparqlMap;
  
  
  static File baseDir = new File("./src/test/resources/mongo-test/");
  
  private String testname;
  private String datasetname;
  private String query;
  private Map<String,String> jsons;
  private Model mapping;
  
  
  public SparqlMapMongoTest(String testname, String datasetname, String query, Map<String, String> jsons, Model mapping) {
    super();
    this.testname = testname;
    this.datasetname = datasetname;
    this.query = query;
    this.jsons = jsons;
    this.mapping = mapping;
  }

  /**
   * Constructs the data for the paramterized test
   * 
   * @return
   */
  @Parameters(name="{0}")
  public static Iterable<Object[]> initTestSuite(){
    List<Object[]> tests = Lists.newArrayList();
    
    
    List<File> datasetdirs = Arrays.asList(baseDir.listFiles(new FileFilter() {
      
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory() && pathname.exists();
      }
    }));
    
    for(File datasetdir: datasetdirs){
      // get the dataset name
      String datasetname = datasetdir.getName();
      
      
  
  
      List<File> queryfiles = Lists.newArrayList(new File(datasetdir,"queries").listFiles(new FilenameFilter() {
        
        @Override
        public boolean accept(File dir, String name) {
          // TODO Auto-generated method stub
          return name.endsWith(".sq");
        }
      }));
      
      
      
      //determine the data files
      Map<String,String> collections = Maps.newHashMap();
      List<File> jsonFiles = Lists.newArrayList(datasetdir.listFiles(new FilenameFilter() {
        
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".json");
        }
      }));
      for(File jsonFile: jsonFiles){
        try {
          String collJson = Files.toString(jsonFile, Charsets.UTF_8);
          //some cleaning
          collJson = collJson.replace(System.getProperty("line.separator"), " ");
          collections.put(jsonFile.getName(),collJson );
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
      
      
      // construct the test
      for(File querFile : queryfiles){
        try {
          String query = Files.toString(querFile, Charsets.UTF_8);
          String queryname = querFile.getName();
          
          List<Object> test = Lists.newArrayList(
              datasetname + "/" + queryname,
              "sparqlmaptest-" + datasetname,
              query,
              collections,
              RDFDataMgr.loadModel(new File(datasetdir,"mapping.ttl").getAbsolutePath(),Lang.TURTLE)
              );
          tests.add(test.toArray());
  
          
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
  
        
        
      
     
      
    }
    
    
    
    return tests;
    
    
  }

  public void dbSetup(){
    String mongodbportstring = System.getenv("MONGODB_PORT");
    String monogohost = mongodbportstring.substring(6, mongodbportstring.indexOf(':',6));
    String mongoport =  mongodbportstring.substring(mongodbportstring.indexOf(':',6)+1);
    
    try(MongoClient mongoClient = new MongoClient(monogohost, Integer.parseInt(mongoport))){
      
      List<String> dbnames = Lists.newArrayList(mongoClient.listDatabaseNames());
      if(dbnames.contains(datasetname)){
        mongoClient.dropDatabase(datasetname);
  
      }
      MongoDatabase mongodb = mongoClient.getDatabase(datasetname);
      for(String collName : jsons.keySet()){
        String colljson = jsons.get(collName);
        mongodb.createCollection(collName);
        MongoCollection<Document> dbcoll = mongodb.getCollection(collName);
        BasicDBList coll =  (BasicDBList) JSON.parse(colljson);
        for(Object object :coll){
          dbcoll.insertOne(Document.parse(object.toString()));
        }
      }
      
    }
    
    
    
    
    
  }
  
  @Before
  public void setup(){
    dbSetup();
    
       
    
  }

  @Test
  public void test() {
  
  }

}
