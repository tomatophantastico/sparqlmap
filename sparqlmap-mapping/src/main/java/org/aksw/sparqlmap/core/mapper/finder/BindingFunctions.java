package org.aksw.sparqlmap.core.mapper.finder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.util.JenaHelper;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.jooq.lambda.Seq;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

public class BindingFunctions {
  
  
  public static Seq<Tuple2<Integer,QuadPosition>> unfoldPosition(Var sharedVar, Set<Quad> toUnfold, List<Quad> allQuads){
    
    return Seq.seq(toUnfold)
        .flatMap(quad -> Stream.of(QuadPosition.values())
            .map(pos -> JenaHelper.getField(quad, pos).equals(sharedVar)?new Tuple2<Integer,QuadPosition>(allQuads.indexOf(quad),pos):null )
            .filter(Objects::nonNull)
            );
    
    
  }
  
  public static SetMultimap<Var, Quad> indexQuadsByVariable(Collection<Quad> quads){
    SetMultimap<Var, Quad> result = quads.stream().flatMap(quad -> 
      Stream.of(QuadPosition.values())
      .map(pos -> Tuple.tuple(JenaHelper.getField(quad, pos),quad)))
      .filter(t2 -> t2.v1.isVariable())
      .collect(
          Multimaps.toMultimap(
              t2 -> (Var) t2.v1, 
              t2 -> t2.v2, 
              MultimapBuilder.hashKeys().hashSetValues()::build));


    return result;
  }
  
  public static Stream<Tuple2<Node,TermMap>> termWise(Tuple2<Quad,QuadMap> t2){
    return Stream.of(QuadPosition.values()).map(pos -> Tuple.tuple(JenaHelper.getField(t2.v1,pos),t2.v2.get(pos)));
  }
  
  
  public static Stream<Tuple4<Node,Quad,TermMap,QuadMap>> termWiseWithQuad(Tuple2<Quad,QuadMap> t2){
    return Stream.of(QuadPosition.values()).map(pos -> Tuple.tuple(JenaHelper.getField(t2.v1,pos),t2.v1,t2.v2.get(pos),t2.v2));
  }
  
  

}
