package org.aksw.sparqlmap.cli;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.sparqlmap.common.SparqlMapSetup;
import org.aksw.sparqlmap.config.ConfigBeanBase;
import org.aksw.sparqlmap.config.ConfigBeanCli;
import org.aksw.sparqlmap.config.ConfigBeanDataSource;
import org.aksw.sparqlmap.config.SparqlMapAction;
import org.aksw.sparqlmap.web.SparqlMapWebSpringConfig;
import org.apache.xmlbeans.impl.common.Levenshtein;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class SparqlMapStarter {

    public static Character PARAM_VAL_SEPARATOR = '=';
  
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
           try{
               startSparqlMap(bc,cc,dc, args);

           }catch (Exception e){

               Throwable root = Throwables.getRootCause(e);
               if(root instanceof FileNotFoundException){
                   print("File not found: " + root.getMessage());
               }else{

                   //other type of exception, we go full stacktrace
                   print(e.getMessage());

               }

           }
       }


    }catch (ParameterException e) {

       //parse the "main" parameter out

       Pattern pattern = Pattern.compile("Was passed main parameter '(.*?)' but no main parameter was defined in your arg class");
       String message = e.getMessage();
       Matcher matcher = pattern.matcher(message);
       String main = null;
       if(matcher.find()){
           main = matcher.group(1);

       }

       List<String> paramCandidates = Lists.newArrayList();

       if(main != null){
           if(main.contains("=")){
               main = main.substring(0,main.indexOf("="));
           }
           String mainLocal = main;



           paramCandidates = jc.getParameters().stream()
                   .flatMap(param -> Stream.of(param.getNames()
                           .split(", ")))
                   .filter(p -> Levenshtein.distance(mainLocal.toLowerCase(),p.toLowerCase())<5).collect(Collectors.toList());

       }

       if(paramCandidates.isEmpty()){
           print("Error processing command line options: " + e.getMessage());
           jc.usage();
       }else{
           print("Could not process option " + main + " did you mean: " +  paramCandidates.stream().collect(Collectors.joining(", ")));
           jc.usage();
       }


   }


  }


  private static void print(String out){
     System.err.println(out);
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
