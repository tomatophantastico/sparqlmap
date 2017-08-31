package org.aksw.sparqlmap.backend.metamodel;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.QueryMetadata;
import org.aksw.sparqlmap.core.SparqlMapPhase;
import org.aksw.sparqlmap.core.UpdateRequestBinding;
import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityChecker;
import org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityRequires;
import org.aksw.sparqlmap.core.mapper.finder.BindingFunctions;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;


public class MetamodelUpdateProcessor implements UpdateProcessor {


  private UpdateableDataContext dataContext;
  private UpdateRequestBinding updateRequestBinding;
  private QueryMetadata updateMetadata;



  public MetamodelUpdateProcessor(UpdateableDataContext dataContext,
      UpdateRequestBinding updateRequestBinding, QueryMetadata updataMetadata) {
    super();
    this.dataContext = dataContext;
    this.updateRequestBinding = updateRequestBinding;
    this.updateMetadata = updataMetadata;

  }

  @Override
  public Context getContext() {
    return null;
  }

  @Override
  public DatasetGraph getDatasetGraph() {
    throw new UnsupportedOperationException(
        "DatasetGraph not available, use dump methods to aquire.");
  }

  @Override
  public void execute() {
    List<UpdateScript> operations = Lists.newArrayList();

    updateMetadata.start(SparqlMapPhase.TRANSLATE);

    this.updateRequestBinding.getUpdateBindings().forEach((update, binding) -> {


      binding.getRows().forEach(union -> {
        Iterator<Quad> headIter = binding.getHead().iterator();

        List<Tuple3<Node, Node, TermMap>> insertables = union.stream()
            // increment synchronous over the two streams
            .map(quadmap -> Tuple.tuple(headIter.next(), quadmap))
            // convert into term/termmap pairs
            .flatMap(BindingFunctions::termWiseWithQuad)
            // ( quadSubject, node, termmap)
            .map(t3 -> {
              return Tuple.tuple(t3.v2.getSubject(), t3.v1, t3.v3);
            })
            // consider every pair only once
            .distinct().collect(Collectors.toList());
        // convert the binding requirements


        List<Tuple4<Node, Table, Column, Object>> requireds = insertables.stream().flatMap(t3 -> {
          List<CompatibilityRequires> reqs =
              CompatibilityChecker.requires(t3.v3, t3.v2).orElse(Lists.newArrayList());

          List<Tuple2<Column, Object>> colValues =
              reqs.stream().map(bind(dataContext)).collect(Collectors.toList());


          return colValues.stream().map(t2 -> {
            return Tuple.tuple(t3.v1, t2.v1.getTable(), t2.v1, t2.v2);
          });
        }).collect(Collectors.toList());
        Multimap<Node, Tuple3<Table, Column, Object>> bySubject = requireds.stream()
            .collect(ImmutableListMultimap.toImmutableListMultimap(t4 -> t4.v1, Tuple4::skip1));
        bySubject.asMap().forEach((subject, t3s) -> {
          t3s.stream()
              .collect(ImmutableListMultimap.toImmutableListMultimap(t3 -> t3.v1, Tuple3::skip1))
              .asMap().forEach((table, t2s) -> {
                InsertInto insInto = new InsertInto(table);
                t2s.forEach(t2 -> insInto.value(t2.v1, t2.v2));
                operations.add(insInto);
              });

        });



      });

    });

    updateMetadata.stop(SparqlMapPhase.TRANSLATE);
    updateMetadata.start(SparqlMapPhase.EXECUTE);

    operations.forEach(operation -> {

      dataContext.executeUpdate(operation);
    });
    updateMetadata.stop(SparqlMapPhase.EXECUTE);


  }


  public static Function<Tuple4<Node, Quad, TermMap, QuadMap>, Object> expand() {
    return (t4) -> {
      return t4;
    };

  }



  public static Function<CompatibilityRequires, Tuple2<Column, Object>> bind(DataContext context) {
    return (requires) -> {
      LogicalTable ltab = requires.getColumn().getTable();

      // not covering the sql views at the moment
      if (ltab.getTablename() == null) {
        throw new ImplementationException("Cannot insert data into mappings with rr:sqlQuery");
      }
      Table tab = context.getDefaultSchema().getTableByName(ltab.getName());

      Column col = tab.getColumnByName(requires.getColumn().getName());

      Object val = requires.getValue();

      return Tuple.tuple(col, val);
    };
  }

}
