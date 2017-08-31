package org.aksw.sparqlmap.core.mapper.finder;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.normalizer.QueryNormalizer;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalTable;
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
  private static Query getFathers =  QueryFactory.create("PREFIX : <http://example.org/> \n SELECT ?name ?fathername {?s ?p ?father. ?s :name ?name.  ?father :name ?fathername} ");
  public static List<QuadMap> personMap;
      
  {
    LogicalTable persontable = LogicalTable.builder().tablename("person").build();

    TermMap personSubject = TermMapTemplate.builder()
        .template(
            Lists.newArrayList(TermMapTemplateTuple.builder().prefix(prefix + "person/").column(LogicalColumn.builder(persontable).name("id").build()).build()))
        .termTypeIRI(R2RML.IRI_STRING) 
        .build();
    TermMap hasName = TermMapConstant.builder().constantIRI(prefix+"name").termTypeIRI(R2RML.IRI_STRING).build();
    TermMap name = TermMapColumn.builder().column(LogicalColumn.builder(persontable).name("name").build()).termTypeIRI(R2RML.LITERAL_STRING) .build();
    
    TermMap hasId = TermMapConstant.builder().constantIRI(prefix + "id").termTypeIRI(R2RML.IRI_STRING).build();
    TermMap id = TermMapColumn.builder().column(LogicalColumn.builder(persontable).name("id").build()).termTypeIRI(R2RML.LITERAL_STRING).build() ;
    TermMap graph = TermMapConstant.builder().constantIRI(Quad.defaultGraphNodeGenerated.getURI()).termTypeIRI(R2RML.IRI_STRING).build();
    
    TermMap hasFather = TermMapConstant.builder().constantIRI(prefix + "hasFather").termTypeIRI(R2RML.IRI_STRING).build();
    TermMap father = TermMapTemplate.builder()
        .template(
            Lists.newArrayList(TermMapTemplateTuple.builder().prefix(prefix + "person/").column(LogicalColumn.builder(persontable).name("father").build()).build()))
        .termTypeIRI(R2RML.IRI_STRING) 
        .build();
    
    personMap = Lists.newArrayList(
        QuadMap.builder().logicalTable(persontable).subject(personSubject).predicate(hasName).object(name).graph(graph).build(),
        QuadMap.builder().logicalTable(persontable).subject(personSubject).predicate(hasId).object(id).graph(graph).build(),
        QuadMap.builder().logicalTable(persontable).subject(personSubject).predicate(hasFather).object(father).graph(graph).build()
        );
    
    
    
    
  }
  
  
 
  
  
  
  
  @Test
  public void test() {
    
    R2RMLMapping r2rmapping = new R2RMLMapping();
    r2rmapping.addQuadMaps(personMap);
    
    
    TranslationContext tc = new TranslationContext();
    tc.setQuery(querypersonName);
    

    tc.setBeautifiedQuery(new AlgebraGenerator().compile(tc.getQuery()));
    QueryNormalizer.normalize(tc);
    
    //tc.setQueryInformation(FilterFinder.getQueryInformation(tc.getBeautifiedQuery()));

    
    
    QueryBinding qbind = new StreamingBinder(r2rmapping).bind(tc);
    
    
    Assert.assertEquals(1,qbind.rows.size());
    
  }
  @Test
  public void testJoin(){
    QueryBinding binding = executePersonTest(querypersonIdName);
    assertEquals(2, binding.head.size());
    assertEquals(1,binding.rows.size());
    
    
  }
  
  
  @Test
  public void testSUbObjJoin(){
    QueryBinding binding  =executePersonTest(getFathers);
    
    assertEquals(3, binding.head.size());
    assertEquals(1,binding.rows.size());
    
  }
  
  
  
  
  
  
  
  private QueryBinding executePersonTest(Query query){
    
    R2RMLMapping r2rmapping = new R2RMLMapping();
    r2rmapping.addQuadMaps(personMap);
    
    
    TranslationContext tc = new TranslationContext();
    tc.setQuery(query);
    QueryNormalizer.normalize(tc);
    
    //tc.setQueryInformation(FilterFinder.getQueryInformation(tc.getBeautifiedQuery()));

    
   
    
    return new StreamingBinder(r2rmapping).bind(tc);
   
  }

 
}
