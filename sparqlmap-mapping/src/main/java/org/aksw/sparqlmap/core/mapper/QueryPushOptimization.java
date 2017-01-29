package org.aksw.sparqlmap.core.mapper;

import java.util.List;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.mapper.finder.MappingBinding;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.util.JenaHelper;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
/**
 * If the prohjected variables bind exclusively to values from the mapping, the underlying database does not have to be fully query.
 * 
 * Consider a "SELECT DISTINCT ?p {?s ?p ?o}" will in most cases be answered using the mapping and a check if the database is populated.
 * 
 * @author joerg
 *
 */
public class QueryPushOptimization {
  
  
  
  /**
   * Checks if this optimization is applicable to this query
   * 
   * @param origQuery
   * @param queryBinding
   * @return
   */
  public static void setProjectionPush(
       TranslationContext context) {
    boolean pushable =true;
    Query origQuery = context.getQuery();
    MappingBinding queryBinding = context.getQueryBinding();
    // check here if we can projection push
    if (!origQuery.isDistinct()) {
      pushable = false;
    } else{
      
      
   // check if all bindings for projected variables are constant
      List<Var> pvars = origQuery.getProjectVars();
      projectvarloop:
      for (Var pvar : pvars) {
        for (Quad quad : queryBinding.getBindingMap().keySet()) {
          for(QuadPosition pos: QuadPosition.values()){
            Node quadNode = JenaHelper.getField(quad, pos);
            if(pvar.equals(quadNode)){
              for(QuadMap qmc : queryBinding.getBindingMap().get(quad)){
                TermMap tm = qmc.get(pos);
                if(!(tm instanceof TermMapConstant)){
                  pushable = false;
                  break projectvarloop;
                }
              }
            }
          }
        }
      } 
    }
    context.getQueryInformation().setProjectionPushable(pushable);
  }

}
