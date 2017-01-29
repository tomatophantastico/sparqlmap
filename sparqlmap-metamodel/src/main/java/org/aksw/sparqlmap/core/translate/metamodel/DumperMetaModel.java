package org.aksw.sparqlmap.core.translate.metamodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.sparqlmap.core.Dumper;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.writer.NQuadsWriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class DumperMetaModel implements Dumper{


  private R2RMLMapping r2rmlMapping;
  
  private MetaModelContext mcontext;
 
  
  
  
  
  
  public DumperMetaModel(MetaModelContext mcontext, R2RMLMapping r2rmlMapping) {
    super();
    
    
    this.mcontext = mcontext;
    this.r2rmlMapping = r2rmlMapping;
  }



  public DatasetGraph dumpDatasetGraph(){
    DatasetGraph dsg = MetaModelQueryDump.assembleDs(r2rmlMapping.getQuadMaps().values(), mcontext.getDataContext());
    return dsg;
    
  }



  @Override
  public Iterator<Quad> streamDump() {
    
    
    return dumpDatasetGraph().find();
    
//    List<Iterator<Quad>> quadIters = Lists.newArrayList();
//    
//    
//    for(String mappingUri : Sets.newTreeSet(r2rmlMapping.getQuadMaps().keys())){
//      
//      List<QuadMap> quadMaps  = Lists.newArrayList(r2rmlMapping.getQuadMaps().get(mappingUri));
//      //get the Logical table from the first quad map, they *must* be all the same by R2RML convention
//      LogicalTable ltable = quadMaps.get(0).getLogicalTable();
//      //get all columns mentioned in the quads
//      
//      
//      MetaModelQueryWrapper qw = new MetaModelQueryWrapper(mcontext);
//       
//      int quadcount = 0; 
//       
//       for(QuadMap quadMap: quadMaps){
//         Quad dumpQuad = new Quad(NodeFactory.createVariable("g_" + quadcount ), 
//               NodeFactory.createVariable("s"), NodeFactory.createVariable("p_"+quadcount), NodeFactory.createVariable("o_"+quadcount));
//         quadcount++;
//         
//         qw.addQuad(dumpQuad, quadMap, true);
//
//        }
//       
//      
//       quadIters.add(new QueryExecutor(qw, mcontext.getConConf().getBaseUri(),mcontext.getDataContext()));
//
//    }
//    return     Iterators.concat(quadIters.iterator());
  }



  public void streamDump(OutputStream stream) {
    
    

    NQuadsWriter.write(stream, streamDump());
    try {
      stream.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  
  /**
   * Currently materializes the data in memory before: beware!
   * @param out
   * @param format
   */
  public void dump(OutputStream out, Lang format){
    
    RDFDataMgr.write(out, dumpDatasetGraph(), format);
    
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
      qms = r2rmlMapping.getQuadMaps().values().stream().filter(
          qm-> mappingfilters.stream().anyMatch(
              filterString->qm.getTriplesMapUri().contains(filterString))
          ).collect(Collectors.toList());
    }else{
      qms = r2rmlMapping.getQuadMaps().values();
    }
    
     
    
    
    return MetaModelQueryDump.streamFast(qms, mcontext.getDataContext(), execService);
    
  }
  
  
  
  
 
  
   
  

}
