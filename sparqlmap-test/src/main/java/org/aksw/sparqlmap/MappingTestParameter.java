package org.aksw.sparqlmap;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MappingTestParameter {

  private String testname;
  private String dsName;
  private File mappingFile;
  private Collection<File> sqlFiles;
  private String query;
  
  
  
  
  
  public String toString(){
    return testname;
  }

}
