package org.aksw.sparqlmap.core.mapper.finder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.algebra.QuadVisitorBase;
import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.table.TableUnit;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * this class generates a MappingBinding for a query by walking all over it.
 * @author joerg
 *
 */
public class Binder {
	private static Logger log = LoggerFactory.getLogger(Binder.class);
	
	private R2RMLMapping mapconf;

	
	public Binder(R2RMLMapping mappingConf) {
		this.mapconf = mappingConf;
	}


	public MappingBinding bind(TranslationContext tc){
	  Op op = tc.getBeautifiedQuery();
	  
	  MappingBinding binding = new MappingBinding(tc.getQueryInformation().getFiltersforvariables(), mapconf.getQuadMaps().values());
		
		
		OpWalker.walk(op, new BinderVisitor(binding));
		
		

		return binding;
	}
	
	
	
	private class BinderVisitor extends QuadVisitorBase{
		
	  private MappingBinding binding;
		
		public BinderVisitor(
				MappingBinding binding) {
			this.binding = binding;
		}


		// we use this stack to track track which what to merge on unions, joins and left joins
		Stack<Collection<Quad>> quads = new Stack<Collection<Quad>>();
		
		
		@Override
		public void visit(OpJoin opJoin) {
			log.debug("Visiting opJoin " + opJoin);
			Collection<Quad> rightQuads = quads.pop();

			Collection<Quad> leftQuads = quads.pop();
			
			
			//we now merge the bindings for each and every triple we got here.
			
			boolean changed = binding.preLeftJoin(leftQuads,rightQuads);
			changed = changed | binding.preLeftJoin(rightQuads, leftQuads);

			//if we modified any binding, we have to walk this part of the Op-Tree again.
			
			if(changed){
				OpWalker.walk(opJoin, this);

			}
			putOnStack(leftQuads, rightQuads);
		}

		@Override
		public void visit(OpLeftJoin opLeftJoin) {
			log.debug("Visiting opLeftJoin"+opLeftJoin);
			
			if(opLeftJoin.getLeft() instanceof OpTable && ((OpTable)opLeftJoin.getLeft()).getTable() instanceof TableUnit){
				//leftjoin without triples. do nothing
				
			}else{
				Collection<Quad> leftQuads = quads.pop();
				Collection<Quad> rightQuads = quads.pop();
				//we now merge the bindings for each and every triple we got here.
				
				boolean changed =  binding.preLeftJoin(leftQuads, rightQuads);
	
				//if we modified any binding, we have to walk this part of the Op-Tree again.
				
				if(changed){
					OpWalker.walk(opLeftJoin, this);
				}
				putOnStack(rightQuads, leftQuads);
			}
		}
		
		
		
		@Override
		public void visit(OpUnion opUnion) {
			log.debug("Visiting opUnion" + opUnion);
			//just popping the triples, so they are not used later on 
			Collection<Quad> rightSideTriples = quads.pop();
			Collection<Quad> leftSideTriples = quads.pop();
			
			putOnStack(leftSideTriples, rightSideTriples);
			
			
		}

		private void putOnStack(Collection<Quad> leftSideTriples,
				Collection<Quad> rightSideTriples) {
			//do not nothing to the triples but put them together, so they can be merged by a later join
			Collection<Quad> combined = new HashSet<Quad>();
			combined.addAll(leftSideTriples);
			combined.addAll(rightSideTriples);
			quads.add(combined);
		}
		
		
		
		
		@Override
		public void visit(OpQuadPattern opQuadBlock) {
			quads.add(opQuadBlock.getPattern().getList());
			
			for(Quad quad: opQuadBlock.getPattern().getList()){
					binding.init(quad);
			
			}
			
			
			
			binding.preLeftJoin(opQuadBlock.getPattern().getList(), opQuadBlock.getPattern().getList());
			
		
			
		}
		
		
	
		
		
		/**creates a subset of the bindings
		 * 
		 * @return
		 */
		private Map<Quad,Collection<QuadMap>> partitionBindings(Collection<Quad> quads){
			Map<Quad,Collection<QuadMap>> subset = new HashMap<Quad, Collection<QuadMap>>();
			for(Quad quad : quads){
				subset.put(quad, binding.getBindingMap().get(quad));
			}
			
			return subset;
		}


		
		
	  
	  @Override
    public
	  void visit(OpTable opTable){
	    
	    if(opTable.getTable() instanceof TableUnit){
	      // do nothing here
	    }else{
	      throw new ImplementationException("Values/Table not implmeneted");

	    }
	  }
	}
	
	

	
	
	
	
	
	


	


}
