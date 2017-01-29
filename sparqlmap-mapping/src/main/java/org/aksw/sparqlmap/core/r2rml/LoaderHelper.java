package org.aksw.sparqlmap.core.r2rml;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class LoaderHelper {

  public static Resource getSingleResourceObject(StmtIterator stmtIterator) {
    RDFNode rdfnode = getSingleRDFNode(stmtIterator);

    Resource result = null;
    if (rdfnode!=null) {
      result = rdfnode.asResource();
    }
    return result;
  }

  public static String getSingleLiteralObjectValue(StmtIterator stmtIterator) {
    RDFNode rdfnode = getSingleRDFNode(stmtIterator);
    String result = null;
    if(rdfnode!=null){
      result =  rdfnode.asLiteral().getLexicalForm();
    }
    return result;
    
  }
  
  public static RDFNode getSingleRDFNode(StmtIterator stmtIterator){
    RDFNode result = null;

    if (stmtIterator.hasNext()) {

      List<Statement> stmnts = stmtIterator.toList();
      if (stmnts.size() > 1) {
        throw new R2RMLValidationException("Expected only one statement here, got:" + stmnts.size());
      }

      RDFNode object = stmnts.get(0).getObject();

      result =  object;
    }
    return result;
    
  }
  

}
