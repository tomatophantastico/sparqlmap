package org.aksw.sparqlmap.core;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.mapper.finder.FilterFinder;
import org.aksw.sparqlmap.core.mapper.finder.QueryInformation;
import org.aksw.sparqlmap.core.mapper.finder.StreamingBinder;
import org.aksw.sparqlmap.core.normalizer.QueryNormalizer;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;

import lombok.Getter;
import lombok.Setter;


/**
 *  The main class of Sparqlmap.
 *  Provides methods for executing SPARQL-queries over mapped databases. 
 * @author joerg
 *
 */
public class SparqlMap implements Closeable{
  
  private static Logger log = LoggerFactory.getLogger(SparqlMap.class);

  private R2RMLMapping mapping;
  
  private StreamingBinder binder;

  /**
   * The list of backends to use.
   *
   */
  @Getter
  @Setter
  private List<SparqlMapBackend> backends;

  
  private Queue<QueryMetadata> queryMetaData = EvictingQueue.create(1000);



  public void setMapping(R2RMLMapping mapping) {
    this.mapping = mapping;
    this.binder = new StreamingBinder(mapping);
  }
  
  public R2RMLMapping getMapping() {
    return mapping;
  }
  


  
  
  protected UpdateProcessor executeUpdate(UpdateRequest request, QueryMetadata queryMeta) {
    
    
    queryMeta.start(SparqlMapPhase.BIND);
    UpdateRequestBinding updateBinding = binder.bind(request);
    queryMeta.stop(SparqlMapPhase.BIND);
   

    Optional<SparqlMapBackend> updateBackend = backends.stream().filter(backend -> backend.updateEnabled()).findFirst();

    
    if(updateBackend.isPresent()){
      return updateBackend.get().executeUpdate(updateBinding,queryMeta);

    }else{
      throw new UnsupportedOperationException("No updateable backend defined.");
    }
    
  
    
  }

  
  public UpdateProcessor update(UpdateRequest request){
    
    QueryMetadata queryMetadata = new QueryMetadata();
    this.queryMetaData.add(queryMetadata);
    queryMetadata.setQueryString(request.toString());
    return executeUpdate(request, new QueryMetadata());

  }
  
  public UpdateProcessor update(String query){
    QueryMetadata queryMetadata = new QueryMetadata();
    this.queryMetaData.add(queryMetadata);
    queryMetadata.setQueryString(query);
    queryMetadata.start(SparqlMapPhase.PARSE);
    UpdateRequest updateRequest = UpdateFactory.create(query);
    queryMetadata.stop(SparqlMapPhase.PARSE);
	  return executeUpdate(updateRequest, queryMetadata);
  }


  public QueryExecution execute(String query){
    QueryMetadata queryMetadata  = new QueryMetadata();
    this.queryMetaData.add(queryMetadata);
    TranslationContext tcontext = new  TranslationContext();
    
    tcontext.setQueryString(query);
    
    return executeQuery(tcontext,queryMetadata);
  }
  
  public QueryExecution execute(TranslationContext tcontext){
     
    
    return executeQuery(tcontext, new QueryMetadata());
    
    
    
    
  }
  
  
  protected QueryExecution executeQuery(TranslationContext tcontext, QueryMetadata metadata) {

    //Perform the common tasks
    
    //check if the query is compiled
    if(tcontext.getQuery()==null){
      
      metadata.start(SparqlMapPhase.PARSE);
      tcontext.setQuery(QueryFactory.create(tcontext.getQueryString()));
      metadata.stop(SparqlMapPhase.PARSE);
    }
    
    if(log.isDebugEnabled()){
      log.debug(tcontext.getQuery().toString());
    }
    metadata.start(SparqlMapPhase.OPTIMIZE);
    QueryNormalizer.normalize(tcontext);     
    metadata.stop(SparqlMapPhase.OPTIMIZE);
    
    
    if(log.isDebugEnabled()){
      log.debug(tcontext.getBeautifiedQuery().toString());
    }
    
    metadata.start(SparqlMapPhase.BIND);
    //Analyze Query
    QueryInformation qi = FilterFinder.getQueryInformation(tcontext.getBeautifiedQuery());
    tcontext.setQueryInformation(qi);
    
    metadata.stop(SparqlMapPhase.BIND);

    tcontext.setQueryBinding(binder.bind(tcontext.getBeautifiedQuery()));
    
    if(log.isDebugEnabled()){
      log.debug(tcontext.getQueryBinding().toString());
    }

    //delegate to the backend.
    
    
    Optional<SparqlMapBackend> queryBackend = backends.stream().filter(backend -> backend.queryEnabled()).findFirst();
    if(queryBackend.isPresent()) {
      return queryBackend.get().executeQuery(tcontext,metadata);
    }else {
      throw new UnsupportedOperationException("No backend with query capabilites configured, cannot execute query");
    }

    
  }
  

  
  
  
  
  public Dumper getDumpExecution(){
    Optional<SparqlMapBackend> dumpBackend = backends.stream().filter(SparqlMapBackend::dumpEnabled).findFirst();
    if(dumpBackend.isPresent()) {
      return dumpBackend.get().dump(this.mapping.getQuadMaps());
    }else {
      throw new UnsupportedOperationException("No backend with dump capabilites configured, cannot create dumper");
    }
  }
  
  public List<String> validateMapping(){
    return backends.stream()
        .filter(SparqlMapBackend::validateSchemaEnabled)
        .flatMap(backend -> backend.validateSchema(mapping).stream())
        .collect(Collectors.toList());

  }
  

public LogicalSchema getDefaultSchema() {
  
  return backends.iterator().next().getDefaultSchema();
}

  
  


  public void close(){
    backends.forEach(SparqlMapBackend::close);
  }
}
