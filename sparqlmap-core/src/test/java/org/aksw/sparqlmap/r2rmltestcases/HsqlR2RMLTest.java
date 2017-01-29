package org.aksw.sparqlmap.r2rmltestcases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.aksw.sparqlmap.DBHelper;
import org.aksw.sparqlmap.DockerHelper.DBConnConfig;
import org.aksw.sparqlmap.R2RMLTestParameter;
import org.aksw.sparqlmap.TestFileManager;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class HsqlR2RMLTest extends R2RMLTest {
  
  
  private static final Logger log = LoggerFactory.getLogger(HsqlR2RMLTest.class);
  
  public HsqlR2RMLTest(R2RMLTestParameter param) {
    super(param);
  }

  private static Server server;
  
  private SparqlMap sparqlMap;
  
  private DataContext dcon;
  

  
  HikariDataSource cp ;
  
  @Parameters(name="{0}")
  public static Collection<Object[]> dbdata(){
    
  Collection<R2RMLTestParameter> params = new TestFileManager().getR2rmltest().get("hsqldb");  
  
    return params.stream().map(param -> {
      R2RMLTestParameter[] p  = {param}; // cannot convert to array without intermediate assignment
      return p;
    }).collect(Collectors.toList());
  }


  @BeforeClass
  public static void startServer(){
    server = new Server();
    server.setSilent(false);
    server.setDatabaseName(0, "bsbm2-100k");
    server.setDatabasePath(0, "mem:sparqlmaptest\"");
    server.start();
  }
  
  @Before
  public void init(){
    cp = new HikariDataSource();
    cp.setJdbcUrl("jdbc:hsqldb:mem:sparqlmaptest/");
    cp.setUsername("sa");
    cp.setPassword("");
    dcon = new JdbcDataContext(cp);

    
    
  }
  @After
  public void stop(){
    cp.close();
  }
  
  
  @Override
  public  SparqlMap getSparqlMap() {
    synchronized (this) {
      if(sparqlMap==null){
        sparqlMap = SparqlMapBuilder.newSparqlMap(null).connectTo(new JdbcDataContext(cp)).mappedBy(RDFDataMgr.loadModel(param.getR2rmlLocation())).create();
      }
    }
   
    
    return sparqlMap;
  }


  @Override
  public String getDBName() {
    // TODO Auto-generated method stub
    return "HSQLDB";
  }


  @Override
  public void flushDatabase() throws ClassNotFoundException, SQLException {
   
      Connection conn = cp.getConnection();

      DBHelper.flushDb(conn);

      conn.close();
      
    }


  @Override
  DataContext getDatacontext() {
    return dcon;
  }


  @Override
  public void loadFileIntoDB(String file) throws ClassNotFoundException, SQLException, IOException {
    
    log.info(String.format("Loading %s into the database",file));
    
     Connection conn = cp.getConnection();
     DBHelper.loadSqlFile(conn, file);
     
     conn.close();

    
  }
    

  
  
}
