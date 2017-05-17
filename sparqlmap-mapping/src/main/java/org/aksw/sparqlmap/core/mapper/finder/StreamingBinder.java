package org.aksw.sparqlmap.core.mapper.finder;

import java.util.Collection;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Multimap;

public class StreamingBinder {
  
  
  public static  QueryBinding bind(TranslationContext tc, R2RMLMapping mapping){
    
   
    return bind(tc.getBeautifiedQuery(),mapping.getQuadMaps());
  }
  
  
  public static QueryBinding bind(Op op, Collection<QuadMap> quadMaps){
    StreamingBindingVisitor visitor = new StreamingBindingVisitor(quadMaps);
    
    OpWalker.walk(op, visitor);
    
    
    return QueryBinding.builder().head(visitor.heading).rows(visitor.rows).build();
  }

}
