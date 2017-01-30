package org.aksw.sparqlmap.cli;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Joiner;

import lombok.Data;

@Configuration
@ConfigurationProperties
@Data
public class SparqlMapCliConfig {
  


  
  @NotNull(message="Specify what you want to do, either: DIRECTMAPPING, DUMP, QUERY or WEB" )
  private SparqlMapAction action;
  
  private String query;
  
  private String dumpLocation;
  
  private String outputFormat;
  
  private List<String> mappings;
  
  private boolean fast = false;
  


}
