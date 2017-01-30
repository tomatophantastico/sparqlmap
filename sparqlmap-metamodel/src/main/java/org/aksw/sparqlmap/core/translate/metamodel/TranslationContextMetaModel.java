package org.aksw.sparqlmap.core.translate.metamodel;

import java.util.Map;
import java.util.Set;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class TranslationContextMetaModel {
  
  
  
  
  
  TranslationContext tcontext;
  
  public TranslationContextMetaModel(TranslationContext tcontext) {
    super();
    this.tcontext = tcontext;
    flatBindings = tcontext.getQueryBinding().asMaps();
    
  }

  Set<Map<Quad,QuadMap>> flatBindings;
  
  DatasetGraph materialization;

  
  
  
}
