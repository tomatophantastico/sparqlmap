package org.aksw.sparqlmap.core.normalizer;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlmap.core.TranslationContext;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.AlgebraQuad;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryNormalizer {
  
  private static AlgebraGenerator agen = new  AlgebraGenerator();
  
  
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryNormalizer.class);
  
  /**
   * Transforms the query into the form required for sparqlmap.
   * Includes filter extraction and rewriting some patterns. 
   * 
   * @param sparql
   * @return
   */
  
  public static void normalize(TranslationContext context){
    
    Query sparqlQuery = context.getQuery();
    
    Op query  = agen.compile(sparqlQuery);
    LOGGER.debug("Qp before rewriting is: {}",query );
    
    
    
    // this odd construct is neccessary as there seems to be no convenient way of extracting the vars of a query from the non-algebra-version.
    if(sparqlQuery.getProject().isEmpty()){
    
      sparqlQuery.setQueryResultStar(false);
      List<Var> vars = new ArrayList<Var>( OpVars.mentionedVars(query));
      for (Var var : vars) {
        sparqlQuery.getProject().add(var);

      }
      query  = agen.compile(sparqlQuery);
    }
    
    
    PropertyPathRewriter pprw = new PropertyPathRewriter(context );
    query = Transformer.transform(pprw, query);
    

      
   LOGGER.debug("Op after property path rewriting is {}", query);

    
    
    query = AlgebraQuad.quadize(query);
    RenameExtractVisitor rev = new RenameExtractVisitor(context);
    
    
    Op newOp = Transformer.transform(rev, query);
    LOGGER.debug("Op after renaming/extract rewriting is {}", newOp);

    context.setBeautifiedQuery(newOp);
    
  }
  
  
  
  

}
