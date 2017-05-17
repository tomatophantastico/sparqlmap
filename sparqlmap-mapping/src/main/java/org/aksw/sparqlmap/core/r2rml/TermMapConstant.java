package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.Optional;

import org.aksw.sparqlmap.core.schema.LogicalColumn;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapConstant  extends TermMap{
  
  private String constantLiteral;
  private String constantIRI;
  
  @Builder
  public TermMapConstant(String lang, String datatypIRI, String termTypeIRI,  String constantLiteral,
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

  @Override
  public Collection<LogicalColumn> getColumns() {
    return Lists.newArrayList();
  }
  
 
 
  
 
}
