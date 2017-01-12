package org.aksw.sparqlmap.common;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties
@Data
public class BaseConfig {
  
 
  
  private Integer jdbcMaxPoolSize = 10;
  
  
  private String baseiri;
  
  private String r2rmlfile;
  
  private String dmBaseUriPrefix;
  private String dmMappingUriPrefix;
  private String dmVocabUriPrefix;
  private String dmInstanceUriPrefix;
  private String dmSeparatorChar;


}
