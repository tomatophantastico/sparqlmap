package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.aksw.sparqlmap.core.schema.LogicalColumn;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
//@AllArgsConstructor
public abstract class TermMap {

  private String lang;
  private String datatypIRI;
  private String termTypeIRI;
  private String condition;
  private String transform;
  
  @Setter(value = AccessLevel.NONE)  
  private Optional<Pattern> conditionPattern;
  
  
  public TermMap(String lang, String datatypIRI, String termTypeIRI, String condition, String transform) {
    super();
    if(termTypeIRI==null){
      throw new ImplementationException("provide termtype");
    }
    this.lang = lang;
    this.datatypIRI = datatypIRI;
    this.termTypeIRI = termTypeIRI;
    this.transform = transform;
    this.condition = condition;
    this.conditionPattern = Optional.ofNullable(condition).map(Pattern::compile);
  }
  
  
  
  public boolean isIRI(){
    return termTypeIRI.equals(R2RML.IRI_STRING);
  }
  public boolean isLiteral(){
    return termTypeIRI.equals(R2RML.LITERAL_STRING);
  }
  public boolean isBlank(){
    return termTypeIRI.equals(R2RML.BLANKNODE_STRING);
  } 
  
  public abstract boolean isConstant();
  public abstract boolean isColumn();
  public abstract boolean isTemplate();
  public abstract boolean isReferencing();

  public void setCondition(String condition){
    this.condition = condition;
    this.conditionPattern = Optional.ofNullable(condition).map(Pattern::compile);
    
  }
  
 

 
  public final static  TermMap NULLTERMMAP = new TermMap(null,null,SMAP.NULLRESOURCE_STRING,null,null) {
    
    @Override
    public boolean isTemplate() {
      return false;
    }
    
    @Override
    public boolean isReferencing() {
      return false;
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
    public Collection<LogicalColumn> getColumns() {
      return Lists.newArrayList();
    }
  };
  
  public abstract Collection<LogicalColumn> getColumns();
  
  
  public Collection<String> getColumnNames(){
    return getColumns().stream().map(lcol -> lcol.getName()).collect(Collectors.toList());
  }
  
  
  
}