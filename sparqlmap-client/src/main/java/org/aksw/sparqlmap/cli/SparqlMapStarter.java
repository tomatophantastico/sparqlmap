package org.aksw.sparqlmap.cli;

import org.aksw.sparqlmap.common.SparqlMapSetup;
import org.aksw.sparqlmap.web.SparqlMapWebController;
import org.aksw.sparqlmap.web.spring.ContextManagerConfiguration;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;

import com.google.common.collect.Lists;

public class SparqlMapStarter {
  
 public static void main(String[] args) {
    
    SpringApplication springApp = null;
    if(Lists.newArrayList(args).stream().map( String::toLowerCase)
        .anyMatch(arg -> arg.equals("--action=web"))){
      springApp = new SpringApplication(SparqlMapWebController.class,ContextManagerConfiguration.class,SparqlMapSetup.class);
      springApp.run(args);
    }else{
      springApp = new SpringApplication(SparqlMapSetup.class,SparqlMapCli.class);
      springApp.setWebEnvironment(false);
      springApp.setBannerMode(Mode.OFF);
      springApp.run(args).close();
    }
    
     
  }

}
