package org.aksw.sparqlmap.cli;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelQueryDump;
import org.aksw.sparqlmap.common.SparqlMapSetup;
import org.aksw.sparqlmap.config.ConfigBeanCli;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.TranslationContext;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.metamodel.factory.DataContextFactory;
import org.apache.metamodel.factory.DataContextProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;

import com.google.common.collect.Lists;

@Configuration
@ComponentScan(basePackages={"org.aksw.sparqlmap.cli", " org.aksw.sparqlmap.common"})
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class SparqlMapCli implements ApplicationRunner{
  
  
  //expose for testing and for custom interactions
  public static OutputStream out = System.out;
  public static OutputStream err = System.err;

  @Autowired
  ConfigBeanCli cliConf;
  
  @Autowired
  SparqlMap sm;

  
  DataContextFactory dconf;
  DataContextProperties props;
  


  @Override
  public void run(ApplicationArguments args) throws Exception {
    try{
    OutputStream out;
    if(cliConf.getDumpLocation()==null){
      out = SparqlMapCli.out;
    }else{
      out = new FileOutputStream(new File(cliConf.getDumpLocation()));
    }
    switch(cliConf.getAction()){
      case DIRECTMAPPING:
        RDFFormat dmtargetLang =  new RDFFormat(cliConf.getFormat());
        RDFDataMgr.write(
            out, 
            sm.getMapping().getR2rmlMapping(), 
            dmtargetLang.getLang());
        break;
      case DUMP:
        Lang dtargetLang =  cliConf.getFormat();
        sm.getDumpExecution().dump(out, dtargetLang);
        break;
      case QUERY:
        TranslationContext tcon = new TranslationContext();
        tcon.setQueryString(cliConf.getQuery());
        tcon.setQueryName("cliquery");
        QueryExecution qexec =  sm.execute(tcon);
        int queryType = tcon.getQuery().getQueryType();
        if(Query.QueryTypeAsk == queryType){
          ResultSetFormatter.out(out, qexec.execAsk());
        }
        if(Query.QueryTypeSelect == queryType){
          ResultsFormat selectoutputFormat =  cliConf.getQueryFormat();
          ResultSetFormatter.output(out, qexec.execSelect(), selectoutputFormat);
        } 
        if(Query.QueryTypeDescribe == queryType){
          RDFFormat descTargetLang = new RDFFormat(cliConf.getFormat());
          RDFDataMgr.write(out, qexec.execDescribe(), descTargetLang);
          
        }
        if(Query.QueryTypeConstruct == queryType){
          RDFFormat constTargetLang =  new RDFFormat(cliConf.getFormat());
          RDFDataMgr.write(out, qexec.execConstruct(), constTargetLang);
        }
      }
      
      out.close();
      
    } catch (Throwable e){
      e.printStackTrace();
    }
      
      
  }
  
 
}
