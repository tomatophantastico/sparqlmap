package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.aksw.sparqlmap.backend.metamodel.TranslationContextMetaModel;
import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.metamodel.DataContext;

public class MetaModelQueryExecution implements QueryExecution{
  

 
  
  
  public MetaModelQueryExecution(TranslationContextMetaModel tc, DataContext dcontext, boolean rowwiseBlanks) {
    super();

    
    this.query = tc.getTcontext().getQuery();
    
    dataset = DatasetImpl.wrap(MetaModelQueryDump.assembleDs(tc.getTcontext(), dcontext, rowwiseBlanks)); 
    
    
  }

  private Query query;
  private Dataset dataset;
  
  

  @Override
  public void setInitialBinding(QuerySolution binding) {
    throw new ImplementationException("Not implemented");
  }

  @Override
  public Dataset getDataset() {
    return dataset;
  }

  @Override
  public Context getContext() {
    throw new ImplementationException("Not implemented");
  }

  @Override
  public Query getQuery() {
    return query;
  }

  @Override
  public ResultSet execSelect() {
    return QueryExecutionFactory.create(query, dataset).execSelect();
  }

  @Override
  public Model execConstruct() {
    
    return QueryExecutionFactory.create(query, dataset).execConstruct();

  }

  @Override
  public Model execConstruct(Model model) {
    return QueryExecutionFactory.create(query, dataset).execConstruct(model);

  }

  @Override
  public Iterator<Triple> execConstructTriples() {
        return QueryExecutionFactory.create(query, dataset).execConstructTriples();

  }

  @Override
  public Iterator<Quad> execConstructQuads() {
    return QueryExecutionFactory.create(query, dataset).execConstructQuads();

  }

  @Override
  public Dataset execConstructDataset() {
    return QueryExecutionFactory.create(query, dataset).execConstructDataset();

  }

  @Override
  public Dataset execConstructDataset(Dataset dataset) {
    return QueryExecutionFactory.create(query, dataset).execConstructDataset(dataset);

  }

  @Override
  public Model execDescribe() {
    
    // as the query is rewritten to a construct query, we execture the construct here
    return QueryExecutionFactory.create(query, dataset).execConstruct();
  

  }

  @Override
  public Model execDescribe(Model model) {
    // as the query is rewritten to a construct query, we execture the construct here
    return QueryExecutionFactory.create(query, dataset).execConstruct(model);


  }

  @Override
  public Iterator<Triple> execDescribeTriples() {
    // as the query is rewritten to a construct query, we execture the construct here
    return  QueryExecutionFactory.create(query, dataset).execConstructTriples();

  }
  
  


  @Override
  public boolean execAsk() {
    return QueryExecutionFactory.create(query, dataset).execAsk();

  }

  @Override
  public void abort() {
    throw new ImplementationException("Abort not implemented");
  }

  @Override
  public void close() {
    dataset = null;
 }

  @Override
  public boolean isClosed() {
    return dataset==null;
  }

  @Override
  public void setTimeout(long timeout, TimeUnit timeoutUnits) {
    throw new ImplementationException("Timeout not implemented");
    
  }

  @Override
  public void setTimeout(long timeout) {
    throw new ImplementationException("Timeout not implemented");
    
  }

  @Override
  public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
    throw new ImplementationException("Timeout not implemented");
    
  }

  @Override
  public void setTimeout(long timeout1, long timeout2) {
    throw new ImplementationException("Timeout not implemented");
    
  }

  @Override
  public long getTimeout1() {
    return 0;
  }

  @Override
  public long getTimeout2() {
    return 0;
  }

}
