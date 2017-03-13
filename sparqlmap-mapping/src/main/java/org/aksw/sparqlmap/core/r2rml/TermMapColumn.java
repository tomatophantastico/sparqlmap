package org.aksw.sparqlmap.core.r2rml;

import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermMapColumn extends TermMap {

  private String column;

  @Builder
  public TermMapColumn(Optional<String> lang, Optional<String> datatypIRI, String termTypeIRI, String column) {
    super(lang, datatypIRI, termTypeIRI);
    this.column = column;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public boolean isColumn() {
    return true;
  }

  @Override
  public boolean isTemplate() {
    return false;
  }

  @Override
  public boolean isReferencing() {
    return false;
  }
  
  public static class TermMapColumnBuilder{
    Optional<String> datatypIRI = Optional.empty();
    Optional<String> lang = Optional.empty();
  }


  
}
