package org.aksw.sparqlmap.backend.metamodel;

import java.util.Arrays;

import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.CompiledQuery;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.update.Update;
import org.hsqldb.Server;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.google.common.collect.Streams;
import com.zaxxer.hikari.HikariDataSource;


public class Test {
  
  static Server server;
  
  @org.junit.Test
  public void testFromItem(){
    HikariDataSource cp = new HikariDataSource();
    cp.setJdbcUrl("jdbc:hsqldb:mem:sparqlmaptest/");
    cp.setUsername("sa");
    cp.setPassword("");
    
    
    JdbcDataContext jdc = new JdbcDataContext(cp);
    CreateTable ct = new CreateTable(jdc.getDefaultSchema(), "test");
    ct.withColumn("id").ofType(ColumnType.INTEGER).asPrimaryKey();
    ct.withColumn("text").ofType(ColumnType.VARCHAR).ofSize(10);
    jdc.executeUpdate(ct);
    
    jdc.executeQuery("select * from test ").forEach(row -> {
      Assert.assertEquals(2, row.getSelectItems().length);
      
    });
    
  }
  
  @BeforeClass
  public static void startServer(){
    server = new Server();
    server.setSilent(false);
    server.setDatabaseName(0, "bsbm2-100k");
    server.setDatabasePath(0, "mem:sparqlmaptest\"");
    server.start();
    

  }

  
  

}
