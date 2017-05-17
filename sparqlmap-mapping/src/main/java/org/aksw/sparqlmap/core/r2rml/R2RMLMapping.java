package org.aksw.sparqlmap.core.r2rml;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.rdf.model.Model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;


/**
 * a represenation of a R2RML model
 * @author joerg
 *
 */
@Data
public class R2RMLMapping {
  
  // term maps indexed by the iris
  @Setter(value=AccessLevel.PRIVATE)
  private Multimap<String,QuadMap> quadMaps = HashMultimap.create();
  private Model r2rmlMapping;
  private String description;
  
  @Setter(value=AccessLevel.PRIVATE)
  transient private Map<QuadPosition,SetMultimap<TermMap, QuadMap>> indexed  = Maps.newHashMap();

  
  public void addQuadMap(QuadMap quadMap) {
    this.quadMaps.put(quadMap.getTriplesMapUri(), quadMap);
    update();
  }

  public void addQuadMaps(Multimap<String,QuadMap> quadmaps ) {
    this.quadMaps.putAll(quadmaps);
  }
  public void addQuadMaps(Collection<QuadMap> quadmaps ) {
    quadmaps.stream().forEach(qm -> this.quadMaps.put(qm.getTriplesMapUri(),qm));
  }
  
  
   public Collection<QuadMap> getQuadMaps(){
     return quadMaps.values();
   }
   
   
   
   
   public void update(){
     indexed.clear();
     Arrays.stream(QuadPosition.values()).forEach(pos -> indexed.put(pos, MultimapBuilder.hashKeys().hashSetValues().build()) );
     
     
     
     for(QuadMap qm: getQuadMaps()){
      for(QuadPosition qp: QuadPosition.values()){
        TermMap tm = qm.get(qp);
        SetMultimap<TermMap, QuadMap> index = indexed.get(qp);
        index.put(tm, qm);
      }
     }
   }
}

