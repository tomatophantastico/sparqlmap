package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.aksw.sparqlmap.core.errors.ImplementationException;

import com.google.common.collect.Lists;

import lombok.Data;

@Data
//@AllArgsConstructor
public abstract class TermMap {

  private Optional<String> lang;
  private Optional<String> datatypIRI;
  private String termTypeIRI;
  
  public TermMap(Optional<String> lang, Optional<String> datatypIRI, String termTypeIRI) {
    super();
    if(termTypeIRI==null){
      throw new ImplementationException("provide termtype");
    }
    this.lang = lang;
    this.datatypIRI = datatypIRI;
    this.termTypeIRI = termTypeIRI;
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

  
 

 
  public final static  TermMap NULLTERMMAP = new TermMap(null,null,SM.NULLRESOURCE_STRING) {
    
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
  };
  
  public static Collection<String> getCols(TermMap tm){
 
    List<String> cols = Lists.newArrayList();
    
    if(tm instanceof TermMapColumn){
      cols.add(((TermMapColumn) tm).getColumn());
    }else if (tm instanceof TermMapTemplate){
      for(TermMapTemplateTuple tmtt : ((TermMapTemplate) tm).getTemplate()){
        if(tmtt.getColumn()!=null){
          cols.add(tmtt.getColumn());
        }
       
      }
    }
    
    return cols;
  }
}