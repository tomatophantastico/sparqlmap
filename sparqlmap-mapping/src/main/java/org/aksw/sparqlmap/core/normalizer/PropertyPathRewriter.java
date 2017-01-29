package org.aksw.sparqlmap.core.normalizer;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;

public class PropertyPathRewriter extends TransformCopy {
  
  
  private TranslationContext context;
  
  
  
  
  

  
  
  public PropertyPathRewriter(TranslationContext context) {
    super();
    this.context = context;
  }


  
  @Override
  public Op transform(org.apache.jena.sparql.algebra.op.OpPath opPath) {
    Op result = null;
        
    if(opPath.getTriplePath().getPath() instanceof P_OneOrMore1){
      TriplePath triplePath = opPath.getTriplePath();
      P_OneOrMore1 path  = (P_OneOrMore1) opPath.getTriplePath().getPath();
      Path subPath  = path.getSubPath();
      
      if(subPath instanceof P_Path0){
        Node predicate  =((P_Path0) subPath).getNode();
        result = rewritePlusPropertyPath(5, triplePath.getSubject(),predicate,triplePath.getObject());

      }
      
    }
    
    if(result == null){
      throw new ImplementationException("Property path not supported.");
    }
    
    
    return result;
  }
  
  
  private OpUnion rewritePlusPropertyPath(int depth, Node subject, Node predicate, Node object){
    
    if(depth<1){
      throw new IllegalArgumentException("depth must be at least 1");
    }
    
    OpUnion union = null;
    
    
    if(depth>1){
      union = new OpUnion(rewritePlusPropertyPath(depth-1, subject, predicate, object), createPredicateBGPs(depth-1, subject, predicate, object));
    }else{
      union = new OpUnion(createPredicateBGPs(0, subject, predicate, object), createPredicateBGPs(1, subject, predicate, object));
      
    }
    
    
    return union;
    
    
  }

  
  private OpBGP createPredicateBGPs(int length, Node subject, Node predicate, Node object){
    BasicPattern pattern = new BasicPattern();
    if(length==0){
      pattern.add(new Triple(subject,predicate,object));
    }else{
      
      context.getAndIncrementDuplicateCounter();
      String prefix = context.getPropertyPathPrefix();
      
      Node o =  NodeFactory.createVariable(prefix + "0");

      Triple first = new Triple(subject,predicate,o);
      pattern.add(first);
      
      for(int i =1 ;i<length ;i++ ){
        
        Node o_new = NodeFactory.createVariable(prefix + i);
        Triple triple = new Triple(o,predicate,o_new);
        pattern.add(triple);
        o = o_new;     
      }
      
      Triple last = new Triple(o,predicate,object);
      pattern.add(last);
      
    }
    return new OpBGP(pattern);
  }
  
  
  
}
