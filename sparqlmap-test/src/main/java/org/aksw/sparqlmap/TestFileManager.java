package org.aksw.sparqlmap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.PatternFilenameFilter;

import lombok.Data;


/**
 * Helper class to read the test case data.
 * @author joerg
 *
 */
public class TestFileManager {
  
  
  private static final Logger log = LoggerFactory.getLogger(TestFileManager.class);
  
  private static String defaultLocation = "../sparqlmap-test/src/main/resources/";
  
  private String location;
  
  
  /**
   * use this from Sparqlmap subprojects
   */
  public TestFileManager() {
    this(defaultLocation);
  }
  
  
  
  
  
  
  /**
   * use this constructor to explicitly set the location of the folder
   * @param location
   */
  public TestFileManager(String location) {
    
    this.r2rmltest = loadR2RMLTestcases(new File( location,"r2rmltest"));
    this.location = location;

 
  }
  
  







  private Multimap<String, R2RMLTestParameter> loadR2RMLTestcases(File file) {
    Multimap<String, R2RMLTestParameter> r2rmltcs = ArrayListMultimap.create();
    Lists.newArrayList(file.listFiles()).stream().filter(subfile -> subfile.isDirectory()).forEach(folder -> 
      r2rmltcs.putAll( folder.getName(),  loadFromFolder(folder, folder.getName()))
    );
    

    
    return r2rmltcs;
  }


  private Multimap<String,R2RMLTestParameter> r2rmltest;
  
  

  
  public Multimap<String, R2RMLTestParameter> getR2rmltest() {
    return r2rmltest;
  }
  
  
  public static Collection<R2RMLTestParameter> loadFromFolder(File tcFolder,String dbname) {
    Collection<R2RMLTestParameter> testCases = Lists.newArrayList();

    try {
      log.info("Reading testcases from folder: " + tcFolder);
      for(File folder:tcFolder.listFiles()){
        if(folder.isDirectory()&&!folder.isHidden()){ 

        Model manifest = ModelFactory.createDefaultModel();
        manifest.read(new FileInputStream(new File(folder.getAbsolutePath()+"/manifest.ttl")), null,"TTL");
        
        
        
        
        // get the direct mapping test cases
        
        org.apache.jena.query.ResultSet dmRS = 
            QueryExecutionFactory.create(QueryFactory.create("PREFIX test: <http://www.w3.org/2006/03/test-description#> \n" + 
                "PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" + 
                "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#> " +
                "SELECT * WHERE {\n " +
                "   ?tc a rdb2rdftest:DirectMapping ; \n" + 
                " dcterms:title ?title ; \n" + 
                " dcterms:identifier ?identifier ;\n" + 
                " test:purpose ?purpose ;\n" + 
                " test:specificationReference ?reference ;\n" + 
                " test:reviewStatus ?reviewStatus ;\n" + 
                " rdb2rdftest:hasExpectedOutput ?expectedOutput ;\n" + 
                " rdb2rdftest:database ?db ;\n" +
                " rdb2rdftest:output ?outfname .\n" + 
                "   ?db rdb2rdftest:sqlScriptFile ?dbfile .\n" + 

                " } "),manifest).execSelect();
        
        while(dmRS.hasNext()){
          Binding bind = dmRS.nextBinding();
          String title = bind.get(Var.alloc("title")).getLiteral().toString();
          String identifier = bind.get(Var.alloc("identifier")).getLiteral().toString();
          String purpose = bind.get(Var.alloc("purpose")).getLiteral().toString();
          String reference = bind.get(Var.alloc("reference")).getLiteral().toString();
          String expectedOutput = bind.get(Var.alloc("expectedOutput")).getLiteral().toString();
          String outfname = bind.get(Var.alloc("outfname")).getLiteral().toString();
          Optional<String> dbnameOpt = Optional.of(bind.get(Var.alloc("db")).getURI());
          String dbFileName = bind.get(Var.alloc("dbfile")).getLiteral().toString();
          
          
          R2RMLTestParameter param = R2RMLTestParameter.builder()
              .testCaseName(identifier)
              .dbname(dbnameOpt.orElse(dbname))
              .dbFileLocation(makeAbsolute(folder, dbFileName))
              .outputLocation(getFileOutName(folder, outfname))
              .referenceOutput(makeAbsolute(folder, outfname))
              .r2rmlLocation(makeAbsolute(folder, "dm_r2rml.ttl"))
              .createDM(true).build();
          
          testCases.add(param);
        }
        
        // get the regular test cases
        
        
        org.apache.jena.query.ResultSet r2rRs = 
            QueryExecutionFactory.create(QueryFactory.create("PREFIX test: <http://www.w3.org/2006/03/test-description#> \n" + 
                "PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" + 
                "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#> " +
                "SELECT * WHERE {\n " +
                "   ?tc a rdb2rdftest:R2RML ; \n" + 
                " dcterms:title ?title ; \n" + 
                " dcterms:identifier ?identifier ;\n" + 
                " test:purpose ?purpose ;\n" + 
                " test:specificationReference ?reference ;\n" + 
                " test:reviewStatus ?reviewStatus ;\n" + 
                " rdb2rdftest:hasExpectedOutput ?expectedOutput ;\n" + 
                " rdb2rdftest:database ?db ;\n" + 
                " rdb2rdftest:output ?outfname ;\n" + 
                " rdb2rdftest:mappingDocument ?mappingfname .\n" + 
                "   ?db rdb2rdftest:sqlScriptFile ?dbfile .\n" + 

                " } "),manifest).execSelect();
        
        while(r2rRs.hasNext()){       
          Binding bind = r2rRs.nextBinding();
          String title = bind.get(Var.alloc("title")).getLiteral().toString();
          String identifier = bind.get(Var.alloc("identifier")).getLiteral().toString();
          String purpose = bind.get(Var.alloc("purpose")).getLiteral().toString();
          String reference = bind.get(Var.alloc("reference")).getLiteral().toString();
          String expectedOutput = bind.get(Var.alloc("expectedOutput")).getLiteral().toString();
          String outfname = bind.get(Var.alloc("outfname")).getLiteral().toString();
          String mappingfname = bind.get(Var.alloc("mappingfname")).getLiteral().toString();
          String dbFileName = bind.get(Var.alloc("dbfile")).getLiteral().toString();
          Optional<String> dbnameOpt = Optional.of(bind.get(Var.alloc("db")).getURI());

          
          R2RMLTestParameter param = R2RMLTestParameter.builder()
              .createDM(false)
              .dbname(dbnameOpt.orElse(dbname))
              .dbFileLocation(makeAbsolute(folder, dbFileName))
              .outputLocation(getFileOutName(folder, outfname))
              .r2rmlLocation(makeAbsolute(folder, mappingfname) )
              .referenceOutput(makeAbsolute(folder, outfname))
              .testCaseName(identifier)
              .build();
          testCases.add(param);

          }
        } 
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return testCases;
    
  }
  
  
  private static String makeAbsolute(File folder, String name){
    return folder.getAbsolutePath() + "/"  +name;
  }
  
  private static String getFileOutName(File folder, String outfname) {
    return folder.getAbsolutePath() + "/" + outfname.split("\\.")[0]
        + "-sparqlmap." + outfname.split("\\.")[1];
  }
  
  
  
  public  Collection<MappingTestParameter> getMappingTests(String dbname){
    
    
    Collection<MappingTestParameter> testCases = Lists.newArrayList();
    
    for(File dataSetFolder : new File (location,"mappingtest").listFiles()){
      String dsname = dataSetFolder.getName();
      
      if(dataSetFolder.isDirectory()){
        File mappingFile = new File(dataSetFolder,String.format("mapping-%s.ttl",dbname.toLowerCase()));
        if(!mappingFile.exists()){
          mappingFile = new File(dataSetFolder,"mapping.ttl");
        }
        if(!mappingFile.exists()){
          //no mapping file present, skipping folder
          continue;
        }
        
        Collection<File> sqlFiles = Lists.newArrayList();
        try {
          Files.newDirectoryStream(dataSetFolder.toPath(),("dataset-"+dbname.toLowerCase() + "*"))
          .forEach(path -> sqlFiles.add(path.toFile()));
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        
        
        
        if(sqlFiles.isEmpty()){
          continue;
        }
        
        FilenameFilter dotSparqlFiles = new PatternFilenameFilter("^.*\\.sparql$");
        for(File sparqlFile: Lists.newArrayList(new File(dataSetFolder,"queries").listFiles(dotSparqlFiles))) {
          try {
            String testname = dataSetFolder.getName() + "/" + sparqlFile.getName();
            String query = new String(Files.readAllBytes(sparqlFile.toPath()));
            
            MappingTestParameter mtp = MappingTestParameter.builder()
                .dsName(dsname)
                .mappingFile(mappingFile)
                .query(query)
                .sqlFiles(sqlFiles)
                .testname(testname)
                .build();
            testCases.add(mtp);
          } catch (IOException e) {
            throw new RuntimeException(e);           
            }
        }
      }  
    }

    return testCases;
    
    
  }
  
  

}
