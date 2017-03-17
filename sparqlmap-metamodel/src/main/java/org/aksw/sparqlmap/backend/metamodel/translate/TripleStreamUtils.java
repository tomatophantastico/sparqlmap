package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;


public class TripleStreamUtils {



  public static Collector<Multimap<Node,Triple>, Multimap<Node, Triple>, DatasetGraphMap> collectToListDatasetGraphMap(){
  
    return new Collector<Multimap<Node,Triple>, Multimap<Node, Triple>, DatasetGraphMap>(){
     
      @Override
      public Set<java.util.stream.Collector.Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
      }



      @Override
      public Supplier<Multimap<Node, Triple>> supplier() {
        return HashMultimap::create;
      }

      @Override
      public Function<Multimap<Node, Triple>, DatasetGraphMap> finisher() {
        return mapToDatasetGraphMap();
      }



      @Override
      public BiConsumer<Multimap<Node, Triple>, Multimap<Node, Triple>> accumulator() {
        return Multimap::putAll;
      }



      @Override
      public BinaryOperator<Multimap<Node, Triple>> combiner() {
        return (map1,map2) -> {
          map1.putAll(map2);
          return map1;
        };
      }
      
    };
  }
  
  public static Function<Multimap<Node, Triple>,Multimap<Node, Triple>> setGraphNames(DatasetDescription dsd){
    return setGraphNames(Sets.newHashSet(dsd.getDefaultGraphURIs()), Sets.newHashSet(dsd.getNamedGraphURIs()));
  }

  
  public static Function<Multimap<Node, Triple>,Multimap<Node, Triple>> setGraphNames(Set<String> from, Set<String> fromNamed){
    if(from.isEmpty()){
      from.add(Quad.defaultGraphNodeGenerated.getURI());
    }
    
    return (triplesmap) -> {
      List<Triple> addToDg = Lists.newArrayList();
      List<Node> graphsToDrop = Lists.newArrayList();
      triplesmap.asMap().forEach((graph, triples) -> {
        if(!Quad.defaultGraphNodeGenerated.getURI().equals(graph.getURI())
            && from.contains(graph.getURI()) ){
          addToDg.addAll(triples);
        }
        if(!fromNamed.isEmpty() && !fromNamed.contains(graph.getURI())){
          graphsToDrop.add(graph);
        }

      });
      graphsToDrop.forEach(rmgraph -> triplesmap.removeAll(rmgraph));
      if(!addToDg.isEmpty()){
        triplesmap.putAll(Quad.defaultGraphNodeGenerated, addToDg);
      }
      return triplesmap;
      
      
    };
  }
  
  public static Function<Multimap<Node, Triple>,DatasetGraphMap> mapToDatasetGraphMap(){
    return(multimap) -> {
        DatasetGraphMap dsgm = new DatasetGraphMap();
        multimap.asMap().forEach((node,triples) -> {
          if(Quad.defaultGraphNodeGenerated.getURI().equals(node.getURI())){
            dsgm.setDefaultGraph(new CollectionGraph(triples));
          } else{
            dsgm.addGraph(node, new CollectionGraph(triples));
          }
        });
        return dsgm;
      };
  }
}
