package org.aksw.sparqlmap.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Charsets;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix="ds")
@Data
public class DataSourceConfig {
  
  private DataSourceType type;
  private String url;
  private String dbName;
  private String username;
  private String password;
  private Integer maxPoolSize = 10;
  private char quoteChar = '"'; 
  private char separatorChar = ',';
  private char escapeChar = '\\'; 
  private String encoding = "UTF-8";
  private Integer columnNameLineNumber = 1;
  private Boolean failOnInconsistentRowLength = true;
  private Boolean multiLineValues = false;
  private Boolean skipEmptyLines = true;
  private Boolean skipEmptyColumns = true;

}
