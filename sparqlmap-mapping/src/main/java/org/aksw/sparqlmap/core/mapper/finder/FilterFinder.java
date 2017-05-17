package org.aksw.sparqlmap.core.mapper.finder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.aksw.sparqlmap.core.algebra.QuadVisitorBase;
import org.aksw.sparqlmap.core.mapper.RightFirstWalker;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class FilterFinder{
	
	
	
	
	
	public static QueryInformation getQueryInformation(Op op) {
		// for every triple, this map holds all expressions that are applicable
		// to its variables.
		QueryInformation qi = new QueryInformation();
		qi.setQuery(op);

		final Multiset<Expr> filterStack = HashMultiset.create();

		RightFirstWalker.walk(op,new Putter(filterStack, qi), new Pusher(filterStack), 
				new Popper(filterStack));

		return qi;

	}
	

	
	
	/**
	 * fills the stack
	 * @author joerg
	 *
	 */
	private static class Pusher extends QuadVisitorBase{
		Multiset<Expr> filterStack;
		public Pusher(Multiset<Expr> filterStack) {
			this.filterStack = filterStack;
		}

		@Override
		public void visit(OpFilter filter){
			for(Expr expr: filter.getExprs().getList()){
				filterStack.add(expr);
			}
			
		}
	}
	/**
	 * removes from the stack
	 * @author joerg
	 *
	 */
	private static class Popper extends QuadVisitorBase{
		
		private Multiset<Expr> filterStack;

		public Popper(Multiset<Expr> filterStack) {
			this.filterStack = filterStack;		}

		@Override
		public void visit(OpFilter filter) {
			for(Expr expr: filter.getExprs().getList()){
				filterStack.remove(expr);
			}
		}
				
	}
	
	/**
	 * puts the filters into the triples2variables2expressions- map
	 * @author joerg
	 *
	 */
	private static class Putter extends QuadVisitorBase{
		
		private Multiset<Expr> filterStack;
		private QueryInformation qi;

		public Putter(
				Multiset<Expr> filterStack,
				QueryInformation qi) {
			this.filterStack = filterStack;
			this.qi = qi;
		}
		@Override
		public void visit(OpDistinct opDistinct) {
			qi.setDistinct(opDistinct);
			
		}
		
		@Override
		public void visit(OpOrder opOrder) {
			qi.setOrder(opOrder);
		}
		
		@Override
		public void visit(OpProject opProject) {
			qi.setProject(opProject);
		}
		@Override
		public void visit(OpReduced opReduced) {
			qi.setReduced(opReduced);
		}
		
		@Override
		public void visit(OpSlice opSlice) {
			qi.setSlice(opSlice);
		}		
	}
}
