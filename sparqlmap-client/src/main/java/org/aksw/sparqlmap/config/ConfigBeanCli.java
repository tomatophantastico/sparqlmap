package org.aksw.sparqlmap.config;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import lombok.Data;

@Configuration
@ConfigurationProperties
@Data
@Parameters(separators="=")
public class ConfigBeanCli {
  
  @NotNull(message="Specify what you want to do, either: DIRECTMAPPING, DUMP, QUERY or WEB" )
  @Parameter(names={"-a","--action"},required=true,order=0,converter = SparqlMapActionConverter.class, description="Tells SparqlMap what to do: \n\t(web) starts a webserver on port 8090\n\t(dump) dumps the mapped data into STDOUT \n\t(directmapping) dumps a R2RML representation of a direct mapping of the dataset. \n\t(query) executes a query over the dataset and writes the result on STDOUT  ")
  private SparqlMapAction action = SparqlMapAction.WEB;
  
  @Parameter(names={"-q", "--query"}, description="A query to be executed against the virtual datastore")
  private String query;
  

  @Parameter(names={"--queryformat"}, converter=ResultFormatConverter.class,description="Select queries will return this format, for example: csv, xml, json, tsv")
  private ResultsFormat queryFormat = ResultsFormat.FMT_RS_JSON;
  
  @Parameter(names={"-o", "--out"}, description="The location of the dump result file.")
  private String dumpLocation;
  
  
  @Parameter(names={"--format"},converter=RDFFormatConverter.class, description="When creating RDF, use this serialization. Uses jena naming for RDF Formats (see RDFFormat.java)")
  private RDFFormat format = RDFFormat.TURTLE_PRETTY;
  

   
  private boolean fast = false;
  
  public static class SparqlMapActionConverter implements IStringConverter<SparqlMapAction>{

    @Override
    public SparqlMapAction convert(String value) {
      
      return SparqlMapAction.valueOf(value.toUpperCase());
    }
    
  }
  
  public static class ResultFormatConverter implements IStringConverter<ResultsFormat>{

    @Override
    public ResultsFormat convert(String value) {
      ResultsFormat result =  ResultsFormat.lookup(value);
      if(result==null){
        throw new ParameterException("Unknown resultsformat: "+ value);
      }
      
      return result;
    }
    
  }
  
  public static class RDFFormatConverter implements IStringConverter<RDFFormat>{

    @Override
    public RDFFormat convert(String value) {
      
      RDFFormat result =  RDFWriterRegistry.getFormatForJenaWriter(value);
          if(result==null){
            throw new ParameterException("Unknown RDF Format: " + value);
          }
      return result;
    }
    
  }

}
