package org.aksw.sparqlmap.core.r2rml;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * a represenation of a R2RML model
 * @author joerg
 *
 */
@Data
@AllArgsConstructor
public class R2RMLMapping {
  
  // term maps indexed by the iris
  private Multimap<String,QuadMap> quadMaps;
  private Model r2rmlMapping;
  private String description;
  

  
  public void addQuadMap(QuadMap quadMap) {
    this.quadMaps.put(quadMap.getTriplesMapUri(), quadMap);
  }

  public void addQuadMaps(Collection<QuadMap> load) {
   for(QuadMap qm: load){
     addQuadMap(qm);
   }
    
  }

}
