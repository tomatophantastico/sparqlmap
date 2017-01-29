package org.aksw.sparqlmap.core.errors;

/**
 * This Exception is thrown when not implemented modules of SparqlMap are called.
 * 
 * @author joerg
 * 
 */
public class ImplementationException extends SparqlMapException {
  
  /**
   * Only construct with a message.
   * @param msg the message
   */
  public ImplementationException(String msg) {
    super(msg);
  }

}
