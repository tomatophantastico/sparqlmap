package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.schema.LogicalColumn;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapTemplate  extends TermMap{

  private List<TermMapTemplateTuple> template;

  @Builder 
  private TermMapTemplate(String lang, String datatypIRI, String termTypeIRI, 
      List<TermMapTemplateTuple> template, String condition, String transform) {
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
  
  
  @Override
  public Collection<LogicalColumn> getColumns() {
    return template.stream()
        .map(ttt -> ttt.getColumn())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }


  

  
}
