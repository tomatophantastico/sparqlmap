package org.aksw.sparqlmap.core.translate.metamodel;

import java.util.Iterator;
import java.util.List;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapReferencing;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;

import com.google.common.collect.Lists;

/**
 * This class translates tuples from {@link Row}s into RDF {@link Node}s.
 * 
 * @author joerg
 *
 */
public class MetaModelRowBinder {
  
  
  
  
  public MetaModelRowBinder(String baseuri) {
    super();
    this.baseuri = baseuri;
  }

  private String baseuri;

  public Node bind( List<SelectItem> selects, TermMap termMap, Row row){
    Node result  = null;
    
    if(termMap instanceof TermMapConstant){
      result = bind(selects, (TermMapConstant) termMap, row);
    }else if (termMap instanceof TermMapColumn){
      result = bind(selects, (TermMapColumn) termMap, row);
    } else if(termMap instanceof TermMapTemplate){
      result = bind(selects, (TermMapTemplate) termMap, row);
    } else if(termMap instanceof TermMapReferencing){
      result = bind(selects, (TermMapReferencing) termMap, row);

    }
    
    
    return result;
  }
  

  private Node bind(List<SelectItem> selects, TermMapConstant termMap, Row row) {
    Node result = null;
    if(termMap.getTermTypeIRI().equals(R2RML.IRI_STRING)){
      result = NodeFactory.createURI(termMap.getConstantIRI());
    }else if(termMap.getTermTypeIRI().equals(R2RML.BLANKNODE_STRING)) {
      result = NodeFactory.createBlankNode(Integer.toString(termMap.hashCode()));
    }else {
    
        result = NodeFactory.createLiteral(
            termMap.getConstantLiteral(),
            termMap.getLang(),
            new BaseDatatype(termMap.getDatatypIRI())); 
    }
    return result;
    
  }

  
  private Node bind(List<SelectItem> selects, TermMapColumn termMap, Row row) {
    
    Node result = null;
    Object content =  row.getValue(selects.get(0));
    
    
    if(termMap.getTermTypeIRI().equals(R2RML.IRI_STRING)){
      //it might be neccesary to prefix here
      String contentString = content.toString();
      
      if(IRIResolver.checkIRI(contentString)){
        result = NodeFactory.createURI(contentString);
      }else{
        result = NodeFactory.createURI(baseuri + contentString);
      }
      
    }else if(termMap.getTermTypeIRI().equals(R2RML.BLANKNODE_STRING)) {

      result = NodeFactory.createBlankNode(content.toString());
    }else{
      
      NodeFactory.createLiteral(
          content.toString(), 
          termMap.getLang(), 
          new BaseDatatype(termMap.getDatatypIRI()));      
    }
    
    return result;
    
  }
  
  private Node bind(List<SelectItem> selects,TermMapTemplate termMap, Row row) {
    Node result = null;
    List<Object> contents = Lists.newArrayList();
    for(SelectItem select: selects){
      contents.add(row.getValue(select));
    }
    Iterator<Object> contentIter = contents.iterator();
    
    StringBuilder contentSb = new StringBuilder();
    for(TermMapTemplateTuple sc: termMap.getTemplate()){
      if(sc.getPrefix()!=null){
        contentSb.append(sc.getPrefix());
      }
      if(sc.getColumn()!=null){
        contentSb.append(contentIter.next());
      }
    }  
    String content = contentSb.toString();
    
    
    
    if(termMap.getTermTypeIRI().equals(R2RML.IRI_STRING)){
      //it might be neccesary to prefix here
     
      
      if(IRIResolver.checkIRI(content)){
        result = NodeFactory.createURI(content);
      }else{
        result = NodeFactory.createURI(baseuri + content);
      }
      
    }else if(termMap.getTermTypeIRI().equals(R2RML.BLANKNODE_STRING)) {

      result = NodeFactory.createBlankNode(content);
    }else{
      
      NodeFactory.createLiteral(
          content, 
          termMap.getLang(), 
          new BaseDatatype(termMap.getDatatypIRI()));      
    }
    
    return result;
  }
  
  private Node bind(List<SelectItem> selects, TermMapReferencing termMap, Row row) {
    
    
    
    
    return bind(selects,termMap.getParent().getSubject(),row);
    
  }

  
  
  
  
}
