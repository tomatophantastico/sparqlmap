package org.aksw.sparqlmap.common;

import java.io.File;
import java.util.Map;

import org.aksw.sparqlmap.core.ImplementationException;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
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
  
  @Autowired
  DataSourceConfig dsconf;
  
  @Autowired
  DataContext dc;
    
  @Bean
  public SparqlMap configSparqlMap(){
    String baseIri = conf.getBaseiri();

    //create the datacontext
   
    SparqlMapBuilder.SparqlMapMappingBuilder smb = null;
    
    
    
    
    smb = SparqlMapBuilder.newSparqlMap(baseIri).connectTo(dc);
    
    
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
