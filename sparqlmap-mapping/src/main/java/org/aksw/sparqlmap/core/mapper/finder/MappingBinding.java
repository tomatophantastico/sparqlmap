package org.aksw.sparqlmap.core.mapper.finder;

import static org.aksw.sparqlmap.core.util.JenaHelper.getField;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityChecker;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Provides a fluent api for performing operations on triple bindings is a wrapper around the quad bindings
 * 
 * @author joerg
 * 
 */
public class MappingBinding {

	private Multimap<Quad, QuadMap> bindingMap = HashMultimap.create();
	private Map<Quad, Map<String, Collection<Expr>>> quads2variables2expressions;
  private Collection<QuadMap> quadMaps;
  
  private CompatibilityChecker cchecker = new CompatibilityChecker();

  private static Logger log  = LoggerFactory.getLogger(MappingBinding.class);

	public MappingBinding(Map<Quad, Map<String, Collection<Expr>>> quads2variables2expressions,Collection<QuadMap> quadMaps) {
    super();
    this.quads2variables2expressions = quads2variables2expressions;
    this.quadMaps = quadMaps;
  }


  @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Triple Bindings are: \n");
		Set<Quad> quads = this.bindingMap.keySet();
		for (Quad quad: quads) {
			sb.append("* " + quad.toString() + "\n");
			for (QuadMap tm : this.bindingMap.get(quad)) {
				sb.append("    QuadMap: " + tm.toString() + "\n");
				
			}
		}
		if(isEmpty()){
		  sb.append("<empty>");
		}
		
		return sb.toString();
	}
	
	
	public Multimap<Quad, QuadMap> getBindingMap() {
		return bindingMap;
	}
	
	/**
	 * indicates, if at least one triple pattern of the query could have been bound to a triples map.
	 * 
	 * @return
	 */
	public boolean isEmpty(){
	  boolean isEmpty = true;
	  
	  for(Quad quad: bindingMap.keySet()){
	    if(bindingMap.get(quad).size()==1
	        &&bindingMap.get(quad).iterator().next()!=QuadMap.NULLQUADMAP){
	      isEmpty = false;
	      break;
	    }
	  }
	  
	  return isEmpty;
	}
	
	
	
	public void init(Quad quad){
	  
	  if(!bindingMap.containsKey(quad)){
	    
	  
  
    //then check them for compatibility
    Map<String,Collection<Expr>> var2exps = quads2variables2expressions.get(quad);
    String gname = quad.getGraph().getName();
    String sname = quad.getSubject().getName();
    String pname =  quad.getPredicate().getName();
    String oname =  quad.getObject().getName();
    Collection<Expr> sxprs =  var2exps.get(sname);
    Collection<Expr> pxprs = var2exps.get(pname);
    Collection<Expr> oxprs = var2exps.get(oname);
    Collection<Expr> gxprs = var2exps.get(gname);
  
    // iterate over the nodes and remove them if they are not
    // compatible
    for (QuadMap quadmap: quadMaps) {
      boolean allCompatible = true;
      
      if(!(
             cchecker.isCompatible(quadmap.get(QuadPosition.graph),gname,gxprs)
          && cchecker.isCompatible(quadmap.get(QuadPosition.subject),sname,sxprs)
          && cchecker.isCompatible(quadmap.get(QuadPosition.predicate),pname,pxprs)
          && cchecker.isCompatible(quadmap.get(QuadPosition.object),oname,oxprs)
          )){
        allCompatible = false;
        continue;
        }
      if(allCompatible){
        bindingMap.put(quad, quadmap);
      }
 
      }
	  }
	}
	
	
	 public boolean preLeftJoin(Collection<Quad> lefts, Collection<Quad> rights){
	   
	 
	   
	    //now compare each with each 
	      AtomicBoolean result = new AtomicBoolean(false);
	      lefts.forEach((left) -> {
	        Collection<QuadMap> leftMaps = bindingMap.get(left);
	        rights.forEach((right)->{
	          Collection<QuadMap> rightMaps = bindingMap.get(right);
	          boolean hasMerged = false;
	          if(!left.equals(right)){
	            hasMerged = prejoinIndivudualQuad(left, leftMaps, right, rightMaps);
	          }
	          if(hasMerged==true){
	            result.set(hasMerged);
	          }
	          
	        });
	      });
	      
	      return result.get();
	    }
	
	
	/**
   * merges the bindings,  pre-evaluates the left join 
   */
  private boolean prejoinIndivudualQuad(Quad quadLeft, Collection<QuadMap> quadLeftBinding, Quad quadRight,
      Collection<QuadMap> quadRightBinding) {

    boolean wasmerged = false;

    for (QuadPosition f1 : QuadPosition.values()) {
      for (QuadPosition f2 : QuadPosition.values()) {

        Node n1 = getField(quadLeft, f1);
        Node n2 = getField(quadRight, f2);

        if (matches(n1, n2)) {

          Collection<QuadMap> quadLeftBinding_copy = null;

          if (log.isDebugEnabled()) {
            quadLeftBinding_copy = new HashSet<QuadMap>(quadLeftBinding);
          }

          wasmerged = mergeTripleMaps(f1, f2, quadLeftBinding, quadRightBinding);

          if (log.isDebugEnabled()) {
            if (wasmerged) {
              log.debug("Merged on t1: " + quadLeft.toString() + " LJ t2:" + quadRight.toString());
              log.debug("Removed the following triple maps:");

              quadLeftBinding_copy.removeAll(quadLeftBinding);
              for (QuadMap tripleMap : quadLeftBinding) {
                log.debug("" + tripleMap);
              }
            } else {
              log.debug("All compatible on t1: " + quadLeft.toString() + " LJ t2:" + quadRight.toString());
            }
          }
        }
      }
    }

    return wasmerged;

  }
  
  
  /**
   * modifies n1 according to doing a join on with n2
   * 
   * @return true if something was modified
   * @param n1
   * @param n2
   * @param f1
   * @param f2
   * @param triplemaps1
   * @param triplemaps2
   */
  private boolean mergeTripleMaps(QuadPosition f1, QuadPosition f2,
      Collection<QuadMap> triplemaps1, Collection<QuadMap> triplemaps2) {
    // we keep track if a modification was performed. Needed later to notify
    // the siblings.
    boolean removed = false;
    Set<QuadMap> toRemove1 = Sets.newHashSet(); 
    for(QuadMap qmc1: triplemaps1){
      if(qmc1 != QuadMap.NULLQUADMAP){
        for(QuadMap qmc2: triplemaps2){
          if(qmc1 != QuadMap.NULLQUADMAP){
            if(!(cchecker.isCompatible(qmc1.get(f1),qmc2.get(f2)))){
             toRemove1.add(qmc1);
            }
          }
        }
      }
     
    }
    removed = triplemaps1.removeAll(toRemove1);
    if(triplemaps1.isEmpty()){
      triplemaps1.add(QuadMap.NULLQUADMAP);
    }
    
    
    return removed;
  }
  

  
  /**
   * checks if both are variables with the same name
   * 
   * @param n1
   * @param n2
   * @return
   */
  private boolean matches(Node n1, Node n2) {
    boolean result = false;
    if (n1.isVariable() && n2.isVariable()
        && n1.getName().equals(n2.getName())) {
      result = true;
    }
    return result;
  }
  
  
  
  /**
   * Creates a List of Mapping bindings, in which every quad is quaranteed to be only associated to one quadmap.
   * This is performed by calculating the cartesian product.
   * 
   * @return
   */
  
  public Set<Map<Quad,QuadMap>> asMaps(){
    //convert the sets to Lists
  
    return asMaps(Lists.newArrayList(bindingMap.keySet()));
    
  }
  
  private Set<Map<Quad,QuadMap>> asMaps(List<Quad> qml){
    
    Set<Map<Quad,QuadMap>> result = Sets.newHashSet();
    
    if(qml.size()==1){
      
      Quad quad =  qml.get(0);
      for(QuadMap qm :  bindingMap.get(quad)){
        Map<Quad,QuadMap> binding = Maps.newHashMap();
        binding.put(quad, qm);
        result.add(binding);
      }
    }else if(qml.size()>1){
      Set<Map<Quad,QuadMap>> toMultiplies =  asMaps(qml.subList(1, qml.size()));
      Set<Map<Quad,QuadMap>> resultsNew = Sets.newHashSet();
      for(Map<Quad,QuadMap> toMultiply: toMultiplies){
        for(Map<Quad,QuadMap> res: result){
          Map<Quad,QuadMap> binding = Maps.newHashMap();
          binding.putAll(toMultiply);
          binding.putAll(res);
          resultsNew.add(binding);
        }
      }
      result = resultsNew;
    }
    
    
    
    return result;
    
    
   
  }
  
   
}