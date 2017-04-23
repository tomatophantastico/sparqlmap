package org.aksw.sparqlmap.core.r2rml;

import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapTemplate  extends TermMap{

  private List<TermMapTemplateTuple> template;

  @Builder 
  private TermMapTemplate(Optional<String> lang, Optional<String> datatypIRI, String termTypeIRI, 
      List<TermMapTemplateTuple> template, Optional<String> condition, Optional<String> transform) {
    super(lang, datatypIRI, termTypeIRI, condition, transform);
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

  
  public static class TermMapTemplateBuilder{
    Optional<String> datatypIRI = Optional.empty();
    Optional<String> lang = Optional.empty();
  }
  
  

  
}
