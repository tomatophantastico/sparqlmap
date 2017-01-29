package org.aksw.sparqlmap.core.r2rml;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TermMapReferencing extends TermMap {

  private QuadMap parent;
  
  private String parentMapUri;

  private List<JoinOn> conditions;
  
  @Builder
  public TermMapReferencing(String lang, String datatypIRI, String termTypeIRI,  QuadMap parent,
      String parentMapUri, List<JoinOn> conditions) {
    super(lang, datatypIRI, termTypeIRI);
    this.parent = parent;
    this.parentMapUri = parentMapUri;
    this.conditions = conditions;
  }
  
  
  
  
  @Data
  public static class JoinOn {

    private String childColumn;
    private String parentColumn;
   
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
    return false;
  }

  @Override
  public boolean isReferencing() {
    return true;
  }
  
  
  
  
  
  
}
