package org.aksw.sparqlmap.core;

public class TranslationContextJDBC {
  
  private TranslationContext context;
  
  public int subquerycounter = 0;
  
  public int aliascounter = 0 ; 
  
  
  private String sqlQuery;

  public TranslationContextJDBC(TranslationContext context) {
    super();
    this.context = context;
  }

  public TranslationContext getContext() {
    return context;
  }

  public void setContext(TranslationContext context) {
    this.context = context;
  }
  
  public String getSqlQuery() {
    return sqlQuery;
  }

  public void setSqlQuery(String sqlQuery) {
    this.sqlQuery = sqlQuery;
  }
  

  public int getAndIncrementSubqueryCounter() {
   return this.subquerycounter++;
  }
  
  
  

}
