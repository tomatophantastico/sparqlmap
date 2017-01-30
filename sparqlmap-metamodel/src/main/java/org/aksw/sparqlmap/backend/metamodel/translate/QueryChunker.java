package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.List;
import java.util.Stack;

import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorByType;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;

import com.google.common.collect.Lists;

/**
 * The query chunker
 * 
 * @author joerg
 *
 */

public  class QueryChunker {
  
  public static List<Op>  chunkQuery(Op op){
        
    UnionResolver resolv = new UnionResolver();
    
    OpWalker.walk(op, resolv);
    
   
    
    return resolv.getResult();
    
  }
  
  
  
  /**
   * converts unions in queries into separate queries.
   * 
   * Adapted from {@link ApplyTransformVisitor}
   * 
   * @author joerg
   *
   */
  private static class UnionResolver extends OpVisitorByType{
    
    
    List<Stack<Op>> opStacks;
    
    /*
     * pop all, return just one, they sould be identical
     */
    private Op pop(){
      Op result= null;
      for(Stack<Op> opStack : opStacks){
        result = opStack.pop();
      }
      
      return result;
    }
    /*
     * push the op on all stacks
     */
    private void push(Op op){
      for(Stack<Op> opStack: opStacks){
        opStack.push(op);
      }
    }
   

    @Override
    protected void visitN(OpN op) {
      for(Op subOp: op.getElements()){
        pop();
      }
      push(op);
    }



    @Override
    protected void visit2(Op2 op) {
       pop();
       pop();
       push(op);
    }



    @Override
    protected void visit1(Op1 op) {
      pop();
      push(op);
    }



    @Override
    protected void visit0(Op0 op) {
      push(op);
    }



    @Override
    protected void visitExt(OpExt op) {
      //do nothin
    }



    @Override
    protected void visitFilter(OpFilter op) {
      visit1(op);
    }



    @Override
    protected void visitLeftJoin(OpLeftJoin op) {
     visit2(op);
      
    }
    
    
    
    public List<Op> getResult(){
      List<Op> result = Lists.newArrayList();
      for(Stack<Op> opStack :opStacks){
        if(opStack.size()!=1){
          throw new ImplementationException("Stack misaligned");
        }
        
        result.add(opStack.pop());
      }
      
      return result;
    }


    
    
  }
  

}
