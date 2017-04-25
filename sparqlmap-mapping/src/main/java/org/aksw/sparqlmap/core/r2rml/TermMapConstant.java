package org.aksw.sparqlmap.core.r2rml;

import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapConstant  extends TermMap{
  
  private String constantLiteral;
  private String constantIRI;
  
  @Builder
  public TermMapConstant(Optional<String> lang, Optional<String> datatypIRI, String termTypeIRI,  String constantLiteral,
      String constantIRI,String condition, String transform) {
    
    super(lang, datatypIRI, termTypeIRI, condition, transform);
    
    this.constantLiteral = constantLiteral;
    this.constantIRI = constantIRI;
  }
  
  public String getConstant(){
    return constantIRI!=null?constantIRI:constantLiteral;
  }

  @Override
  public boolean isConstant() {
    return true;
  }

  @Override
  public boolean isColumn() {
    return false;
  }

  @Override
  public boolean isTemplate() {
    return false;
  }

  @Override
  public boolean isReferencing() {
    return false;
  }
  
  
  public static class TermMapConstantBuilder{
    Optional<String> datatypIRI = Optional.empty();
    Optional<String> lang = Optional.empty();
  }

 
  
 
}
