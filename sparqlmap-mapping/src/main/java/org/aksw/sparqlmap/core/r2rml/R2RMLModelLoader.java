package org.aksw.sparqlmap.core.r2rml;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;

import com.google.common.collect.Maps;

import com.google.common.collect.Lists;

/**
 * 
 * Contains the logic to create loaded and connected representation of the the R2RML model.
 * 
 * @author joerg
 *
 */
public class R2RMLModelLoader {
    
  public static final String BNODE_RESOLV_PREFIX = "http://aksw.org/Projects/SparqlMap/bnodeResolv/";
  
  public static R2RMLMapping loadModel(Model toLoad, Model r2rmlspec, String baseIri){
    Model original = ModelFactory.createDefaultModel();
    original.add(toLoad.listStatements().toList());
    
    toLoad = ModelFactory.createRDFSModel(r2rmlspec, toLoad);
    
    //resolve the rr:class syntactic sugar
    resolveRRClassStatements(toLoad);
    
    //resolve all r2rml shortcuts
    resolveR2RMLShortcuts(toLoad);
        
    //resolve multiple graph declarations
    resolveMultipleGraphs(toLoad);
    
    //convert all blank nodes into non-anonymous resources.
    resolveBlankNodes(toLoad);
    
    String desc = loadDescription(toLoad);
    
    //load the triples maps
    
    R2RMLMapping mapping = new R2RMLMapping(QuadMapLoader.load(toLoad, baseIri),original,desc);

    
      
    return mapping;
    
   
    
  }
  
  private static String loadDescription(Model toLoad) {
    
    List<String> labels = Lists.newArrayList();
    //check if we got a baseuri-resource
    toLoad.listResourcesWithProperty(
        RDF.type,VOID.Dataset).forEachRemaining(
            res -> toLoad.listObjectsOfProperty(DCTerms.title)
            .filterKeep(n -> n.isLiteral()).forEachRemaining(n->labels.add(n.toString())));
    
    return labels.isEmpty()?"Unlabeled Mapping":labels.iterator().next();
  }

  private static void resolveBlankNodes(Model r2rmlModel) {
    Map<Resource,Resource> bndoe2Resoure = Maps.newHashMap(); 
    List<Statement> toAdd =  Lists.newArrayList();
    List<Statement> toRemove = Lists.newArrayList();
    StmtIterator stmtIter =  r2rmlModel.listStatements();
    while(stmtIter.hasNext()){
      Statement stmt = stmtIter.next();
      if(stmt.getSubject().isAnon() || stmt.getObject().isAnon()){
        Resource subject = stmt.getSubject();
        RDFNode object = stmt.getObject();
        Resource newSubject = null;
        RDFNode newObject = null;
        if(subject.isAnon()){
          if(bndoe2Resoure.containsKey(subject)){
            newSubject = bndoe2Resoure.get(subject);
          }else{
            newSubject = ResourceFactory.createResource(BNODE_RESOLV_PREFIX +  UUID.randomUUID().toString());
            bndoe2Resoure.put(subject, newSubject);
          }
        }else{
          newSubject = subject;
        }
        
        if(object.isAnon()){
          if(bndoe2Resoure.containsKey(object)){
            newObject = bndoe2Resoure.get(object);
          }else{
            newObject = ResourceFactory.createResource(BNODE_RESOLV_PREFIX +  UUID.randomUUID().toString());
            bndoe2Resoure.put((Resource) object, (Resource)newObject);
          }
        }else{
          newObject = object;
        }
        toAdd.add(new StatementImpl(newSubject, stmt.getPredicate(), newObject));
        toRemove.add(stmt);
      }
    }
    r2rmlModel.remove(toRemove);
    r2rmlModel.add(toAdd);
  }

  private static void resolveR2RMLShortcuts(Model reasoningModel) {
    String query = "PREFIX  rr:   <http://www.w3.org/ns/r2rml#> INSERT { ?x rr:subjectMap [ rr:constant ?y ]. } WHERE {?x rr:subject ?y.}";
    UpdateExecutionFactory.create(UpdateFactory.create(query),
        GraphStoreFactory.create(reasoningModel)).execute();
    query = "PREFIX  rr:   <http://www.w3.org/ns/r2rml#> INSERT { ?x rr:predicateMap [ rr:constant ?y ]. } WHERE {?x rr:predicate ?y.}";
    UpdateExecutionFactory.create(UpdateFactory.create(query),
        GraphStoreFactory.create(reasoningModel)).execute();
    query = "PREFIX  rr:   <http://www.w3.org/ns/r2rml#> INSERT { ?x rr:objectMap [ rr:constant ?y ]. } WHERE {?x rr:object ?y.}";
    UpdateExecutionFactory.create(UpdateFactory.create(query),
        GraphStoreFactory.create(reasoningModel)).execute();
    query = "PREFIX  rr:   <http://www.w3.org/ns/r2rml#> INSERT { ?x rr:graphMap [ rr:constant ?y ]. } WHERE {?x rr:graph ?y.}";
    UpdateExecutionFactory.create(UpdateFactory.create(query),
        GraphStoreFactory.create(reasoningModel)).execute();
    reasoningModel.size();
    

  }

  
  
  private static void resolveMultipleGraphs(Model r2mlModel) {
    
    //for all triple maps with multiple graph statements we first get all the subject triple maps and put them into the po maps
    
    List<Resource> allTripleMaps = r2mlModel.listSubjectsWithProperty(RDF.type, R2RML.TRIPLESMAP).toList();
    
    for(Resource tripleMap : allTripleMaps){
      
      //get the subject, we assume that the r2rml is valid and therefore has only one subject.
      Resource subject = r2mlModel.listObjectsOfProperty(tripleMap, R2RML.HASSUBJECTMAP).next().asResource();
      
      //get the graph resource
      List<RDFNode> subjectGraphMaps = r2mlModel.listObjectsOfProperty(subject, R2RML.HASGRAPHMAP).toList();
          
      //for all these graph statements
      for(RDFNode graph: subjectGraphMaps){
        for(RDFNode po: r2mlModel.listObjectsOfProperty(tripleMap,R2RML.HASPREDICATEOBJECTMAP).toList()){
          //we add the the graph map into the PO map
          r2mlModel.add(po.asResource(),R2RML.HASGRAPHMAP,graph);
        }
      }
      
      // and remove them from the mapping
      for (RDFNode graph : subjectGraphMaps) {
        r2mlModel.remove(subject,R2RML.HASGRAPHMAP,graph);
      }
    }
  }
  
  
  /**
   * removes all apostrophes from the template and split it
   * @return
   */

 
  
  
  private static void resolveRRClassStatements(Model r2rmlModel) {
    
    for (Statement triplesMapClassStmt : r2rmlModel.listStatements((Resource)null, R2RML.HASCLASS,(RDFNode) null).toList()){
      Resource subjectMap = triplesMapClassStmt.getSubject();
      
      //get all triples Maps where this is used
      for(Resource typedTRiplesMap : r2rmlModel.listResourcesWithProperty(R2RML.HASSUBJECTMAP,subjectMap).toList()){
        Resource classResource = triplesMapClassStmt.getResource();
        
        triplesMapClassStmt.remove();
        
        Resource poBlankNode = r2rmlModel.createResource();
        typedTRiplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP, poBlankNode);
        poBlankNode.addProperty(R2RML.HASPREDICATE, RDF.type);
        poBlankNode.addProperty(R2RML.HASOBJECT, classResource);
      }
    }
  }

  
  

 

}
