package org.aksw.sparqlmap.web.spring;

import org.aksw.sparqlmap.core.spring.ContextSetup;
import org.aksw.sparqlmap.web.SparqlMapContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ContextManagerConfiguration {
  
  @Autowired
  Environment env;
  
  @Bean SparqlMapContextManager configureContextManager(){
    SparqlMapContextManager manager = new SparqlMapContextManager();
    
    
    // scan for a sparqlmap home variable
    if(env.getProperty("SPARQLMAP_HOME") !=null){
      
      ApplicationContext root =  ContextSetup.contextFromFolder(  env.getProperty("SPARQLMAP_HOME"));
      SparqlMapContextManager conMgm = new SparqlMapContextManager();
      conMgm.putContext(SparqlMapContextManager.ROOT, root);
      
    }
    
    // scan for other parameters given in this context.
    
    
    return manager;
    
    
    
    
  }

}
