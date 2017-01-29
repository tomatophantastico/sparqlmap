package org.aksw.sparqlmap.querytests;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMaker;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.resultset.ResultSetMem;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aol.cyclops.data.MutableInt;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class JenaMemoryModelLoadBenchmark {
  
  static int count = 100000;
  
  static final MetricRegistry metrics = new MetricRegistry();
  
  @After
  public void gc(){
    Runtime.getRuntime().gc();
  }
  
  @BeforeClass
  public static void setup(){
  }
  
  @AfterClass
  public static  void teardown(){
    ConsoleReporter.forRegistry(metrics).outputTo(System.out)
    .build().report();
  }
  
  static Multimap<Node,Triple> getSingleMap(){
    Multimap<Node,Triple> quads = HashMultimap.create();

    String prefix = "http://localhost/test/";
    for(int i =0;i<count;i++){
      int graphNo = i % 10;
      int predicateNo = i % 500;
      int objectNo = i % 10000;
      
      quads.put(NodeFactory.createURI(prefix+graphNo), 
          new Triple(NodeFactory.createURI(prefix+i),
              NodeFactory.createURI(prefix+predicateNo),
              NodeFactory.createURI(prefix+objectNo)));
    }
    return quads;
  }
  static List<Multimap<Node,Triple>> getMultipleMaps(){
    
    List<Multimap<Node,Triple>> maps = Lists.newArrayList();
    Multimap<Node,Triple> quads = HashMultimap.create();
    maps.add(quads);

    String prefix = "http://localhost/test/";
    for(int i =0;i<count;i++){
      
      if(i%1000 ==0){
        quads = HashMultimap.create();
        maps.add(quads);
      }
      
      int graphNo = i % 10;
      int predicateNo = i % 500;
      int objectNo = i % 10000;
      
      quads.put(NodeFactory.createURI(prefix+graphNo), 
          new Triple(NodeFactory.createURI(prefix+i),
              NodeFactory.createURI(prefix+predicateNo),
              NodeFactory.createURI(prefix+objectNo)));
    }
    return maps;
  }
  
  
  static List<Quad> getQuads(){
    List<Quad> quads = Lists.newArrayList();
    String prefix = "http://localhost/test/";
    for(int i =0;i<count;i++){

      
      int graphNo = i % 10;
      int predicateNo = i % 500;
      int objectNo = i % 10000;
      
      quads.add(new Quad(NodeFactory.createURI(prefix+graphNo), 
          NodeFactory.createURI(prefix+i),
              NodeFactory.createURI(prefix+predicateNo),
              NodeFactory.createURI(prefix+objectNo)));
    }
    return quads;
  }
  
  
  @Test
  public void testGraphLinking(){
    Timer loading = metrics.timer("gl-gen");
    Context sqcg = loading.time();

    List<Quad> quads = getQuads();
    sqcg.stop();
    Context sqcl = metrics.timer("gl-bucket").time();
    DatasetGraphMapLink dsgml = new DatasetGraphMapLink(GraphFactory.createDefaultGraph());
    Multimap<Node,Triple> graphs = HashMultimap.create();
    for (Quad quad : quads) {
      graphs.put(quad.getGraph(), quad.asTriple());
    }
    sqcl.stop();
    Context sqcb = metrics.timer("gl-load").time();
    graphs.keySet().stream().forEach(graphNode->
    { Graph graph = new CollectionGraph(graphs.get(graphNode));
      dsgml.addGraph(graphNode, graph);
    });
    
    

    sqcb.stop();
    Context sqcv = metrics.timer("gl-validate").time();

    int count = getCount(dsgml);
    assertTrue(count == this.count);
    sqcv.stop();
  }
  
  
  @Test
  public void testStreamLoading() throws Exception {
    
    Timer loading = metrics.timer("st-gen");
    Context sqcg = loading.time();
    
   
    List<Quad> quads = getQuads();
    sqcg.stop();
    Context sqcl = metrics.timer("st-load").time();
    DatasetGraphInMemory dsgim = new DatasetGraphInMemory();
    
    StreamRDF dsgInputStream = StreamRDFLib.dataset(dsgim);
    
    quads.stream().forEach(quad->dsgInputStream.quad(quad));
    

   sqcl.stop();
   Context sqcv = metrics.timer("st-validate").time();

   int count = getCount(dsgim);
    assertTrue(count==this.count);
    sqcv.stop();
    
    
    
  }
  
  
  
  @Test
  public void testEverySingleQuadSeparately() throws Exception {
    Timer loading = metrics.timer("sq-gen");
    Context sqcg = loading.time();
    
   
    List<Quad> quads = getQuads();
    sqcg.stop();
    Context sqcl = metrics.timer("sq-load").time();
    DatasetGraphInMemory dsgim = new DatasetGraphInMemory();
   for(Quad quad: quads){
     dsgim.add(quad);
   }
   sqcl.stop();
   Context sqcv = metrics.timer("sq-validate").time();

   int count = getCount(dsgim);
    assertTrue(count==this.count);
    sqcv.stop();
    
    
  }

  
  
  private int getCount(DatasetGraph dsg){
     
    

    Query count = QueryFactory.create("SELECT (COUNT(*) AS ?no) ?g { graph ?g { ?s ?p ?o}  } group by ?g");
    Dataset ds = DatasetImpl.wrap(dsg);
    ResultSetRewindable rs = new  ResultSetMem(QueryExecutionFactory.create(count,ds).execSelect());
    
    ResultSetFormatter.out(System.out, rs);
    rs.reset();
    
    final MutableInt sum = new MutableInt(0);
    rs.forEachRemaining(row-> sum.set(sum.get() + row.get("no").asLiteral().getInt()));

    return sum.getAsInt();
    

  }
}
