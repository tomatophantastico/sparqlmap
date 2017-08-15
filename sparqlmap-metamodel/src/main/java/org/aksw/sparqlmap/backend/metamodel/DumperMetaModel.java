package org.aksw.sparqlmap.backend.metamodel;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelContext;
import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelQueryDump;
import org.aksw.sparqlmap.backend.metamodel.translate.TripleStreamUtils;
import org.aksw.sparqlmap.core.Dumper;
import org.aksw.sparqlmap.core.errors.SystemInitializationError;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.writer.NQuadsWriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DumperMetaModel implements Dumper{


  private R2RMLMapping r2rmlMapping;
  
  private MetaModelContext mcontext;
 
  
  
  
  
  
  public DumperMetaModel(MetaModelContext mcontext, R2RMLMapping r2rmlMapping) {
    super();
    
    
    this.mcontext = mcontext;
    this.r2rmlMapping = r2rmlMapping;
  }



  public DatasetGraph dumpDatasetGraph(){
    DatasetGraph dsg = MetaModelQueryDump.assembleDs(r2rmlMapping.getQuadMaps(), mcontext.getDataContext());
    return dsg;
    
  }



  @Override
  public Stream<Quad> streamDump() {
    
    return dump(null, true).flatMap(quadbucket -> {
      Collection<Quad> quads = Lists.newArrayList();
      quadbucket.forEach((graph, triple) -> {
        quads.add(new Quad(graph, triple));
      });
      return quads.stream();
      
    });
    
    
  }


  /**
   * Write an NQuads serailization into the outoutstream
   */
  public void streamDump(OutputStream outstream) {
    
    

    NQuadsWriter.write(outstream, streamDump().iterator());
    try {
      outstream.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  
  
  @Override
  public void dump(OutputStream out, Lang lang) {
    dump(out, RDFWriterRegistry.defaultSerialization(lang));
    
  }
  
  /**
   * @param out
   * @param lang
   */
  public void dump(OutputStream out, RDFFormat lang){
    
    // either 
    WriterDatasetRIOT dsWriter = Optional.ofNullable(RDFWriterRegistry.getWriterDatasetFactory(lang)).map(fac -> fac.create(lang)).orElse(null);
    WriterGraphRIOT grWriter = Optional.ofNullable(RDFWriterRegistry.getWriterGraphFactory(lang)).map(fac -> fac.create(lang)).orElse(null);
    
    if(grWriter!=null){
      dump(null, true).map(quads ->  new CollectionGraph(quads.values()) ).forEach(graph -> {
        grWriter.write(out, graph,null,null,new Context());
      });
    }else if(dsWriter!=null){
      dump(null,true).map(TripleStreamUtils.mapToDatasetGraphMap()).forEach(dsg -> {
        dsWriter.write(out, dsg, null, null, new Context());

      });
    }else{
      throw new SystemInitializationError(String.format("Unsupported output format, currently supported is: NTRIPELS,NQUADS, TURTLE, TRIG", lang));
    }
    

  }
  
  /**
   * Opens a stream from a queue for backpressure handling.
   * @param fast if set to false, the tables are iterated sequentially, if true parallel querying is enabled
   * @param mappingfilters 
   * @return
   */
  public Stream<Multimap<Node, Triple>> dump(Collection<String> mappingfilters, boolean fast){
    
    this.r2rmlMapping.getQuadMaps();
    ExecutorService execService = null;
    if(fast){
      execService = Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors(), 
          new ThreadFactoryBuilder().setDaemon(true).build());
    }else{
      execService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
    }
    
    Collection<QuadMap> qms;
    if(mappingfilters!=null){
      qms = r2rmlMapping.getQuadMaps().stream().filter(
          qm-> mappingfilters.stream().anyMatch(
              filterString->qm.getTriplesMapUri().contains(filterString))
          ).collect(Collectors.toList());
    }else{
      qms = r2rmlMapping.getQuadMaps();
    }
    
     
    
    
    return MetaModelQueryDump.streamFast(qms, mcontext.getDataContext(), execService, mcontext.isRowwiseBlanks());
    
  }
  
  
  
  
 
  
   
  

}
