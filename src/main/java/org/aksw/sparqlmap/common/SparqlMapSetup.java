package org.aksw.sparqlmap.common;

import org.aksw.sparqlmap.core.ImplementationException;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
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
        
       smb = SparqlMapBuilder.newSparqlMap(baseIri).connectToAccess(conf.getDsLocation());
      
      case CSV:
        smb = SparqlMapBuilder.newSparqlMap(baseIri).connectToCsv(conf.getDsLocation());
        
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

}
