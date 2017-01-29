package org.aksw.sparqlmap.core.algebra;

import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprVisitorBase;

public class QuadVisitorBase extends OpVisitorBase{
  
  
  private boolean visitExists = true;
  
  
  public void setVisitExists(boolean visitExists) {
    this.visitExists = visitExists;
  }
  
	
	@Override
	public void visit(OpBGP opBGP) {
		throw new ImplementationException("Move to quad");
	}
	
	
	@Override
	public void visit(OpGraph opGraph) {
		throw new ImplementationException("Move to quad");
	}
	
		
	@Override
	public void visit(OpQuadBlock quadBlock) {
		for(OpQuadPattern pattern : quadBlock.convert()){
			visit(pattern);
		}
	}
	
	
	
	@Override
	public void visit(OpFilter opFilter) {
	  //check the filter for exists
	  
	  for(Expr expr: opFilter.getExprs().getList()){
	    expr.visit(new OpInExpressionBridge(this));
	  }
	 
	  super.visit(opFilter);
	  
	  
	  
	}
	
	
	
	private class OpInExpressionBridge extends ExprVisitorBase{
	  
	  
	  QuadVisitorBase quadvisitor;
	  
	  
	 
	  public OpInExpressionBridge(QuadVisitorBase quadvisitor) {
      super();
      this.quadvisitor = quadvisitor;
    }


    @Override
	  public void visit(ExprFunctionOp func) {
      if(visitExists){
        OpWalker.walk(func.getGraphPattern(), quadvisitor);
      }
      
	  }
	  
	  
	  
	  
	}
	
	
	

	
}
