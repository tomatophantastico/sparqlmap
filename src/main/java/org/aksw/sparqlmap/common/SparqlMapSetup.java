package org.aksw.sparqlmap.common;

import java.io.File;

import org.aksw.sparqlmap.core.ImplementationException;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SparqlMapSetup {
  
  
  private static final Logger log = LoggerFactory.getLogger(SparqlMapSetup.class);
  
  @Autowired
  BaseConfig conf;
    
  @Bean
  public SparqlMap configSparqlMap(){
    
    //create the datacontext
   
    SparqlMapBuilder.SparqlMapMappingBuilder smb = null;
    
    DataSourceType dst = conf.getDsType();
    String baseIri = conf.getBaseiri();
    
    switch(dst){
      case JDBC:
        HikariConfig hconf = new HikariConfig();
        hconf.setJdbcUrl(conf.getDsLocation());
        hconf.setUsername(conf.getDsUsername());
        hconf.setPassword(conf.getDsPassword());
        hconf.setMaximumPoolSize(conf.getMaxPoolSize());
        HikariDataSource hds = new HikariDataSource(hconf);
        
        smb = SparqlMapBuilder.newSparqlMap(baseIri).connectJdbcBackend(hds);
        
        
        break;
      case MONGODB3:
       smb = SparqlMapBuilder.newSparqlMap(baseIri).connectToMongoDb3(conf.getDsLocation(), conf.getDsIdentifier(),conf.getDsUsername(),conf.getDsPassword());
        break;
      case ACCESS:
        
       smb = SparqlMapBuilder.newSparqlMap(baseIri).connectToAccess(warnEmptyFile(conf.getDsLocation()));
      
      case CSV:
        smb = SparqlMapBuilder.newSparqlMap(baseIri).connectToCsv(warnEmptyFile(conf.getDsLocation()));
        
        break;
        
      default:
        throw new ImplementationException(String.format("Sorry, {} is not yet implemented", conf.getDsType().toString()));
    }
    if(conf.getR2rmlfile()==null){
      //use direct mapping
      smb.mappedByDefaultMapping(
          conf.getDmBaseUriPrefix(), 
          conf.getDmMappingUriPrefix(), 
          conf.getDmInstanceUriPrefix(), 
          conf.getDmVocabUriPrefix(), 
          conf.getDmSeparatorChar());
      
      
    }else{
      smb.mappedBy(conf.getR2rmlfile());
    }
    return smb.create();    
  }
  
  /**
   * 
   * @return the exact same String
   */
  private String warnEmptyFile(String fileLocation){
    File dsFile = new File(fileLocation); 
    if(!dsFile.exists() || dsFile.length()<=0){
      log.warn("empty datasource file: " + fileLocation);
    }
    return fileLocation;
  }

}
