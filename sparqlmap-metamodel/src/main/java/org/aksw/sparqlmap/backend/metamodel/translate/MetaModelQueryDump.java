package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.DatasetDescriptionAssembler;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.metamodel.DataContext;

import com.aol.cyclops.data.async.Queue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;


/**
 * ignores the joins and fetches data based on the tables of the quad maps
 * Pushes all filters as far down as possible
 * 
 * @author joerg
 *
 */
public class MetaModelQueryDump {
  
  
  


  
  public static Stream<Multimap<Node,Triple>> streamFast(Collection<QuadMap> quadmaps, DataContext context, ExecutorService exec, boolean rowwiseBlanks){
    Multimap<LogicalTable,QuadMap> bucketedMaps = bucketFilterNonNullMaps(new HashSet<QuadMap>(quadmaps));
    Queue<Multimap<Node,Triple>> queue =  new Queue<Multimap<Node,Triple>>(new  LinkedBlockingQueue<Multimap<Node,Triple>>(1000));
    
    AtomicInteger jobCount = new AtomicInteger(bucketedMaps.keySet().size());
    if(jobCount.get()>0){
      for (LogicalTable ltab : bucketedMaps.keySet()) {
        // build the query
        MetaModelSelectiveDump sd = new MetaModelSelectiveDump(ltab,bucketedMaps.get(ltab) ,context,queue,jobCount, rowwiseBlanks);
        exec.execute(sd);
      }
    }else{
      queue.close();
    }
    return queue.stream();
  }
  
  
  public static DatasetGraph assembleDs(TranslationContext tcontext, DataContext context, boolean rowwiseBlanks) {

   return assembleDs(tcontext.getQueryBinding().getRows().stream().flatMap(list -> list.stream()).collect(Collectors.toList()), context, rowwiseBlanks, tcontext.getQuery().getDatasetDescription());

  }
  
  
  public static DatasetGraph assembleDs(Collection<QuadMap> quadmaps, DataContext context) {
    
    
    return assembleDs(quadmaps, context, false, new DatasetDescription());
  }
  
  public static DatasetGraph assembleDs(Collection<QuadMap> quadmaps, DataContext context, boolean rowwiseBlanks, DatasetDescription dsd) {
    if(dsd==null){
      dsd = new DatasetDescription();
    }
   
    
    return streamFast(quadmaps, context,Executors.newWorkStealingPool(),rowwiseBlanks)
    .map(TripleStreamUtils.setGraphNames(dsd))
    .collect(TripleStreamUtils.collectToListDatasetGraphMap());
    
  }
  
  
  
  
  
 
  
  
  public static Multimap<LogicalTable, QuadMap> bucketFilterNonNullMaps(Collection<QuadMap> quadmaps ){
    Multimap<LogicalTable, QuadMap> bucketedMaps = HashMultimap.create();
    for (QuadMap quadmap : quadmaps) {
      if(quadmap != QuadMap.NULLQUADMAP){
        bucketedMaps.put(quadmap.getLogicalTable(), quadmap);
      }
    }
    return bucketedMaps;
  }
  
  


}
