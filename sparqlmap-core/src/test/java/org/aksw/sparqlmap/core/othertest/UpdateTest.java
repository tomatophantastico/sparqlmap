package org.aksw.sparqlmap.core.othertest;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.aksw.sparqlmap.DBHelper;
import org.aksw.sparqlmap.TestHelper;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.hsqldb.Server;
import org.junit.Before;
import org.junit.Test;

import com.zaxxer.hikari.HikariDataSource;

public class UpdateTest {
  
  private static final String testcaselocation = "../sparqlmap-test/src/main/resources/update/";

  
  private Server server;

  private HikariDataSource cp;

  private JdbcDataContext dcon;

  @Before
  public void setup(){
    server = new Server();
    server.setSilent(false);
    server.setDatabaseName(0, "bsbm2-100k");
    server.setDatabasePath(0, "mem:sparqlmaptest\"");
    server.start();
    
    cp = new HikariDataSource();
    cp.setJdbcUrl("jdbc:hsqldb:mem:sparqlmaptest/");
    cp.setUsername("sa");
    cp.setPassword("");
    dcon = new JdbcDataContext(cp);
    
    try(Connection conn = cp.getConnection()){
      DBHelper.loadSqlFile(conn, testcaselocation + "/dataset.sql");

    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  


  @Test
  public void testDump() throws SQLException{
    
    SparqlMap sm = SparqlMapBuilder.newSparqlMap(null).connectTo(dcon).mappedBy(testcaselocation+"mapping.ttl").create();
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
  public void testInsert(){
    SparqlMap sm = SparqlMapBuilder.newSparqlMap(null).connectTo(dcon).mappedBy(testcaselocation+"mapping.ttl").create();

    sm.update("INSERT DATA {<http://example.org/data/event/id=4> <http://example.org/vocab#label> \"event4\" }").execute();
    
    Model result = ModelFactory.createModelForGraph(sm.getDumpExecution().dumpDatasetGraph().getDefaultGraph());
    try(FileOutputStream fos = new FileOutputStream(testcaselocation + "result-updated.ttl")){
      RDFDataMgr.write(fos, result, Lang.NTRIPLES);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Model exptected  = RDFDataMgr.loadModel(testcaselocation + "expected-updated.ttl");
    TestHelper.assertModelAreEqual(RDFDataMgr.loadModel(testcaselocation + "result-updated.ttl"),exptected);
    
    
    
    
  }

}
