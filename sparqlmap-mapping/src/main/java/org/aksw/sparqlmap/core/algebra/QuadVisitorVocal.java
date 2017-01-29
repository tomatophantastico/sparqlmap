package org.aksw.sparqlmap.core.algebra;

import org.aksw.sparqlmap.core.errors.ImplementationException;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTopN;


/**
 * This Visitor throws exceptions for not overriden methods
 * 
 * @author joerg
 *
 */
public class QuadVisitorVocal extends QuadVisitorBase {





  @Override
  public void visit(OpPath opPath) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpProcedure opProc) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpPropFunc opPropFunc) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpJoin opJoin) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpSequence opSequence) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpDisjunction opDisjunction) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpLeftJoin opLeftJoin) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpConditional opCond) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpMinus opMinus) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpDiff opDiff) {
    throw new ImplementationException("Not Implemeted yet");

  }



  @Override
  public void visit(OpService opService) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpDatasetNames dsNames) {
    throw new ImplementationException("Not Implemeted yet");

  }



  @Override
  public void visit(OpExt opExt) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpNull opNull) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpLabel opLabel) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpAssign opAssign) {
    throw new ImplementationException("Not Implemeted yet");

  }


  @Override
  public void visit(OpList opList) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpOrder opOrder) {
    throw new ImplementationException("Not Implemeted yet");

  }





  @Override
  public void visit(OpReduced opReduced) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpSlice opSlice) {
   throw new ImplementationException("Not Implemeted yet");
  }

  @Override
  public void visit(OpGroup opGroup) {
    throw new ImplementationException("Not Implemeted yet");

  }

  @Override
  public void visit(OpTopN opTop) {
    throw new ImplementationException("Not Implemeted yet");

  }
  
  
  

}
