package org.aksw.sparqlmap.cli;

import org.aksw.sparqlmap.common.SparqlMapSetup;
import org.aksw.sparqlmap.web.ContextManagerConfiguration;
import org.aksw.sparqlmap.web.SparqlMapWebConfig;
import org.aksw.sparqlmap.web.SparqlMapWebController;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;

import com.google.common.collect.Lists;

public class SparqlMapStarter {
  
 public static void main(String[] args) {
    
    SpringApplication springApp = null;
    if(Lists.newArrayList(args).stream().map( String::toLowerCase)
        .anyMatch(arg -> arg.equals("--action=web"))){
      springApp = new SpringApplication(SparqlMapWebConfig.class,SparqlMapWebController.class,ContextManagerConfiguration.class,SparqlMapSetup.class);
      springApp.run(args);
    }else{
      springApp = new SpringApplication(SparqlMapSetup.class,SparqlMapCli.class);
      springApp.setWebEnvironment(false);
      springApp.setBannerMode(Mode.OFF);
      springApp.run(args).close();
    }
    
     
  }

}
