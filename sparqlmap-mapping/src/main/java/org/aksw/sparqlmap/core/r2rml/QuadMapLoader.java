package org.aksw.sparqlmap.core.r2rml;

import java.util.List;
import java.util.Set;

import org.aksw.sparqlmap.core.r2rml.QuadMap.LogicalTable;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class QuadMapLoader {

  public static Multimap<String,QuadMap> load(Model model, String baseIri) {
    Multimap<String,QuadMap> quadMaps = HashMultimap.create();

    for (Resource triplesMapUri : model.listResourcesWithProperty(RDF.type, R2RML.TRIPLESMAP).toList()) {
      
      
      
      Resource logicalTable = triplesMapUri.getRequiredProperty(R2RML.HASLOGICALTABLE).getObject().asResource();
      StmtIterator tablenames = model.listStatements(logicalTable, R2RML.HASTABLENAME, (RDFNode) null);
      String tablename = LoaderHelper.getSingleLiteralObjectValue(tablenames);
      tablename = R2RMLHelper.unescape(tablename);
      StmtIterator queries = model.listStatements(logicalTable, R2RML.HASSQLQUERY, (RDFNode) null);
      String query = LoaderHelper.getSingleLiteralObjectValue(queries);
      query = R2RMLHelper.cleanSelectQuery(query);
      StmtIterator versions = model.listStatements(logicalTable, R2RML.SQLVERSION, (RDFNode) null);
      Resource versionResource = LoaderHelper.getSingleResourceObject(versions);
      String version  = versionResource != null?versionResource.getURI():null;
      // load the subject Map

      StmtIterator subjectMaps = model.listStatements(triplesMapUri, R2RML.HASSUBJECTMAP, (RDFNode) null);
      Resource subjectMap = LoaderHelper.getSingleResourceObject(subjectMaps);
      TermMap subject = TermMapLoader.load(model, subjectMap,baseIri);

      // here we store the graph maps declared on the subject, and therefore
      // triples map level.
      List<TermMap> triplesMapGraphMaps = Lists.newArrayList();

      // and load the respective term maps
      List<Statement> graphMaps = subjectMap.listProperties(R2RML.HASGRAPHMAP).toList();
      for (Statement graphMapStmnt : graphMaps) {
        triplesMapGraphMaps.add(TermMapLoader.load(model, graphMapStmnt.getObject().asResource(),baseIri));
      }

      // get the pos
      List<Statement> pos = model.listStatements(triplesMapUri, R2RML.HASPREDICATEOBJECTMAP, (RDFNode) null).toList();

      for (Statement po : pos) {
        if (!po.getObject().isResource()) {
          throw new R2RMLValidationException("non-resource in object position of rr:predicateObjectMap");
        }

        Resource poMap = po.getObject().asResource();
        
        List<Resource> predicateMaps = 
            model.listStatements(poMap, R2RML.HASPREDICATEMAP, (RDFNode) null).mapWith(stmt -> stmt.getObject().asResource()).toList();
        
        for(Resource predicateMap: predicateMaps){

          TermMap predicate = TermMapLoader.load(model, predicateMap, baseIri);
  
          Resource objectMap = LoaderHelper.getSingleResourceObject(
               model.listStatements(poMap, R2RML.HASOBJECTMAP, (RDFNode) null));
          TermMap object = TermMapLoader.load(model, objectMap, baseIri);
  
          List<TermMap> pographs = Lists.newArrayList();
          // and load the respective term maps
          List<Statement> pographMaps = poMap.listProperties(R2RML.HASGRAPHMAP).toList();
          for (Statement pographMapStmnt : pographMaps) {
            TermMap graph = TermMapLoader.load(model, pographMapStmnt.getObject().asResource(),baseIri);
            //we map the rr:defaultGraph to the jena default graph
            if(graph instanceof TermMapConstant && ((TermMapConstant) graph).getConstantIRI().equals(R2RML.DEFAULTGRAPH_STRING)){
              graph = TermMapLoader.defaultGraphTermMap();
            }
            triplesMapGraphMaps.add(graph);
          }
          
          // resolve referencing maps
          
         
          
  
          // collect all the graph information
          Set<TermMap> graphs = Sets.newHashSet();
          graphs.addAll(triplesMapGraphMaps);
          graphs.addAll(pographs);
  
          if (graphs.isEmpty()) {
            graphs.add(TermMapLoader.defaultGraphTermMap());
          }
          // here we got everything we need, so we construct the quadmaps
          
          
          for(TermMap graph : graphs){
            QuadMap quadMap = QuadMap.builder().
            triplesMapUri(triplesMapUri.getURI()).
            graph(graph).
            subject(subject).
            predicate(predicate).
            object(object).build();
            
            LogicalTable ltab = LogicalTable.builder()
              .tablename(tablename)
              .query(query)
              .version(version).build();
            quadMap.setLogicalTable(ltab);
            quadMaps.put(quadMap.getTriplesMapUri(), quadMap);
          }
  
        }
      }

    }
    resolveTermMapReferencing(quadMaps);
    
    
    return quadMaps;

  }
  
//  /**
//   * ensure that every termmap knows its quad map
//   * 
//   * @param quadMaps
//   */
//  private static void setQuadMap(Multimap<String,QuadMap> quadMaps){
//    for(QuadMap quadMap: quadMaps.values()){
//      for(TermMap tm: quadMap.getTermMaps()){
//        tm.setQuadMap(quadMap);
//      }
//    }
//    
//    
//  }
  
  /**
   * resolve parent triples maps later, as all other maps have to be loaded first.
   */
  private static void resolveTermMapReferencing(Multimap<String,QuadMap> quadMaps){
    
    for(QuadMap quadMap: quadMaps.values()){
      for(QuadPosition pos: QuadPosition.values()){
        
        TermMap tm = quadMap.get(pos);
        if (tm instanceof TermMapReferencing) {
          TermMapReferencing tmf = (TermMapReferencing) tm;
          
          QuadMap parent =  quadMaps.get(tmf.getParentMapUri()).iterator().next();          
          tmf.setParent(parent);
        }
      }
    }
  }

}
