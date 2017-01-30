package org.aksw.sparqlmap.core.errors;

/**
 * Indicates a problem binding the R2RML model to the underlying database.
 * 
 * @author joerg
 *
 */
public class BindingException extends SparqlMapException {

  public BindingException(String string, Exception e) {
    super(string,e);
  }
  public BindingException(String string) {
    super(string);
  }

}
