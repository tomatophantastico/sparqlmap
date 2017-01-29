package org.aksw.sparqlmap;

import java.net.URL;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class R2RMLTestParameter {
  
  String testCaseName;
  String r2rmlLocation;
  String outputLocation; 
  String referenceOutput;
  String dbFileLocation; 
  boolean createDM;
  String dbname;
  
  
  
  public String toString(){
    return testCaseName;
  }

  
}