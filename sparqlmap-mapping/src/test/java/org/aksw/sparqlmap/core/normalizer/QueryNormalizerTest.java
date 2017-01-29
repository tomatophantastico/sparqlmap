package org.aksw.sparqlmap.core.normalizer;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.aksw.sparqlmap.core.TranslationContext;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.util.VarUtils;
import org.junit.Test;

public class QueryNormalizerTest {

  
  @Test
  public void testQuery(){
    eval(4,  "select * {<http://sssss.ss/s> ?p <http://oooooo.oo/o>}");
  }
  
  @Test
  public void testQueryLimit(){
    eval(4,"select ?p {<http://sssss.ss/s> ?p <http://oooooo.oo/o>} limit 10");
  }
  
  @Test 
  public void testQueryLimitJoin(){
    eval(6,"select ?p {<http://sssss.ss/s> ?p <http://oooooo.oo/o>. <http://sss.ss/s> ?p <http://xxxx.x/x>} limit 10");
  }
  @Test 
  public void testQueryLimitJoinOrderBy(){
    eval(6,"select ?p {<http://sssss.ss/s> ?p <http://oooooo.oo/o>. <http://sss.ss/s> ?p <http://xxxx.x/x>}  order by ?p limit 10");
  }
  @Test 
  public void testQueryDistinctLimitJoinOrderBy(){
    eval(6,"select distinct ?p {<http://sssss.ss/s> ?p <http://oooooo.oo/o>. <http://sss.ss/s> ?p <http://xxxx.x/x>}  order by ?p limit 10");
  }
  @Test 
  public void testQueryDistinctFilterLimitJoinOrderBy(){
    eval(6,"select distinct ?p {<http://sssss.ss/s> ?p <http://oooooo.oo/o>. <http://sss.ss/s> ?p <http://xxxx.x/x> filter (isIRI(?p) )}  order by ?p limit 10");
  }
  
  private void eval(int count, String query){
    AlgebraGenerator gen = new AlgebraGenerator();
    
    TranslationContext qc = TranslationContext.builder().query(QueryFactory.create(query)).build();
    
    QueryNormalizer.normalize(qc);
    
    assertEquals(count, new HashSet( OpVars.mentionedVars(qc.getBeautifiedQuery())).size());
  }

}
