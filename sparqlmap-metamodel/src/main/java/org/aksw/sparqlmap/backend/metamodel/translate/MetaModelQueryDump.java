package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.QuadMap.LogicalTable;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.metamodel.DataContext;

import com.aol.cyclops.data.async.Queue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * ignores the joins and fetches data based on the tables of the quad maps
 * Pushes all filters as far down as possible
 * 
 * @author joerg
 *
 */
public class MetaModelQueryDump {
  
  
  

  public static DatasetGraph assembleDs(TranslationContext tcontext, DataContext context) {

    Collection<Node> dgraphs = tcontext.getQuery().getGraphURIs().stream().map(graphname-> NodeFactory.createURI(graphname)).collect(Collectors.toList());
    
   return assembleDs(tcontext.getQueryBinding().getBindingMap().values(), context,dgraphs);

  }
  
  public static Stream<Multimap<Node,Triple>> streamFast(Collection<QuadMap> quadmaps, DataContext context, ExecutorService exec){
    Multimap<LogicalTable,QuadMap> bucketedMaps = bucketFilterNonNullMaps(new HashSet<QuadMap>(quadmaps));
    Queue<Multimap<Node,Triple>> queue =  new Queue<Multimap<Node,Triple>>(new  LinkedBlockingQueue<Multimap<Node,Triple>>(1000));
    
    AtomicInteger jobCount = new AtomicInteger(bucketedMaps.keySet().size());
    if(jobCount.get()>0){
      for (LogicalTable ltab : bucketedMaps.keySet()) {
        // build the query
        MetaModelSelectiveDump sd = new MetaModelSelectiveDump(ltab,bucketedMaps.get(ltab) ,context,queue,jobCount);
        exec.execute(sd);
      }
    }else{
      queue.close();
    }
    return queue.stream();
  }
  
  
  public static DatasetGraph assembleDs(Collection<QuadMap> quadmaps, DataContext context) {
    return assembleDs(quadmaps, context, Lists.newArrayList(Quad.defaultGraphNodeGenerated));
  }
  
  public static DatasetGraph assembleDs(Collection<QuadMap> quadmaps, DataContext context, Collection<Node> defaultGraphs) {
    final Multimap<Node,Triple> graphs = HashMultimap.create();

    Stream<Multimap<Node,Triple>> stream = streamFast(quadmaps, context,Executors.newWorkStealingPool());
    stream.forEach(iresult-> 
      graphs.putAll(iresult));
    
    DatasetGraph dsgml = convert(graphs,defaultGraphs);
    

    
    
    return dsgml;
  }
  
  
  
  public static DatasetGraph convert(final Multimap<Node, Triple> graphs){
    return convert(graphs, Sets.newHashSet(Quad.defaultGraphNodeGenerated));
  }

  public static DatasetGraph convert(final Multimap<Node, Triple> graphs, Collection<Node> defaultGraphs) {
    DatasetGraphMap  dsgml = new DatasetGraphMap();
    
    List<Triple> defaultGraphTriples = Lists.newArrayList();
    
    graphs.asMap().forEach((g,triples)-> {
      if(defaultGraphs.contains(g)){
        defaultGraphTriples.addAll(triples);
      }else{
        dsgml.addGraph(g, new CollectionGraph(triples));
      }
    });
    dsgml.setDefaultGraph(new CollectionGraph(defaultGraphTriples));
    
    
    
  
    return dsgml;
  }
  
  
  
  private static Multimap<LogicalTable, QuadMap> bucketFilterNonNullMaps(Collection<QuadMap> quadmaps ){
    Multimap<LogicalTable, QuadMap> bucketedMaps = HashMultimap.create();
    for (QuadMap quadmap : quadmaps) {
      if(quadmap != QuadMap.NULLQUADMAP){
        bucketedMaps.put(quadmap.getLogicalTable(), quadmap);
      }
    }
    return bucketedMaps;
  }

}
