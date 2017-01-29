package org.aksw.sparqlmap;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.sparqlmap.DockerHelper.DBConnConfig;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;

import com.google.common.collect.Lists;


/**
 * A Collection of methods conveniently bundeled for different db vendors.
 * Useful for handling test databases.
 * Do not use in production databases.
 * @author joerg
 *
 */
public class DBHelper {
  
  private static Logger log = LoggerFactory.getLogger(DBHelper.class);
  
  
  public static void flushDb(Connection conn){
    UpdateableDataContext dc = new JdbcDataContext(conn);
    
   List<Table> tables = Lists.newArrayList(dc.getDefaultSchema().getTables());
   Iterator<Table> tabIter = tables.iterator();
    while (!tables.isEmpty()){
      if(!tabIter.hasNext() && !tables.isEmpty() ){
        tables = Lists.newArrayList(dc.getDefaultSchema().getTables());
        tabIter = tables.iterator();
      }
      
      Table tab = tabIter.next();
      
      if(tab.getPrimaryKeyRelationships().length==0){
        tabIter.remove();
        dc.executeUpdate(new DropTable(tab));
      }
    }
  }
  
  public static void flushDbOracle(Connection conn){
    UpdateableDataContext dc = new JdbcDataContext(conn);
    
    for (Table table: dc.getDefaultSchema().getTables()){
      try {
        java.sql.Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE \"" + table.getName() + "\" CASCADE CONSTRAINTS");
        stmt.close();

      } catch (SQLException e) {
        log.info("brute force delete threw error, nothing unusual");
      }
    }
  }
  
  public static void loadSqlFile(Connection conn, String file) throws SQLException{
    
    ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
    rdp.addScript(new FileSystemResource(file));
    conn.setAutoCommit(true);
    try{
    rdp.populate(conn);
    }catch(ScriptStatementFailedException e){
        Assume.assumeNoException("Unable to load sql file: " + file, e);
    }
  }
  
  public static Connection getConnection(DBConnConfig dbconf) throws SQLException {
    DriverManager.setLoginTimeout(5);
    Connection conn = DriverManager.getConnection(dbconf.jdbcString,dbconf.username,dbconf.password);
    return conn;
  }
  
  /**
   * attempt to acquire connection for the given connection config.
   * 
   * returns false if fails, true if connection was successfully established.
   * 
   * @param dbconf
   * @return
   */
  public static boolean waitAndConnect(DataContext datacontext) {
    boolean success = false;
    for( int i =0;i<20;i++){
      try {
        datacontext.getSchemas();
        success = true;
        log.debug("connection acquired");
        break;
      } catch (Exception e) {
        log.debug("failed to acquire connection, waiting...");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    }
    
    return success;
    
  }
  
  /**
   * checks if the database is already loaded an 
   * 
   * @param dbconf
   * @param dsname
   * @param sqlfile
   * @return
   */
  public static boolean initDb(DataSource dbconf, String dsname, Collection<File> sqlfiles) {
    String tablename = "sparqlmaptest_" + dsname;
    String queryForTestTable = String.format("select 1 from \"%s\" limit 1;",
        tablename);
    
    //create a test connection in a loop
    // to wait till the db has started


    try (Connection conn = dbconf.getConnection();
        java.sql.Statement stmt = conn.createStatement();) {
      boolean createNew = false;
      try (

          ResultSet rs = stmt.executeQuery(queryForTestTable);) {
        
       

        } catch (SQLException e) {
          createNew = true;
        }
      
      if (createNew) {
        DBHelper.flushDb(conn);
        for(File sqlfile: sqlfiles){
          DBHelper.loadSqlFile(conn, sqlfile.getAbsolutePath());
        }
        try (java.sql.Statement stmtInsert = conn.createStatement();) {
          stmtInsert.execute(String.format("Create table %s (id int);", tablename));

        }
      }
    } catch (SQLException e1) {
      e1.printStackTrace();
      return false;
    }
    return true;
  }

}
