package org.aksw.sparqlmap.cli;

import org.aksw.sparqlmap.common.SparqlMapSetup;
import org.aksw.sparqlmap.config.ConfigBeanBase;
import org.aksw.sparqlmap.config.ConfigBeanDataSource;
import org.aksw.sparqlmap.config.SparqlMapAction;
import org.aksw.sparqlmap.config.ConfigBeanCli;

import org.aksw.sparqlmap.web.SparqlMapWebSpringConfig;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class SparqlMapStarter {
  
 public static void main(String[] args) {
  
   ConfigBeanBase bc = new ConfigBeanBase();
   ConfigBeanDataSource dc = new ConfigBeanDataSource();
   ConfigBeanCli cc = new ConfigBeanCli();
   JCommander jc = new JCommander(new Object[] {bc,dc,cc});
   //jc.setCaseSensitiveOptions(false);
   //jc.setProgramName("SparqlMap");
   try {
    jc.parse(args);
    
    if(bc.isHelp()){
      jc.usage();
    }else{
      startSparqlMap(bc,cc,dc, args);
    }
  

    }catch (ParameterException e) {
      System.out.println("Error processing command line options: " + e.getMessage());
      jc.usage();
    }
  }
 
 
 private static void startSparqlMap(ConfigBeanBase bc, ConfigBeanCli cc, ConfigBeanDataSource dc, String[] args){
  
   SpringApplication springApp = null;
   if(SparqlMapAction.WEB.equals(cc.getAction())){
    springApp = new SpringApplicationBuilder(SparqlMapWebSpringConfig.class)
         .initializers(new ApplicationContextInitializer<AnnotationConfigEmbeddedWebApplicationContext>(){

          @Override
          public void initialize(AnnotationConfigEmbeddedWebApplicationContext applicationContext) {
            ConfigurableListableBeanFactory bf =  applicationContext.getBeanFactory(); 
            bf.registerSingleton(bc.getClass().getCanonicalName(), bc);
            bf.registerSingleton(cc.getClass().getCanonicalName(), cc);
            bf.registerSingleton(dc.getClass().getCanonicalName(), dc);
            applicationContext.register(SparqlMapSetup.class,SparqlMapCli.class);
            
          }})
         .main(null)
         .application();
    springApp.run(args);

   }else{
     springApp = new SpringApplication(SparqlMapCli.class);
     springApp.addInitializers(new ApplicationContextInitializer<AnnotationConfigApplicationContext>() {
       @Override
       public void initialize(AnnotationConfigApplicationContext applicationContext) {
         
         ConfigurableListableBeanFactory bf =  applicationContext.getBeanFactory(); 
         bf.registerSingleton(bc.getClass().getCanonicalName(), bc);
         bf.registerSingleton(cc.getClass().getCanonicalName(), cc);
         bf.registerSingleton(dc.getClass().getCanonicalName(), dc);
         applicationContext.register(SparqlMapSetup.class,SparqlMapCli.class);
       }
       
     });
     springApp.setWebEnvironment(false);
     springApp.setBannerMode(Mode.OFF);
     springApp.setMainApplicationClass(null);
     springApp.run(args).close();
   }
 }
}
