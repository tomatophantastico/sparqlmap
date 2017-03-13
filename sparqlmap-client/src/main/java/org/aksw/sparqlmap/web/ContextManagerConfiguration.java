package org.aksw.sparqlmap.web;

import org.aksw.sparqlmap.core.SparqlMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextManagerConfiguration {

  @Autowired
  SparqlMap smRoot;

  @Bean SparqlMapContextManager configureContextManager(){
    
    SparqlMapContextManager conMgm = new SparqlMapContextManager();
    conMgm.putContext(SparqlMapContextManager.ROOT, smRoot);
    
    return conMgm;
  }

}
