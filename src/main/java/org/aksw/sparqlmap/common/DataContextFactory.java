package org.aksw.sparqlmap.common;

import java.net.MalformedURLException;
import java.util.Map;

import org.aksw.sparqlmap.core.ImplementationException;
import org.aksw.sparqlmap.core.SparqlMapException;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.couchdb.CouchDbDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.excel.ExcelConfiguration;
import org.apache.metamodel.excel.ExcelDataContext;
import org.apache.metamodel.factory.ResourceFactoryRegistryImpl;
import org.apache.metamodel.factory.ResourceProperties;
import org.apache.metamodel.factory.ResourcePropertiesImpl;
import org.apache.metamodel.mongodb.mongo2.MongoDbDataContext;
import org.apache.metamodel.util.Resource;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.eobjects.metamodel.access.AccessDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataContextFactory {
  
  
  private static final Logger LOGGER = LoggerFactory.getLogger(DataContextFactory.class);
  
  @Autowired
  DataSourceConfig dsconf;
  
  @Bean
  public DataContext create(){
    DataContext dc = null;
    switch(dsconf.getType()){
    case JDBC: 
      HikariConfig hconf = new HikariConfig();
      hconf.setJdbcUrl(dsconf.getUrl());
      hconf.setUsername(dsconf.getUsername());
      hconf.setPassword(dsconf.getPassword());
      hconf.setMaximumPoolSize(dsconf.getMaxPoolSize());
      HikariDataSource hds = new HikariDataSource(hconf);
      dc = org.apache.metamodel.DataContextFactory.createJdbcDataContext(hds);
      break;
    case MONGODB2: 
      
      dc = new MongoDbDataContext(getMongo(dsconf).getDB(dsconf.getDbName()));
      break;
    case MONGODB3: 
      dc = new org.apache.metamodel.mongodb.mongo3.MongoDbDataContext((getMongo( dsconf).getDatabase(dsconf.getDbName())));
      break;
    case COUCHDB: 
      HttpClient client;
      try {
        client = new StdHttpClient.Builder()
                  .url(dsconf.getUrl())
                  .username(dsconf.getUsername())
                  .password(dsconf.getPassword())
                  .build();
      } catch (MalformedURLException e) {
       throw new SparqlMapException("Cannot connect to COUCHDB",e);
      }
      
      dc = new CouchDbDataContext(client);
      
      
      break;
    case HBASE: 
      throw new ImplementationException("not yet implemented");
     // break;
    case CASSANDRA: 
      throw new ImplementationException("not yet implemented");
      //break;
    case ELASTIC:
      throw new ImplementationException("not yet implemented");
      //break;  
    case SALESFORCE:
      throw new ImplementationException("not yet implemented");
      //break;
    case SUGARCRM: 
      throw new ImplementationException("not yet implemented");
      //break;
    case CSV: 
      Resource csvFile = getResource(dsconf);
      validate(csvFile);
      CsvConfiguration csvconf = new CsvConfiguration(
          dsconf.getColumnNameLineNumber(), 
          dsconf.getEncoding(), 
          dsconf.getSeparatorChar(), 
          dsconf.getQuoteChar(), 
          dsconf.getEscapeChar(), 
          dsconf.getFailOnInconsistentRowLength());
      
      dc = new CsvDataContext(csvFile,csvconf);
      break;
    case EXCEL:
      
      Resource excelF = getResource(dsconf);
      ExcelConfiguration econf = new ExcelConfiguration(
          dsconf.getColumnNameLineNumber(), 
          dsconf.getSkipEmptyLines(), 
          dsconf.getSkipEmptyColumns());
      
      dc = new ExcelDataContext(excelF, econf);
      
      break;
    case ACCESS:
      
      dc = new AccessDataContext(dsconf.getUrl());
      break;
    case XML: 
      throw new ImplementationException("not yet implemented");
      //break;
    case JSON:
      throw new ImplementationException("not yet implemented");
      //break;
    }
    
    
  
    
    return dc;
  }
  
  
  private static MongoClient getMongo(DataSourceConfig dcprops){ 
    MongoClient mc;
    ServerAddress saddr = new ServerAddress(dcprops.getUrl());
    
    if(dcprops.getUsername()!=null){

      mc = new MongoClient(saddr, Lists.newArrayList(MongoCredential.createCredential(dcprops.getUsername(), dcprops.getDbName(), dcprops.getPassword().toCharArray())));
    }else{
      mc = new MongoClient(saddr);
    }
    
    
   return mc;
    
    
  }
  
  private static Resource getResource(DataSourceConfig dsc){
    Map<String,Object> resvalues = Maps.newHashMap();
    if(dsc.getUsername()!=null){
      resvalues.put("username", dsc.getUsername());
    }
    if(dsc.getPassword()!=null){
      resvalues.put("password", dsc.getPassword());
    }
    dsc.setUrl(dsc.getUrl());
    ResourceProperties rps =  new ResourcePropertiesImpl(resvalues);
    
    return  ResourceFactoryRegistryImpl.getDefaultInstance().createResource(rps);
    
  }
  
  
  private static void validate(Resource res){
    if(!res.isExists()){
      LOGGER.warn("Resource " + res.getQualifiedPath() + " does not exist");
    }
  }
  
  
 

}
