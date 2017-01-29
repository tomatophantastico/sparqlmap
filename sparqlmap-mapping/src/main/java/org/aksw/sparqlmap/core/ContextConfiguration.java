package org.aksw.sparqlmap.core;

public class ContextConfiguration {
  
  
  private boolean continueWithInvalidUris = true;
  private String baseUri = "http://localhost/";
  
  
  
  public boolean isContinueWithInvalidUris() {
    return continueWithInvalidUris;
  }
  public void setContinueWithInvalidUris(boolean continueWithInvalidUris) {
    this.continueWithInvalidUris = continueWithInvalidUris;
  }
  public String getBaseUri() {
    return baseUri;
  }
  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }
  

  
  

}
