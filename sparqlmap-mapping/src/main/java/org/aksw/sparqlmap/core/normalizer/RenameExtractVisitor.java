package org.aksw.sparqlmap.core.normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlmap.core.TranslationContext;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


/**
 * In this case, more beautiful means that all expressions, that actually filter
 * are in filter clauses. This means, that ""
 * 
 * @author joerg
 * 
 */
public class RenameExtractVisitor extends TransformCopy {

  private static final String INT_PREFIX = "int_";
  
	private int i = 0;
	
	private static final Logger LOG = LoggerFactory.getLogger(RenameExtractVisitor.class); 
	

	Map<String,Node> termToVariable = new HashMap<String, Node>();
	
	TranslationContext context;
	
	
	public RenameExtractVisitor(TranslationContext context) {
    super();
    this.context = context;
  }

  @Override
	public Op transform(OpQuadPattern quadBlock) {
		List<Quad> patterns = quadBlock.getPattern().getList();
		OpQuadBlock newOp = new OpQuadBlock();
		
		Map<String,String> var2Value = new HashMap<String, String>();
		ExprList exprList = new ExprList();
		List<ExprList> fromFromNamed = Lists.newArrayList();

		for (Quad quad : patterns) {
			quad = uniquefyTriple(quad, exprList); 
			

			newOp.getPattern().add(new Quad(
					rewriteGraphNode(quad.getGraph(), exprList, fromFromNamed ,termToVariable, var2Value),
					rewriteNode(quad.getSubject(), exprList, termToVariable, var2Value), 
					rewriteNode(quad.getPredicate(), exprList, termToVariable, var2Value),
					rewriteNode(quad.getObject(), exprList, termToVariable, var2Value)));
		}

		Op op = newOp;
		if(!exprList.isEmpty()){
		  op = OpFilter.filter(exprList, op);
		} 
		if(!fromFromNamed.isEmpty()){
		  op = recurseFilter(op, fromFromNamed);
		} 
	
		return op;
	}
  
  private Op recurseFilter(Op op, List<ExprList> exprLists){
    Op result = null;
    if(exprLists.size()==0){
      result = op;
    } else if(exprLists.size()==1){
      result = OpFilter.filter(exprLists.get(0), op);
    } else{
      List<ExprList> exprListsMinusOne = exprLists.subList(1,exprLists.size());
      result = OpFilter.filter(exprLists.get(0), recurseFilter(op, exprListsMinusOne));
    }
    
    return result;
  }
	
	private Node rewriteGraphNode(Node graph, ExprList exprList, List<ExprList> fromFromNamed,
      Map<String, Node> termToVariable2, Map<String, String> var2Value) {
	  Node result = null;
	  
	  if(graph.equals(Quad.defaultGraphNodeGenerated)
	       && ! context.getQuery().getGraphURIs().isEmpty() ){
	    Node nNew  = Var.alloc(i++ + INT_PREFIX);
        termToVariable.put(graph.toString(), nNew);
   
	      List<String> graphuris = context.getQuery().getGraphURIs();
	      ExprList filterIn = createFilterIn(nNew, graphuris);
	      fromFromNamed.add(filterIn);

	     result = nNew; 
	  }else if (graph.isVariable() && !context.getQuery().getNamedGraphURIs().isEmpty()) {
      
	    List<String> graphuris = context.getQuery().getNamedGraphURIs();
      ExprList filterIn = createFilterIn(graph, graphuris);
      fromFromNamed.add(filterIn);
      result = graph;
    }else{
      result = rewriteNode(graph, exprList, termToVariable2, var2Value);
    }
	  
	  return result;
  }

  private ExprList createFilterIn(Node nNew, List<String> graphuris) {
    ExprList filterIn = new ExprList();
    ExprList internal = new ExprList();
    ExprVar nodeVar = new ExprVar(nNew);
    
    for(String graphuri: graphuris){
    
//      internal.add(new ExprVar(nNew));
      internal.add( NodeValueNode.makeNode(NodeFactory.createURI(graphuri)));
      
    }
    filterIn.add(new E_OneOf(nodeVar, internal));
    return filterIn;
  }

  @Override
	public Op transform(OpFilter opFilter, Op subOp) {
    
    OpFilter filter = OpFilter.filterDirect(opFilter.getExprs(), opFilter.getSubOp());

		return filter;
	}
  
  
  

	
	/**
	 * Creates a new quad out of the old one, such that
	 * every variable is used only once in the pattern.
	 * 
	 * for example {?x ?x ?y} -> {?x ?genvar_1 ?y. FILTER (?x = ?genvar_1)}
	 * 
	 * @param quad
	 * @param exprList the list with the equals conditions
	 * @return the rewritten Quad
	 */
	private Quad uniquefyTriple(Quad quad, ExprList exprList) {
		
		List<Node> quadNodes = Arrays.asList(quad.getGraph(),quad.getSubject(),quad.getPredicate(),quad.getObject());
		
		List<Node> uniqeNodes = new ArrayList<Node>();
		
		for(Node quadNode: quadNodes){
			if(quadNode.isVariable()&&uniqeNodes.contains(quadNode)){
				Var var_new = Var.alloc(i++ + INT_PREFIX);
				uniqeNodes.add(var_new);
				exprList.add(new E_Equals(new ExprVar(quadNode),new ExprVar(var_new)));

			}else{
				uniqeNodes.add(quadNode);
			}
		}
		
		return new Quad(uniqeNodes.remove(0), uniqeNodes.remove(0),uniqeNodes.remove(0),uniqeNodes.remove(0));
	}



	private Node rewriteNode(Node n, ExprList addTo, Map<String,Node> termToVariable,Map<String,String> var2Value){

		if(n.isConcrete()){

			Node nNew = termToVariable.get(n.toString()); 
			if(nNew==null){
				nNew = Var.alloc(i++ + INT_PREFIX);
				termToVariable.put(n.toString(), nNew);
			}


			if(! (var2Value.containsKey(nNew.getName())&&var2Value.get(nNew.getName()).equals(n.toString()))){
				var2Value.put(nNew.getName(), n.toString());

				Expr newExpr = null;
				if(n.isLiteral()){
					newExpr = 
							new E_Equals(new ExprVar(nNew),NodeValue.makeString(n.getLiteralValue().toString()));


					if(n.getLiteralDatatypeURI()!=null && !n.getLiteralDatatypeURI().isEmpty()){
						newExpr = 
								new E_Equals(
										new E_Datatype(new ExprVar(nNew)),
										NodeValue.makeNodeString(n.getLiteralDatatypeURI()));
					}
					if(n.getLiteralLanguage()!=null && !n.getLiteralLanguage().isEmpty()){
						newExpr = new E_LangMatches(new ExprVar(nNew), NodeValue.makeString(n.getLiteralLanguage()));
					}

				}else{
					// no it is not, create the equivalnce check
					newExpr = new E_Equals(new ExprVar(nNew),
							NodeValue.makeNode(n));

				}
				addTo.add(newExpr);
			}	
			n = nNew;
		}
		return n;
	}
	
	@Override
	public Op transform(OpProject opProject, Op subOp) {
		
		
		
		OpVars.mentionedVars(subOp);
		
		
		opProject.getVars();
		// TODO Auto-generated method stub
		return super.transform(opProject, subOp);
	}
	
	
	
	
	
	
	
}
