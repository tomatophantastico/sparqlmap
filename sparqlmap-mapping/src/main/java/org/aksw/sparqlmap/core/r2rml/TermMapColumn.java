package org.aksw.sparqlmap.core.r2rml;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermMapColumn extends TermMap {

  private String column;

  @Builder
  public TermMapColumn(String lang, String datatypIRI, String termTypeIRI, String column) {
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


  
}
