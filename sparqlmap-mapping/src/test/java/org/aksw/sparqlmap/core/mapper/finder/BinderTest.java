package org.aksw.sparqlmap.core.mapper.finder;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.normalizer.QueryNormalizer;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.QuadMap.LogicalTable;
import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;



public class BinderTest {
  
  String prefix = "http://example.org/";
  
  private static AlgebraGenerator gen = new AlgebraGenerator();
  
  private static Query querypersonName = QueryFactory.create("PREFIX : <http://example.org/> \n SELECT * {?s :name ?name} ");
  private static Query querypersonIdName = QueryFactory.create("PREFIX : <http://example.org/> \n SELECT * {?s :name ?name.?s :id ?id} ");

  private static List<QuadMap> personMap;
      
  {
    TermMap personSubject = TermMapTemplate.builder()
        .template(
            Lists.newArrayList(TermMapTemplateTuple.builder().prefix(prefix + "person/").column("id").build()))
        .termTypeIRI(R2RML.IRI_STRING) 
        .build();
    TermMap hasName = TermMapConstant.builder().constantIRI(prefix+"name").termTypeIRI(R2RML.IRI_STRING).build();
    TermMap name = TermMapColumn.builder().column("name").termTypeIRI(R2RML.LITERAL_STRING) .build();
    
    TermMap hasId = TermMapConstant.builder().constantIRI(prefix + "id").termTypeIRI(R2RML.IRI_STRING).build();
    TermMap id = TermMapColumn.builder().column("id").termTypeIRI(R2RML.LITERAL_STRING).build() ;
    TermMap graph = TermMapConstant.builder().constantIRI(Quad.defaultGraphNodeGenerated.getURI()).termTypeIRI(R2RML.IRI_STRING).build();
    
    LogicalTable persontable = LogicalTable.builder().tablename("person").build();
    
    personMap = Lists.newArrayList(
        QuadMap.builder().logicalTable(persontable).subject(personSubject).predicate(hasName).object(name).graph(graph).build(),
        QuadMap.builder().logicalTable(persontable).subject(personSubject).predicate(hasId).object(id).graph(graph).build()
        );
    
    
  }
  
  
 
  
  
  
  
  @Test
  public void test() {
    
    R2RMLMapping r2rmapping = new R2RMLMapping(HashMultimap.create(),null,null);
    r2rmapping.addQuadMaps(personMap);
    
    
    TranslationContext tc = new TranslationContext();
    tc.setQuery(querypersonName);
    QueryNormalizer.normalize(tc);
    
    tc.setQueryInformation(FilterFinder.getQueryInformation(tc.getBeautifiedQuery()));

    
    Binder binder = new Binder(r2rmapping);
    MappingBinding binding = binder.bind(tc);
    
    binding.getBindingMap().forEach((quad,quadmap)->assertNotNullQuadMap(quad, quadmap));
  }
  @Test
  public void testJoin(){
    Multimap<Quad,QuadMap> binding = executePersonTest(querypersonIdName);
    binding.forEach((quad,quadmap)->assertNotNullQuadMap(quad, quadmap));
    
  }
  
  
  
  
  
  
  
  
  
  private Multimap<Quad,QuadMap> executePersonTest(Query query){
    
    R2RMLMapping r2rmapping = new R2RMLMapping(HashMultimap.create(),null,null);
    r2rmapping.addQuadMaps(personMap);
    
    
    TranslationContext tc = new TranslationContext();
    tc.setQuery(query);
    QueryNormalizer.normalize(tc);
    
    tc.setQueryInformation(FilterFinder.getQueryInformation(tc.getBeautifiedQuery()));

    
    Binder binder = new Binder(r2rmapping);
    MappingBinding binding = binder.bind(tc);
    
    return  binding.getBindingMap();
   
  }
  private void assertNotNullQuadMap(Quad quad, QuadMap quadmap){
    Assert.assertTrue(String.format("Triple: %s bound to null",quad),!quadmap.equals(QuadMap.NULLQUADMAP));
  }
  

}
