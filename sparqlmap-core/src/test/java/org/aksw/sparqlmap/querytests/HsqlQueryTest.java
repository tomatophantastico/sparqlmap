package org.aksw.sparqlmap.querytests;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.aksw.sparqlmap.DBHelper;
import org.aksw.sparqlmap.DockerHelper.DBConnConfig;
import org.aksw.sparqlmap.MappingTestParameter;
import org.aksw.sparqlmap.R2RMLTestParameter;
import org.aksw.sparqlmap.TestFileManager;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.hsqldb.Server;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import com.zaxxer.hikari.HikariDataSource;

public class HsqlQueryTest extends QueryBaseTest {
  
  private static Server server;
  
  public HsqlQueryTest(MappingTestParameter param) {
    super(param);
  }
  
  
  @Parameters(name="{0}")
  public static Collection<Object[]> dbdata(){
    Collection<MappingTestParameter> params = new TestFileManager().getMappingTests("hsqldb");
    
    return params.stream().map(param -> {
      MappingTestParameter[] p  = {param}; // cannot convert to array without intermediate assignment
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
  
  
  @Override
  public void initContext() {
   
    DataContext dc = new JdbcDataContext(getDs());
    
    sparqlMap = SparqlMapBuilder.newSparqlMap("http://example.com/sparqlmaptest/")
        .connectTo(dc)
        .mappedBy(RDFDataMgr.loadModel(param.getMappingFile().getAbsolutePath()))
        .create();
  }

  @Override
  public boolean populateDB() {
    HikariDataSource ds = getDs();
    boolean worked =  DBHelper.initDb(ds,param.getDsName(),param.getSqlFiles());
    ds.close();
    
    return worked;
    
  }
  
  private HikariDataSource getDs(){
    HikariDataSource cp = new HikariDataSource();
    cp.setJdbcUrl("jdbc:hsqldb:mem:sparqlmaptest/");
    cp.setUsername("sa");
    cp.setPassword("");
    return cp;
    
  }
  
  
}
