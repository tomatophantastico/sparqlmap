package org.aksw.sparqlmap.config;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import lombok.Data;

@Configuration
@ConfigurationProperties
@Data
@Parameters(separators = "=")
public class ConfigBeanBase {
  
  public ConfigBeanBase(){
    String hostname;
    try {
      hostname =  InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostname = "localhost";
    }
    
    baseiri = String.format("http://%s/base/",hostname);
    dmBaseUriPrefix = String.format("http://%s/",hostname);

    
  }


  @Parameter(names = {"-h","--help"}, help = true)
  private boolean help;

  @Parameter(names={"--baseiri","--baseuri","-bi"}, description="The uri prefix used, when incomplete iris are encountered during generation.")
  private String baseiri;
  
  @Parameter(names={"--r2rmlfile","-f"}, description="Path to the R2RML mapping file. The suffix MUST match its RDF serialization (.ttl for TURTLE serializations)", order=3)
  private String r2rmlFile;
  
  @Parameter(names={"--dmBaseUriPrefix"}, description="The base iri (the prefix) used in the direct mapping (actually a R2RML representation of Direct Mapping instructions for a dataset)")
  private String dmBaseUriPrefix;
  
  @Parameter(names={"--dmMappingUriPrefix"}, description="The prefix for the mapping resources. These are not visible in resulting data set")
  private String dmMappingUriPrefix;
  
  @Parameter(names={"--dmVocabUriPrefix"}, description="Vocabularies (predicates and is-a statements) generated for a direct mapping will have this prefix")
  private String dmVocabUriPrefix;
  
  @Parameter(names={"--dmInstanceUriPrefix"}, description="Instance resources use this prefix.")
  private String dmInstanceUriPrefix;
  
  @Parameter(names={"--dmSeparatorChar"}, description="In case multiple columns are involved in creating a URI, this separator is used to distinguish the columns.")
  private String dmSeparatorChar = "+";
  
 
  

 

}
