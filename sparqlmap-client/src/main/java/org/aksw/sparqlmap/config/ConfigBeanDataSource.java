package org.aksw.sparqlmap.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix="ds")
@Data
@Parameters(separators="=")
public class ConfigBeanDataSource {
  
  @NotNull
  @Parameter(names={"-type","--ds.type"})
  private DataSourceType type;
  @NotNull
  @Parameter(names={"-url","--ds.url"},order=1,required=true,description="The location of the ")
  private String url;
  @Parameter(names={"-dbname","--ds.dbName"},description="Additional resources for connecting to a database, that cannot be encoded in the url. Highly endpoint sepcific")
  private String dbName;
  @Parameter(names={"-u","--ds.username"},description="For data sources that require authentication")
  private String username;
  @Parameter(names={"-p","--ds.password"}, password = true, description="For data sources that require authentication")
  private String password;
  @Parameter(names={"--ds.maxPoolSize"})
  private Integer maxPoolSize = 10;
  @Parameter(names={"--ds.quoteChar"}, validateValueWith=CharVal.class, description="CSV specific")
  private String quoteChar = "\"";
  @Parameter(names={"--ds.separatorChar"}, validateValueWith=CharVal.class, description="CSV specific")
  private String separatorChar = ",";
  @Parameter(names={"--ds.escapeChar"}, validateValueWith=CharVal.class,  description="CSV specific")
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
  
  
  public static class StringDatasoureTypeEnumConv implements IStringConverter<DataSourceType>{
    @Override
    public DataSourceType convert(String value) {
      return DataSourceType.valueOf(value.toUpperCase());
    }
  }

  
  public static class CharVal implements IValueValidator<String>{

    @Override
    public void validate(String name, String value) throws ParameterException {
      if(value == null || value.length()!=1){
        throw new ParameterException("Character required, length of value is != 1");
      }
    }

  
  }
  
  

}
