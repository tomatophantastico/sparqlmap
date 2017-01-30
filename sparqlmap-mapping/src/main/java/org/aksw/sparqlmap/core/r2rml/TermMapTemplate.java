package org.aksw.sparqlmap.core.r2rml;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapTemplate  extends TermMap{

  private List<TermMapTemplateTuple> template;

  @Builder 
  private TermMapTemplate(String lang, String datatypIRI, String termTypeIRI, 
      List<TermMapTemplateTuple> template) {
    super(lang, datatypIRI, termTypeIRI);
    this.template = template;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public boolean isColumn() {
    return false;
  }

  @Override
  public boolean isTemplate() {
    return true;
  }

  @Override
  public boolean isReferencing() {
    return false;
  }

  
  

  
}
