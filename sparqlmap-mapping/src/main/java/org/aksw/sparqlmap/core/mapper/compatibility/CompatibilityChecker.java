package org.aksw.sparqlmap.core.mapper.compatibility;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapReferencing;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import com.google.common.collect.Lists;

public class CompatibilityChecker {
  
  
	
  
  

 
	
  public boolean isCompatible(TermMap termMap1, TermMap termMap2) {
    boolean isCompatible = true;
    if(termMap1==TermMap.NULLTERMMAP || termMap2 == TermMap.NULLTERMMAP){
      isCompatible = false;
    }else 
    //do basic checking on TermMap attributes
    if(!(Objects.equals(termMap1.getTermTypeIRI(), termMap2.getTermTypeIRI())
        &&  Objects.equals(termMap1.getDatatypIRI(), termMap2.getDatatypIRI())
        &&  Objects.equals(termMap1.getLang(), termMap2.getLang()))){
      
      isCompatible = false;
    }else{
      // we can do more checking by evaluating the subclasses
      if(termMap1.isConstant()){
        if(termMap2.isConstant()){
          isCompatible =_isCompatible((TermMapConstant) termMap1, (TermMapConstant) termMap2);

        }else if(termMap2.isColumn()){
          isCompatible =_isCompatible((TermMapConstant) termMap1, (TermMapColumn) termMap2);

        }else if(termMap2.isReferencing()){
          isCompatible =_isCompatible((TermMapConstant) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_isCompatible((TermMapConstant) termMap1, (TermMapTemplate) termMap2);
        }
      }else if(termMap1.isColumn()){
        if(termMap2.isConstant()){
          isCompatible =_isCompatible((TermMapConstant) termMap2, (TermMapColumn) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_isCompatible((TermMapColumn) termMap1, (TermMapColumn) termMap2);

        }else if(termMap2.isReferencing()){
          isCompatible =_isCompatible((TermMapColumn) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_isCompatible((TermMapColumn) termMap1, (TermMapTemplate) termMap2);
        }
      }else if(termMap1.isReferencing()){
        if(termMap2.isConstant()){
          isCompatible =_isCompatible((TermMapConstant) termMap2, (TermMapReferencing) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_isCompatible((TermMapColumn) termMap2, (TermMapReferencing) termMap1);

        }else if(termMap2.isReferencing()){
          isCompatible =_isCompatible((TermMapReferencing) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_isCompatible((TermMapReferencing) termMap1, (TermMapTemplate) termMap2);
        }
      }else{
        if(termMap2.isConstant()){
          isCompatible =_isCompatible((TermMapConstant) termMap2, (TermMapTemplate) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_isCompatible((TermMapColumn) termMap2, (TermMapTemplate) termMap1);

        }else if(termMap2.isReferencing()){
          isCompatible =_isCompatible((TermMapReferencing) termMap2, (TermMapTemplate) termMap1);

        }else{
          isCompatible =_isCompatible((TermMapTemplate) termMap1, (TermMapTemplate) termMap2);
        }
      }
      
    }
    
    
//    return isCompatible         
//    
//    
//    
//
//    if (!termMap1.getTermTypeIRI().equals(termMap2.getTermTypeIRI())) {
//      isCompatible = false;
//    } else {
//      // we compare literal termMaps by comparing the data types.
//      if (termMap1.getTermTypeIRI().equals(R2RML.LITERAL_STRING)) {
//        String datatype1 = termMap1.getDatatypIRI();
//        String datatype2 = termMap2.getDatatypIRI();
//
//        if (!((datatype1 == null && datatype2 == null)
//            || (DataTypeHelper.isNumeric(datatype1) && DataTypeHelper.isNumeric(datatype2)) || (datatype1
//              .equals(datatype2)))) {
//          isCompatible = false;
//        }
//      } if(termMap1.getTermTypeIRI().equals(R2RML.IRI_STRING)){
//        // we check both iris for compatibility by converting everything into tuples
//        
//        
//        
//      }else {
//        // deal with resources here we can treat blank nodes and iri the same
//        // here
//
//      }
//    }
    // it is not the case that both term maps produce numeric values
    return isCompatible;

  }
	  
  private boolean _isCompatible(TermMapTemplate termMap1, TermMapTemplate termMap2) {
    
    return isCompatible(termMap1.getTemplate(), termMap2.getTemplate());
  }

  private boolean _isCompatible(TermMapReferencing termMap1, TermMapTemplate termMap2) {
    return isCompatible(termMap1.getParent().getSubject(),termMap2);
  }

  private boolean _isCompatible(TermMapReferencing termMap1, TermMapReferencing termMap2) {
    return isCompatible(termMap1.getParent().getSubject(),termMap2.getParent().getSubject());
  }

  private boolean _isCompatible(TermMapColumn termMap1, TermMapTemplate termMap2) {
    // as the RDF-Term matches, we say yes here
    return true;
  }

  private boolean _isCompatible(TermMapColumn termMap1, TermMapReferencing termMap2) {
    return isCompatible(termMap1, termMap2.getParent().getSubject());
  }

  private boolean _isCompatible(TermMapColumn termMap1, TermMapColumn termMap2) {
    // as the RDF-Term matches, we say yes here

    return true;
  }

  private boolean _isCompatible(TermMapConstant termMap1, TermMapTemplate termMap2) {
    //we convert the constant into a template and eval them
    
    
    return isCompatible(asTermMapTuple(termMap1), termMap2.getTemplate());
  }

  private boolean _isCompatible(TermMapConstant termMap1, TermMapReferencing termMap2) {
    return isCompatible(termMap1, termMap2.getParent().getSubject());
  }

  private boolean _isCompatible(TermMapConstant termMap1, TermMapColumn termMap2) {
    // as the RDF-Term matches, we say yes here
    return true;
  }

  private boolean _isCompatible(TermMapConstant termMap1, TermMapConstant termMap2) {
    return isCompatible(asTermMapTuple(termMap1), asTermMapTuple(termMap2));

  }

  /* simple implementation that checks only for simple equals statements 
   */
  public boolean isCompatible(TermMap termMap1, String var, Collection<Expr> exprs) {
    boolean isCompatible = true;
    for(Expr expr : exprs){
      if(expr instanceof E_Equals){
        ExprVar exprVar = null;
        NodeValueNode exprValue = null;
        E_Equals equals = (E_Equals) expr;
        if(equals.getArg1() instanceof ExprVar && equals.getArg2() instanceof NodeValue){
          exprVar = (ExprVar) equals.getArg1();
          exprValue = (NodeValueNode) equals.getArg2();
        }
        if(equals.getArg2() instanceof ExprVar && equals.getArg1() instanceof NodeValue){
          exprVar = (ExprVar) equals.getArg2();
          exprValue = (NodeValueNode) equals.getArg1();
        }
        if(exprVar!=null && exprVar.getVarName().equals(var) && exprValue !=null){
          
          //if an equals check is not true, this term map is not compatible to the expression presented here
          isCompatible = isCompatible && isCompatible(termMap1,exprValue.asNode());
        }
      }
    }
    return isCompatible;
  }		


	



		
	protected boolean isCompatible(List<TermMapTemplateTuple> tmtemplate, List<TermMapTemplateTuple> tmtemplate2){
	  
	  
	  PeekingIterator<TermMapTemplateTuple> iTmTemp1 = Iterators.peekingIterator(tmtemplate.iterator());
	  PeekingIterator<TermMapTemplateTuple> iTmTemp2 = Iterators.peekingIterator(tmtemplate2.iterator());
	  
	  TermMapTemplateWalker tmw = new TermMapTemplateWalker(iTmTemp1, iTmTemp2);
	  return tmw.eval();
	  

	}
	
	

	
	


	

		
	public boolean isCompatible(TermMap tm, Node n) {
		if(n.isVariable()){
			return true;
			
		}else if(n.isLiteral()){
		
			return isCompatibleLiteral(tm, n);
			
		}else if(n.isURI()){
			return isCompatibleUri(tm, (Node_URI) n);

		} else{
		  // this is a blank node
		  return false;
		}
	}


	private boolean isCompatibleUri(TermMap termMap, Node_URI n) {
	  boolean result = true;
	  
	   //check if the term map produces a IRI
	  if(!termMap.getTermTypeIRI().equals(R2RML.IRI_STRING)){
	    result = false;
	  } else if(termMap instanceof TermMapConstant){
	  //check constant Term Maps
	    TermMapConstant tmc = (TermMapConstant) termMap;
	    result = tmc.getConstantIRI().equals(n.getURI());
	    
	  }else if(termMap instanceof TermMapColumn){
	  // column based IRI TermMaps are always compatible, so no check here
	  // last option is template based TermMap
	  }else if(termMap instanceof TermMapReferencing){
	    result = isCompatible(((TermMapReferencing)termMap).getParent().getSubject(), n);
	  }else{
	    String nodeUri = n.getURI();
	    TermMapTemplate tmt = (TermMapTemplate) termMap;
	    
	    	    
	    
	    //in this loop the nodeUri String gets compared with the template
	    //we simply check if all static parts of the template are part
	    //of the string, in the right order.
	    
	    int nodeUriPos = 0;
	    for(TermMapTemplateTuple tmtsc:  tmt.getTemplate()){
	      //comparing with the static component
	      
	      String tmString = tmtsc.getPrefix();
	      if(tmString == null||tmString.isEmpty()){
	        // do nothing, if the string is null or empty
	      }else if(nodeUri.indexOf(tmString, nodeUriPos)>=0){
	         //it is contained, so advancing the pointer

	        nodeUriPos = nodeUri.indexOf(tmString, nodeUriPos);
	        
	      }else{  
	        //nodeUriPos is -1, so the string is not there
          result = false;
          break;
        }	      
	    }
	  }
	  return result;
	}
	  
	  

	private boolean isCompatibleLiteral(TermMap termMap, Node n) {
		//if the term map does not produces a Literal, it is not compatible with one.
		if(!termMap.getTermTypeIRI().equals(R2RML.LITERAL_STRING)){
			return false;
		}
		
		//we check for the datatype
		//if exactly one of them is null, then it is false
		if((n.getLiteralDatatypeURI() != null && termMap.getDatatypIRI() ==null) 
				||(n.getLiteralDatatypeURI() == null && termMap.getDatatypIRI() !=null)){
			return false;
		}
		//if they are not null and different
		if((n.getLiteralDatatypeURI() != null && termMap.getDatatypIRI() !=null) && !n.getLiteralDatatypeURI().equals(termMap.getDatatypIRI())){
			return false;
		}
		// if the language does not match
		String nodeLang = n.getLiteralLanguage();
		String termMapLang = termMap.getLang();
		if(!nodeLang.equals(termMapLang)){
		  return false;
		}
		
		
		//otherwise we could give it a try, for now
		return true;
	}
	
	private List<TermMapTemplateTuple> asTermMapTuple(TermMap termMap){
	  List<TermMapTemplateTuple> result = Lists.newArrayList();
	  
	  
	  if(termMap.isConstant()&&termMap.isIRI() ){
	    result.add(TermMapTemplateTuple.builder().prefix(((TermMapConstant) termMap).getConstantIRI()).build());
	  }else if(termMap.isColumn()&&termMap.isIRI()){
	    result.add(TermMapTemplateTuple.builder().column(((TermMapColumn)termMap).getColumn()).colUrlEncoding(false).build());
	  }else{
	    throw new IllegalArgumentException("not an iri and (column or constant)");
	  }
	  
	  
	  return result;
 
	}
	
	
	
}
	

