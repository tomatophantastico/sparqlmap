package org.aksw.sparqlmap.common;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties
@Data
public class BaseConfig {
  
 
  private String dsUsername;
  private String dsPassword;
  @NotNull(message="Define the location of the data source, according to the Data Source type, for example a file location or a JDBC-URL")
  private String dsLocation;
  private String dsIdentifier;
  private Integer maxPoolSize = 10;
  
  @NotNull(message="Provide a Data Source Type e.g. JDBC, CSV or ACCESS")
  private DataSourceType dsType;
  
  private String baseiri;
  
  private String r2rmlfile;
  
  private String dmBaseUriPrefix;
  private String dmMappingUriPrefix;
  private String dmVocabUriPrefix;
  private String dmInstanceUriPrefix;
  private String dmSeparatorChar;


}
