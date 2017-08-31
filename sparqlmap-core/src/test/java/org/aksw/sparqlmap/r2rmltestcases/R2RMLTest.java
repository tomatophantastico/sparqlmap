package org.aksw.sparqlmap.r2rmltestcases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.aksw.sparqlmap.DBHelper;
import org.aksw.sparqlmap.DockerHelper.DBConnConfig;
import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.R2RMLTestParameter;
import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.automapper.MappingGenerator;
import org.aksw.sparqlmap.core.automapper.MappingPrefixes;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.parser.QueryParserException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;


@RunWith(value = Parameterized.class)
public abstract class R2RMLTest {
	
	public static String baseUri = "http://example.com/base/";
	
	
	
	R2RMLTestParameter param;
	
	
	
	private static Logger log = LoggerFactory.getLogger(R2RMLTest.class);
	
	private static Properties fails;
	
	{
	    fails = new Properties();
        try {
	    InputStream stream = 
	               ClassLoader.getSystemClassLoader().getResourceAsStream("bsbm_failing.properties");
	       fails.load(stream);

            stream.close();
        } catch (IOException e) {
            log.error("Problem loading failing test information",e);
        }
	}
	
	

	static Boolean dbIsReachable = null;


	public R2RMLTest(R2RMLTestParameter param) {
		super();
		this.param = param;
	}


	@Test
	public void runTestcase() throws ClassNotFoundException, SQLException, IOException{
	  if(fails.containsKey(param.getTestCaseName())){
	      String value = fails.getProperty(param.getTestCaseName());
	      String dbs = value.split(":")[0];
	      Assume.assumeFalse(value.split(":")[1], 
	              dbs.equals("ALL")
	              || Lists.newArrayList(dbs.split(",")).contains(getDBName().toLowerCase()));

	  }
	 


	  flushDatabase();
		loadFileIntoDB(param.getDbFileLocation());
		
		
		if(param.isCreateDM()){
			createDM(param.getR2rmlLocation());
		}
		
		
		
		//let the mapper run.
		
		SparqlMap r2r = getSparqlMap();
    
	  
    try {
      r2r.getDumpExecution().streamDump(new FileOutputStream(new File(param.getOutputLocation())));
    } catch (QueryParserException e) {
      Assume.assumeNoException(e);
    }
    r2r.close();
	
		
		assertAreEqual(param.getOutputLocation(),param.getReferenceOutput());
		
	}
	
	 public abstract String getDBName();
	
	
	
	 
	  
	

	
	
	
	
	
	
	
	public void createDM(String wheretowrite) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException, MetaModelException{
		
		String basePrefix = "http://example.com/base/";
		
		MappingGenerator db2r2rml = new MappingGenerator( 
		    new MappingPrefixes(basePrefix)
		      .withInstancePrefix(basePrefix)
		      .withMappingPrefix(basePrefix)
		      .withVocabularyPrefix(basePrefix));
		
		Model mapping = db2r2rml.generateMapping(SchemaTranslator.translate(getDatacontext().getDefaultSchema()));
		mapping.write(new FileOutputStream(new File(wheretowrite)), "TTL", null);
		
		
	}
	


	
	
	
	/**
	 * closes the connection
	 * @param conn
	 */
	public void closeConnection(Connection conn){
		//makeshift connection handling is ok here.
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * load the file into the database
	 * @param file
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public abstract void loadFileIntoDB(String file) throws ClassNotFoundException, SQLException, IOException;


	/**
	 * deletes all tables of the database
	 * @return true if delete was successfull
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public abstract void flushDatabase() throws ClassNotFoundException, SQLException;
	
	


	/**
	 * compares the two files for equality
	 * @param outputLocation2
	 * @param referenceOutput2
	 * @return true if they are equal
	 * @throws SQLException 
	 * @throws IOException 
	 */
	
	public void assertAreEqual(String outputLocation, String referenceOutput) throws SQLException, IOException {
		
	  
		Model m1 = ModelFactory.createDefaultModel();
		String fileSuffixout = outputLocation.substring(outputLocation.lastIndexOf(".")+1).toUpperCase();
		
		if(fileSuffixout.equals("NQ")){
			DatasetGraph dsgout = RDFDataMgr.loadDatasetGraph(outputLocation);
			DatasetGraph dsdref = RDFDataMgr.loadDatasetGraph(referenceOutput);
			
			
			
			
			Assert.assertFalse("Empty result, should have been:"+ Files.toString(new File(referenceOutput), Charsets.UTF_8) ,dsgout.isEmpty() && !dsdref .isEmpty());

			
			Iterator<Node> iout = dsgout.listGraphNodes();
			List<Node> iref = Lists.newArrayList(dsdref.listGraphNodes());
		
	    while (iout.hasNext()){
	      Node outNode = (Node) iout.next();
	      Graph outgraph =  dsgout.getGraph(outNode);
	      Graph refGRaf = dsdref.getGraph(outNode);
	      
	      if(refGRaf==null){
	        log.info("Missing graph in reference output :" + outNode);
	        break;
	      }else{
	        
	        TestHelper.assertModelAreEqual(
	            ModelFactory.createModelForGraph(outgraph), 
	            ModelFactory.createModelForGraph(refGRaf));
	        
  
  	      }
	      iref.remove(outgraph);
	    }
	    if(!iref.isEmpty()){
	      log.info("not all reference graphs were created"  + iref.toString());
	    }
	    
	    
			    
		}else {
		//if(fileSuffixout.equals("TTL")){
			m1.read(new FileInputStream(outputLocation),null,"TTL");
			Model m2 = ModelFactory.createDefaultModel();
			m2.read(new FileInputStream(referenceOutput),null,"TTL");
			
			TestHelper.assertModelAreEqual(m1, m2);
		}	
	}

	abstract DataContext getDatacontext();
	abstract SparqlMap getSparqlMap();
	

}
