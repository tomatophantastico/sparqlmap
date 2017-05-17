package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.Optional;

import org.aksw.sparqlmap.core.schema.LogicalColumn;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermMapColumn extends TermMap {

  private LogicalColumn column;

  @Builder
  public TermMapColumn(String lang, String datatypIRI, String termTypeIRI, LogicalColumn column, String condition, String transform) {
    super(lang, datatypIRI, termTypeIRI, condition, transform);
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
  
  
  @Override
  public Collection<LogicalColumn> getColumns() {
    return Lists.newArrayList(column);
  }
  
 

  
}
