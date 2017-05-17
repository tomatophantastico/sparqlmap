package org.aksw.sparqlmap.core.mapper.compatibility;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapLoader;
import org.aksw.sparqlmap.core.r2rml.TermMapReferencing;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.aksw.sparqlmap.core.util.JenaHelper;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.jooq.lambda.tuple.Tuple2;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import com.google.common.collect.Lists;

public class CompatibilityChecker {
  
  
	
  
  

 public static boolean isCompatible(TermMap termMap1, TermMap termMap2){
   return requires(termMap1, termMap2).isPresent();
 }
	
  public static Optional<List<CompatibilityRequires>> requires(TermMap termMap1, TermMap termMap2) {
    Optional<List<CompatibilityRequires>> isCompatible;// = Optional.of(Lists.newArrayList());
    if(termMap1==TermMap.NULLTERMMAP || termMap2 == TermMap.NULLTERMMAP){
      isCompatible = Optional.empty();
    }else 
    //do basic checking on TermMap attributes
    if(!(Objects.equals(termMap1.getTermTypeIRI(), termMap2.getTermTypeIRI())
        &&  Objects.equals(termMap1.getDatatypIRI(), termMap2.getDatatypIRI())
        &&  Objects.equals(termMap1.getLang(), termMap2.getLang()))){
      
      isCompatible = Optional.empty();
    }else{
      // we can do more checking by evaluating the subclasses
      if(termMap1.isConstant()){
        if(termMap2.isConstant()){
          isCompatible =_requires((TermMapConstant) termMap1, (TermMapConstant) termMap2);

        }else if(termMap2.isColumn()){
          isCompatible =_requires((TermMapConstant) termMap1, (TermMapColumn) termMap2);

        }else if(termMap2.isReferencing()){
          isCompatible =_requires((TermMapConstant) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_requires((TermMapConstant) termMap1, (TermMapTemplate) termMap2);
        }
      }else if(termMap1.isColumn()){
        if(termMap2.isConstant()){
          isCompatible =_requires((TermMapConstant) termMap2, (TermMapColumn) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_requires((TermMapColumn) termMap1, (TermMapColumn) termMap2);

        }else if(termMap2.isReferencing()){
          isCompatible =_requires((TermMapColumn) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_requires((TermMapColumn) termMap1, (TermMapTemplate) termMap2);
        }
      }else if(termMap1.isReferencing()){
        if(termMap2.isConstant()){
          isCompatible =_requires((TermMapConstant) termMap2, (TermMapReferencing) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_requires((TermMapColumn) termMap2, (TermMapReferencing) termMap1);

        }else if(termMap2.isReferencing()){
          isCompatible =_requires((TermMapReferencing) termMap1, (TermMapReferencing) termMap2);

        }else{
          isCompatible =_requires((TermMapReferencing) termMap1, (TermMapTemplate) termMap2);
        }
      }else{
        if(termMap2.isConstant()){
          isCompatible =_requires((TermMapConstant) termMap2, (TermMapTemplate) termMap1);

        }else if(termMap2.isColumn()){
          isCompatible =_requires((TermMapColumn) termMap2, (TermMapTemplate) termMap1);

        }else if(termMap2.isReferencing()){
          isCompatible =_requires((TermMapReferencing) termMap2, (TermMapTemplate) termMap1);

        }else{
          isCompatible =_requires((TermMapTemplate) termMap1, (TermMapTemplate) termMap2);
        }
      }
      
    }
 
    return isCompatible;

  }
	  
  private static Optional<List<CompatibilityRequires>> _requires(TermMapTemplate termMap1, TermMapTemplate termMap2) {
    
    return isCompatible(termMap1, termMap2);
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapReferencing termMap1, TermMapTemplate termMap2) {
    return requires(termMap1.getParent().getSubject(),termMap2);
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapReferencing termMap1, TermMapReferencing termMap2) {
    return requires(termMap1.getParent().getSubject(),termMap2.getParent().getSubject());
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapColumn termMap1, TermMapTemplate termMap2) {
    // as the RDF-Term matches, we say yes here
    return Optional.of(Lists.newArrayList());
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapColumn termMap1, TermMapReferencing termMap2) {
    return requires(termMap1, termMap2.getParent().getSubject());
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapColumn termMap1, TermMapColumn termMap2) {
    // as the RDF-Term matches, we say yes here

    return Optional.of(Lists.newArrayList());
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapConstant termMap1, TermMapTemplate termMap2) {
    //we convert the constant into a template and eval them
    
    
    return isCompatible(asTermMap(termMap1), termMap2);
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapConstant termMap1, TermMapReferencing termMap2) {
    return requires(termMap1, termMap2.getParent().getSubject());
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapConstant termMap1, TermMapColumn termMap2) {
    // as the RDF-Term matches, we say yes here
    return Optional.of(Lists.newArrayList(CompatibilityRequires.builder().column(termMap2.getColumn()).value(termMap1.getConstant()).build()));
  }

  private static Optional<List<CompatibilityRequires>> _requires(TermMapConstant termMap1, TermMapConstant termMap2) {
    if(termMap1.equals(termMap2)){
      return Optional.of(Lists.newArrayList());
    }else{
      return Optional.empty();
    }
    

  }

  /* simple implementation that checks only for simple equals statements 
   */
  public static boolean isCompatible(TermMap termMap1, String var, Collection<Expr> exprs) {
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


	



		
	private static Optional<List<CompatibilityRequires>> isCompatible(TermMapTemplate tmt1, TermMapTemplate tmt2){

	  TermMapTemplateWalker tmw = new TermMapTemplateWalker(tmt1, tmt2);
	  return tmw.eval();
	}
	
	

	
	


	public static boolean isCompatible(TermMap tm, Node n){
	  return requires(tm,n).isPresent();
	}

		
	public static Optional<List<CompatibilityRequires>> requires(TermMap tm, Node n) {
	  Optional<List<CompatibilityRequires>> result;
	  
		if(n.isVariable()){
			result = Optional.of(Lists.newArrayList());
			
		}else if(n.isLiteral()){
			result =  requires(tm, TermMapLoader.termMapFromLiteralNode((Node_Literal)n));
			
		}else if(n.isURI()){
			result =  requires(tm, TermMapLoader.termMapFromNodeUri( n.getURI()));

		} else{
		  // this is a blank node
		  result =  Optional.empty();
		}
		
		return result;
	}


	
	private static TermMapTemplate asTermMap(TermMap termMap){
	  List<TermMapTemplateTuple> result = Lists.newArrayList();
	  
	  
	  if(termMap.isConstant()){
	    TermMapConstant tc = (TermMapConstant) termMap;
	    String prefix = Optional.ofNullable(tc.getConstantIRI()).orElse(tc.getConstantLiteral());
	    result.add(
	        TermMapTemplateTuple.builder().prefix(prefix).build());
	  }else if(termMap.isColumn()){
	    result.add(TermMapTemplateTuple.builder()
	        .column(((TermMapColumn)termMap).getColumn())
	        .colUrlEncoding(false).build());
	  }else{
	    throw new IllegalArgumentException("not column or constant)");
	  }
	  
	  
	  
	  
	  return TermMapTemplate.builder()
	      .datatypIRI(termMap.getDatatypIRI())
	      .termTypeIRI(termMap.getTermTypeIRI())
	      .template(result)
	      .lang(termMap.getLang())
	      .build();
 
	}
	
	
	public static boolean isCompatible(Quad quad, QuadMap qm){
	  
	  boolean res = true;
	  for(QuadPosition pos : QuadPosition.values()){
	    if(!isCompatible(qm.get(pos),JenaHelper.getField(quad, pos))){
	      res = false;
	      break;
	    }
	  }
	  
	  
	  return res;
	}
	
	
	
}
	

