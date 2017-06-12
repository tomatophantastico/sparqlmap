package org.aksw.sparqlmap.core.mapper.finder;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityChecker;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple5;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import lombok.Getter;

public class StreamingBindingVisitor extends OpVisitorBase{
  
  
  private Collection<QuadMap> quadMaps;


  public StreamingBindingVisitor(Collection<QuadMap> quadMaps) {
    super();
    this.quadMaps = quadMaps;
  }


  
  
  
  @Getter
  List<Quad> heading = null;
  
  @Getter
  List<List<QuadMap>> rows = null; 
  
  
  @Override
  public void visit(OpQuadBlock opQuadBlock) {
    opQuadBlock.getPattern().forEach(quad -> this.join(quad));
    
  }
  
  @Override
  public void visit(OpQuadPattern quadPattern) {
    quadPattern.getPattern().forEach(quad -> this.join(quad));
  }
  
  @Override
  public void visit(OpQuad opQuad) {
   this.join(opQuad.getQuad());
  }
  
  
  public void join(Quad quad){
    joinInternal(quad, quadMaps);
  }
  
  public void join(Collection<Quad> quads){
    quads.stream().forEach(quad -> joinInternal(quad, quadMaps));
    ;
  }
  
  
  
  private void joinInternal(Quad quad, Collection<QuadMap> quadMaps){
    
    
    List<Quad> headingLeft = heading;
    List<Quad> headingRight = new ImmutableList.Builder<Quad>().add(quad).build();
    
   
    
    
    //transform the heading
    if(heading==null){
      heading = headingRight;
    }else{
      heading = new ImmutableList.Builder<Quad>().addAll(heading).add(quad).build();
    }
    
    
    //get a list of all 
    
    
    List<List<QuadMap>> initialBinding = Seq.seq(quadMaps)
          .filter((quadmap) ->  
            CompatibilityChecker
              .isCompatible(quad, quadmap))
          .map(ImmutableList::of)
          .collect(Collectors.toList());
    
    if(rows ==null){
      rows = (initialBinding);
    }else{
      //compute the join conditions
      
      SetMultimap<Var, Quad> leftVars =  BindingFunctions.indexQuadsByVariable(headingLeft);
      SetMultimap<Var, Quad> rightVars = BindingFunctions.indexQuadsByVariable(headingRight);
      
      Set<Var> sharedVars =  Sets.intersection(leftVars.keySet(), rightVars.keySet());
      
      
      // find out what to join against which
      List<Tuple5<Var,Integer,QuadPosition,Integer,QuadPosition>> joinConditions = sharedVars.stream().flatMap(var -> {
             return 
              BindingFunctions.unfoldPosition(var, leftVars.get(var), headingLeft)
           .crossJoin(
              BindingFunctions.unfoldPosition(var, rightVars.get(var), headingRight))
           .map(t2 -> new Tuple5<Var,Integer,QuadPosition,Integer,QuadPosition>(var,t2.v1.v1,t2.v1.v2,t2.v2.v1,t2.v2.v2));
      }).collect(Collectors.toList());
          
      
      

      // and execute the join
      
      rows = Seq.seq(rows).innerJoin(initialBinding, (leftrow, rightrow) -> {
       // filter if there are any conditions not met
        return !joinConditions.stream().filter(condition -> {
          
          TermMap leftMap = leftrow.get(condition.v2).get(condition.v3);
          TermMap rightMap = rightrow.get(condition.v4).get(condition.v5);

          return !CompatibilityChecker.isCompatible(leftMap,rightMap);
        }).findAny().isPresent();
      } )
        // and unfold the tuples
        .map(t2 -> new ImmutableList.Builder<QuadMap>().addAll(t2.v1).addAll(t2.v2).build())
        .collect(Collectors.toList());    
    }
  }
  
  

  
 
  
  
  @Override
  public void visit(OpFilter opFilter) {
    super.visit(opFilter);
  }
  
  
  @Override
  public void visit(OpLeftJoin opLeftJoin) {
    // TODO Auto-generated method stub
    super.visit(opLeftJoin);
  }
  

}
