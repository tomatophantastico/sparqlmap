package org.aksw.sparqlmap.core.errors;

/**
 * Used, if something goes wrong when executing the query.
 * 
 * @author joerg
 *
 */
public class QueryExecutionException extends SparqlMapException {

  public QueryExecutionException(String string) {
    super(string);

  }
  public QueryExecutionException(String string, Exception e) {
    super(string,e);
  }
  
  

}
