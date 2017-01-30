package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.sparqlmap.core.errors.QueryExecutionException;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;




public class QueryExecutor implements Iterator<Quad> {

  
  private final MetaModelQueryWrapper querywrapper;
  
  private MetaModelRowBinder binder;

    
  private DataContext dataContext;
  
  private BlockingQueue<Quad> quadQueue = new ArrayBlockingQueue<Quad>(100);
  
  private ExecutorService execService;
  
  private boolean queryStarted = false;
  
  private boolean queryFinished = false;
  

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutor.class);

  public QueryExecutor(MetaModelQueryWrapper query, String baseuri, DataContext dataContext) {
    super();
    this.querywrapper = query;
    binder = new MetaModelRowBinder( baseuri);
    this.execService = Executors.newFixedThreadPool(1);
    this.dataContext = dataContext;
  }
  
  
  
  public QueryExecutor(MetaModelQueryWrapper query, String baseuri,DataContext dataContext, ExecutorService execService) {
    super();
    this.querywrapper = query;
    binder = new MetaModelRowBinder( baseuri);
    this.execService = execService;
    this.dataContext = dataContext;
  }
  
  
  
  
  
  
  @Override
  public boolean hasNext() {
    boolean result = false;
    Quad peek = quadQueue.peek();
    
    if(peek!=null && !peek.equals(posion)){
      result  = true;
    } else if(!queryFinished){
      //do some polling,
      // TODO implement better notification
      
      if(!queryStarted){
        start();
      }
      
      
      for(int i = 0; i< 1000; i++){
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if(!quadQueue.isEmpty()){
          result = true;
          break;
        }
      }
      throw new QueryExecutionException("Could not poll for dump result");
    }

    return result;
  }

  @Override
  public Quad next() {     
    if(!queryStarted){
      start();
    }
    try {
      return quadQueue.take();
    } catch (InterruptedException e) {
      throw new QueryExecutionException("Unable to read from internal quad queue",e);
    }
  }
  
  



  public void start(){
    if(!queryStarted){
      BackgroundLoader loader = new BackgroundLoader();
      execService.execute(loader);
    }
  }
  
  
  

  


  private class BackgroundLoader implements Runnable{
    
    // we use this cache to keep track of recently generated quads
    // if quads are produced multiple times, there is a chance that they are filtereds out like that.
    private Cache<Quad, Quad> cache = CacheBuilder.newBuilder().maximumSize(500).build();




    @Override
    public void run() {
      
      Iterator<Row> rowIter = dataContext.executeQuery(querywrapper.getQuery()).iterator();
      
      Map<Quad,QuadMap> quads2quadMaps = querywrapper.getQuads2quadMaps();
      
      
      
      
      try {
        
        while(rowIter.hasNext()){
          Row row = rowIter.next();
          
          for(Quad quad: quads2quadMaps.keySet()){
            QuadMap quadMap = quads2quadMaps.get(quad);
            
            
            
            // Constructing a Quad, position by position
            Node g = quad.getGraph().isConcrete() ? 
                quad.getGraph(): 
                binder.bind(
                    querywrapper.getSelectItems(quad.getGraph().getName()),
                    quadMap.getGraph(), row) ;
            Node s = quad.getSubject().isConcrete() ?
                quad.getSubject():
                  binder.bind(
                      querywrapper.getSelectItems(quad.getSubject().getName()),
                      quadMap.getSubject(), row);
            Node p = quad.getPredicate().isConcrete() ?
                quad.getPredicate():
                  binder.bind(
                      querywrapper.getSelectItems(quad.getPredicate().getName()),
                      quadMap.getPredicate(), row);;
            Node o = quad.getObject().isConcrete() ?
                quad.getObject():
                  binder.bind(
                      querywrapper.getSelectItems(quad.getObject().getName()),
                      quadMap.getObject(), row);;
           
            
            
            
            if(s != null && p !=null && o != null){
             
              
              Quad newQuad = new Quad(g,s,p,o);
              
              if(!(cache.getIfPresent(newQuad)!=null)){
                quadQueue.put(newQuad);

              }else{
                cache.put(newQuad, newQuad);
              }
              
            }

          }

        }
        
        quadQueue.put(posion);
      } catch (InterruptedException e) {
        LOGGER.error("Error filling the results queue", e);
      }
      
    }

  } 
  

  private Quad posion = Quad.create(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
  
}
