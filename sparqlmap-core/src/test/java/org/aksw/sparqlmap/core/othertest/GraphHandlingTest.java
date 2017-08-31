package org.aksw.sparqlmap.core.othertest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.aksw.sparqlmap.DBHelper;
import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.backend.metamodel.MetaModelBackend;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphCollection;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.ColumnType;
import org.hsqldb.Server;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.init.ScriptException;

import com.mongodb.DB;
import com.zaxxer.hikari.HikariDataSource;

public class GraphHandlingTest {
  
  
  private static String testcaselocation = "../sparqlmap-test/src/main/resources/mappingtest/simpsons/";
  private static DataSource ds;
  private static SparqlMap sm;
  
  @Test
  public  void testNamedGRaphsMustNotContainDefaultGraph(){  
   
    sm.execute("select distinct ?g {GRAPH ?g {?s ?p ?o}}").execSelect().forEachRemaining(row -> {
      Assert.assertFalse("Named Graphs must not contain default graph", 
          row.get("g").asResource().getURI().equals(Quad.defaultGraphNodeGenerated.getURI()));
      
    });
  }
  
  @Test
  public void testNGwithOPtional(){
    ResultSet rs =  new ResultSetMem(sm.execute("SELECT DISTINCT ?s ?g { \n" + 
        "   GRAPH <http://thesimpsons.com/GraphAdult> {?s ?p ?o .}\n" + 
        "   OPTIONAL {GRAPH ?g {?s ?x ?y .} FILTER (?g != <http://thesimpsons.com/GraphAdult>)}\n" + 
        "} ORDER BY ?s ?g").execSelect());
    
    rs.forEachRemaining(row -> {
          Optional.ofNullable(row.get("g"))
          .ifPresent((t -> Assert.assertFalse("Named Graphs must not contain default graph", 
              t.asResource().getURI().equals(Quad.defaultGraphNodeGenerated.getURI()))));
        });
  }
  
  @Test
  public void testDescribe(){
    Model rs = sm.execute("DESCRIBE  <http://thesimpsons.com/SiliconAlley>  FROM <http://thesimpsons.com/GraphAdult>").execDescribe();
    Assert.assertFalse(rs.isEmpty());
  }
  

  
  @Test
  public void testDescribeWherTwoVars(){
    Model rs = sm.execute("DESCRIBE ?s ?o FROM <http://thesimpsons.com/GraphAdult> WHERE { ?s ?p ?o FILTER (?s = <http://thesimpsons.com/SiliconAlley>)}").execDescribe();
    Assert.assertTrue(rs.size() < 
        sm.execute("DESCRIBE ?s FROM <http://thesimpsons.com/GraphAdult> WHERE { ?s ?p ?o FILTER (?s = <http://thesimpsons.com/SiliconAlley>)}").execDescribe().size());
  }
  
  @Test
  public void testDescribeWhere(){
    Model rs = sm.execute("DESCRIBE ?s FROM <http://thesimpsons.com/GraphAdult> WHERE { ?s ?p ?o FILTER (?s = <http://thesimpsons.com/SiliconAlley>)}").execDescribe();
    Assert.assertFalse(rs.isEmpty());
  }
  
  @Test
  public void testDescribeNoPermissionWhere(){
    Model rs = sm.execute("DESCRIBE ?s FROM <http://thesimpsons.com/GraphLisa> WHERE { ?s ?p ?o FILTER (?s = <http://thesimpsons.com/SiliconAlley>)}").execDescribe();
    Assert.assertTrue(rs.isEmpty());
  }
  @Test
  public void testDescribeNoPermission(){
    Model rs = sm.execute("DESCRIBE <http://thesimpsons.com/SiliconAlley> FROM <http://thesimpsons.com/GraphLisa>").execDescribe();
    Assert.assertTrue(rs.isEmpty());
  }
  
  @Test
  public void testDescribeNoPermissionNoGRaph(){
    Model rs = sm.execute("DESCRIBE <http://thesimpsons.com/SiliconAlley>").execDescribe();
    Assert.assertTrue(rs.isEmpty());
  }

  @BeforeClass
  public static void startServer() throws ScriptException, SQLException, IOException{

    HikariDataSource cp = new HikariDataSource();
    cp.setJdbcUrl("jdbc:hsqldb:mem:sparqlmaptest/");
    cp.setUsername("sa");
    cp.setPassword("");
    ds = cp;
    
    DBHelper.loadSqlFile(ds.getConnection(), testcaselocation + "dataset-hsqldb.sql");
    
    JdbcDataContext jdc = new JdbcDataContext(ds);
    MetaModelBackend mmBackend = new MetaModelBackend(jdc).closeOnShutdown(cp);
    
     sm =  SparqlMapBuilder.newSparqlMap("http://example.com/graphtest/").connectTo(mmBackend).mappedBy(testcaselocation + "mapping.ttl").create();
    
      
  }
  
 


}
