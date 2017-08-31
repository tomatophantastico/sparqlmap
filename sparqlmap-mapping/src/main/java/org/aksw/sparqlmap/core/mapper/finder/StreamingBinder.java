package org.aksw.sparqlmap.core.mapper.finder;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.UpdateRequestBinding;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateVisitorBase;
import org.apache.jena.update.UpdateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamingBinder {

  private final R2RMLMapping mapping;


  public QueryBinding bind(TranslationContext tc) {


    return bind(tc.getBeautifiedQuery());
  }


  public QueryBinding bind(Op op) {
    StreamingBindingVisitor visitor = new StreamingBindingVisitor(mapping.getQuadMaps());

    OpWalker.walk(op, visitor);

    return QueryBinding.builder().head(visitor.heading).rows(visitor.rows).build();
  }

  public UpdateRequestBinding bind(UpdateRequest updateRequest) {
    UpdateRequestBinding upReqBind = new UpdateRequestBinding(updateRequest);


    updateRequest.forEach(update -> update.visit(new UpdateVisitorBase() {

      @Override
      public void visit(UpdateDataInsert update) {
        StreamingBindingVisitor visitor = new StreamingBindingVisitor(mapping.getQuadMaps());

        visitor.join(update.getQuads());

        upReqBind.getUpdateBindings().put(update,
            QueryBinding.builder().head(visitor.heading).rows(visitor.rows).build());
      }

    }));

    return upReqBind;

  }

}
