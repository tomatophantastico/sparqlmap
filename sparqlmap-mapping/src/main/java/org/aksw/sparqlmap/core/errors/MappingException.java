package org.aksw.sparqlmap.core.errors;

/**
 * Indicates an invalid mapping.
 * 
 * @author joerg
 * 
 */
public class MappingException extends SparqlMapException {
  
  /**
   * Constructs a new Exception. 
   * 
   * @param string the reason for the failure
   */
  public MappingException(String string) {
    super(string);
  }

}
