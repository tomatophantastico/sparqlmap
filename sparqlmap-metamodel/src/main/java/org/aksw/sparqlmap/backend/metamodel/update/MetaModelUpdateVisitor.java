package org.aksw.sparqlmap.backend.metamodel.update;

import java.util.Collection;

import org.aksw.sparqlmap.core.mapper.finder.StreamingBinder;
import org.aksw.sparqlmap.core.mapper.finder.StreamingBindingVisitor;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateMove;
import org.apache.jena.sparql.modify.request.UpdateVisitor;

public class MetaModelUpdateVisitor implements UpdateVisitor{

  private Collection<QuadMap> quadMaps;

  @Override
  public void visit(UpdateDrop update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
  }

  @Override
  public void visit(UpdateClear update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateCreate update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateLoad update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");    
  }

  @Override
  public void visit(UpdateAdd update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateCopy update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateMove update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");    
  }


  @Override
  public void visit(UpdateDataDelete update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateDeleteWhere update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public void visit(UpdateModify update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

  @Override
  public Sink<Quad> createInsertDataSink() {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
  }

  @Override
  public Sink<Quad> createDeleteDataSink() {
    throw new UnsupportedOperationException("This Update method is not yet implemented");

  }

  @Override
  public void visit(UpdateDataInsert update) {
    throw new UnsupportedOperationException("This Update method is not yet implemented");
    
  }

}
