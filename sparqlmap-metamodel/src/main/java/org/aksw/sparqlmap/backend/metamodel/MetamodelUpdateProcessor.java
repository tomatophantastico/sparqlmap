package org.aksw.sparqlmap.backend.metamodel;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelQueryDump;
import org.aksw.sparqlmap.backend.metamodel.update.MetaModelUpdateVisitor;
import org.aksw.sparqlmap.core.UpdateContext;
import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityChecker;
import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityRequires;
import org.aksw.sparqlmap.core.mapper.finder.BindingFunctions;
import org.aksw.sparqlmap.core.mapper.finder.StreamingBindingVisitor;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.http.HeaderElementIterator;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateVisitorBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;


public class MetamodelUpdateProcessor implements UpdateProcessor {
  
  
  private UpdateableDataContext dataContext;
  private R2RMLMapping mapping;
  private UpdateRequest updateRequest; 
  
  private bench bench = new bench();

  public MetamodelUpdateProcessor(UpdateableDataContext dataContext, R2RMLMapping mapping, UpdateRequest updateRequest) {
    super();
    this.dataContext = dataContext;
    this.mapping = mapping;
    this.updateRequest = updateRequest;
  }
  
  public MetamodelUpdateProcessor(UpdateableDataContext dataContext, R2RMLMapping mapping, String query) {
    super();
    this.dataContext = dataContext;
    this.mapping = mapping;
    Stopwatch parse = Stopwatch.createStarted();
    this.updateRequest = UpdateFactory.create(query);
    parse.stop();
    bench.parsingTime = parse.elapsed(TimeUnit.MICROSECONDS);
  }

  @Override
  public Context getContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatasetGraph getDatasetGraph() {
    return MetaModelQueryDump.assembleDs(mapping.getQuadMaps(), dataContext);
  }

  @Override
  public void execute() {
    List<UpdateScript> operations = Lists.newArrayList();
    
    
    updateRequest.forEach(update -> update.visit(new MetaModelUpdateVisitor(){

      @Override
      public void visit(UpdateDataInsert update) {
        Stopwatch queryCreation = Stopwatch.createUnstarted();
        Stopwatch bind = Stopwatch.createUnstarted();
        StreamingBindingVisitor visitor = new StreamingBindingVisitor(mapping.getQuadMaps());
        update.getQuads().stream().forEach(quad -> visitor.join(quad));

        
        visitor.getRows().forEach(union -> {
          bind.start();
          Iterator<Quad> headIter = visitor.getHeading().iterator();

          List<Tuple3<Node, Node, TermMap>> insertables = union.stream()
              //increment synchronous over the two streams
              .map(quadmap -> Tuple.tuple(headIter.next(),quadmap))
              //convert into term/termmap pairs
              .flatMap(BindingFunctions::termWiseWithQuad)
              // ( quadSubject, node, termmap)
              .map(t3 -> {return Tuple.tuple(t3.v2.getSubject(),t3.v1,t3.v3);})
              //consider every pair only once
              .distinct().collect(Collectors.toList());
              //convert the binding requirements
        bind.stop();
        queryCreation.start();
          List<Tuple4<Node,Table,Column,Object>> requireds =  insertables.stream().flatMap(t3 ->{
            List<CompatibilityRequires> reqs = CompatibilityChecker
                .requires(t3.v3,t3.v2).orElse(Lists.newArrayList()); 
            
            List<Tuple2<Column,Object>> colValues = reqs.stream()
                .map(bind(dataContext)).collect(Collectors.toList());
            
            
               return colValues.stream()
                    .map(t2 -> {
                      return Tuple.tuple(
                          t3.v1,t2.v1.getTable(),
                          t2.v1,
                          t2.v2);});
                    }).collect(Collectors.toList());
          Multimap<Node,Tuple3<Table,Column,Object>> bySubject = 
              requireds.stream().collect(ImmutableListMultimap.toImmutableListMultimap(t4 -> t4.v1, Tuple4::skip1));
          bySubject.asMap().forEach((subject,t3s) -> {                 
               t3s.stream().collect(ImmutableListMultimap.toImmutableListMultimap(t3 -> t3.v1, Tuple3::skip1))
               .asMap().forEach((table,t2s) -> {
                 InsertInto insInto = new InsertInto(table);
                 t2s.forEach(t2 -> insInto.value(t2.v1, t2.v2));
                 operations.add(insInto);
               });
               
             } );
          queryCreation.stop();
        });
        
        bench.bindingTime = bind.elapsed(TimeUnit.MICROSECONDS);
        bench.queryCreationTime = queryCreation.elapsed(TimeUnit.MICROSECONDS);
        
      }
    }));
    Stopwatch exec = Stopwatch.createStarted();  
    operations.forEach(operation -> {
      
      dataContext.executeUpdate(operation);
    });
    exec.stop();
    bench.executionTime = exec.elapsed(TimeUnit.MICROSECONDS);
    
    bench.append();
    
    
  }
  
  
  public static Function<Tuple4<Node,Quad,TermMap,QuadMap>,Object> expand(){
    return (t4) -> {return t4;};
    
  }
  
  
  
  
  public static Function<CompatibilityRequires, Tuple2<Column,Object>> bind(DataContext context){
    return (requires) -> {
      LogicalTable ltab = requires.getColumn().getTable();
      
      //not covering the sql views at the moment
      if(ltab.getTablename() == null){
        throw new ImplementationException("Cannot insert data into mappings with rr:sqlQuery");
      }
      Table tab = context.getDefaultSchema().getTableByName(ltab.getName());
      
      Column col = tab.getColumnByName(requires.getColumn().getName());

      Object val = requires.getValue();

      return Tuple.tuple(col,val );};
  }
  
  
  public static void bindToMetaMode(CompatibilityRequires requies){
    
    
  }
  
  
  
  private class bench{
    
   
   long parsingTime;
   long bindingTime;
   long queryCreationTime;
   long executionTime;
    
    
    void append(){
      try {
        Files.append(String.format("%s\n", parsingTime), new File("./build/parsingTime.csv"), Charsets.UTF_8);
        
        Files.append(String.format("%s\n", bindingTime), new File("./build/bindingTime.csv"), Charsets.UTF_8);
        Files.append(String.format("%s\n", queryCreationTime), new File("./build/queryCreationTime.csv"), Charsets.UTF_8);
        Files.append(String.format("%s\n", executionTime), new File("./build/executionTime.csv"), Charsets.UTF_8);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    
    
  }

}
