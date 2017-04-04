package org.aksw.sparqlmap.common;

import org.aksw.sparqlmap.config.ConfigBeanBase;
import org.aksw.sparqlmap.config.ConfigBeanDataSource;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.apache.metamodel.DataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparqlMapSetup {
 
  @Autowired
  ConfigBeanBase conf;
  
  @Autowired
  ConfigBeanDataSource dsconf;
  
  @Autowired
  DataContext dc;
    
  @Bean
  public SparqlMap configSparqlMap(){
    String baseIri = conf.getBaseiri();

    //create the datacontext
   
    SparqlMapBuilder.SparqlMapMappingBuilder smb = null;
    smb = SparqlMapBuilder.newSparqlMap(baseIri).connectTo(dc);
    
    if(conf.getR2rmlFile()==null){
      //use direct mapping
      smb.mappedByDefaultMapping(
          conf.getDmBaseUriPrefix(), 
          conf.getDmMappingUriPrefix(), 
          conf.getDmInstanceUriPrefix(), 
          conf.getDmVocabUriPrefix(), 
          conf.getDmSeparatorChar());
    }else{
      smb.mappedBy(conf.getR2rmlFile());
    }
    return smb.create();    
  }
}
