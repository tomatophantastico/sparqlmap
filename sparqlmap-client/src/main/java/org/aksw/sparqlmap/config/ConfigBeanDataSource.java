package org.aksw.sparqlmap.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix="ds")
@Parameters(separators="=")
@Data
public class ConfigBeanDataSource {
  
  @NotNull
  @Parameter(names={"-type","--ds.type"}, order = 1, required = true)
  private DataSourceType type;
  @NotNull
  @Parameter(names={"-url","--ds.url"},order=2,required=true,description="The location of the data source")
  private String url;
  @Parameter(names={"-dbname","--ds.dbName"},description="Additional resources for connecting to a database, that cannot be encoded in the url. Highly endpoint sepcific")
  private String dbName;
  @Parameter(names={"-u","--ds.username"},description="For data sources that require authentication")
  private String username;
  @Parameter(names={"-p","--ds.password"}, description="For data sources that require authentication")
  private String password;
  @Parameter(names={"--ds.maxPoolSize"})
  private Integer maxPoolSize = 10;
  @Parameter(names={"--ds.quoteChar"}, description="CSV specific")
  private String quoteChar = "\"";
  @Parameter(names={"--ds.separatorChar"}, description="CSV specific")
  private String separatorChar = ",";
  @Parameter(names={"--ds.escapeChar"},  description="CSV specific")
  private String escapeChar = "\\";
  @Parameter(names={"--ds.encoding"})
  private String encoding = "UTF-8";
  @Parameter(names={"--ds.columnNameLineNumber"}, description="CSV specific")
  private Integer columnNameLineNumber = 1;
  @Parameter(names={"--ds.failOnInconsistentRowLength"}, description="CSV specific")
  private Boolean failOnInconsistentRowLength = true;
  @Parameter(names={"--ds.multiLineValues"}, description="CSV specific")
  private Boolean multiLineValues = false;
  @Parameter(names={"--ds.skipEmptyLines"}, description="CSV specific")
  private Boolean skipEmptyLines = true;
  @Parameter(names={"--ds.skipEmptyColumns"}, description="CSV specific")
  private Boolean skipEmptyColumns = true;

}
