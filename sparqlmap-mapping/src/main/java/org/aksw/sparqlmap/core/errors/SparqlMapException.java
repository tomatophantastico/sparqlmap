package org.aksw.sparqlmap.core.errors;
/**
 * 
 * The Base SparqlMap exception.
 * 
 * @author joerg
 *
 */
public class SparqlMapException extends RuntimeException {

  public SparqlMapException(String string) {
    super(string);
  }

  public SparqlMapException(String string, Exception e) {
    super(string,e);
  }

}
