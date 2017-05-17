package org.aksw.sparqlmap.core;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.R2RMLHelper;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class PersonDogMapping {
  
  public static String prefix = "http://example.org/";
  // a hypothecial table (id integer, dogid integer, )
  public static LogicalTable persontable = LogicalTable.builder().tablename("person").build();


  

  public static TermMap constantPerson = TermMapConstant.builder()
      .constantIRI(prefix + "Person/1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public static TermMap columnPerson = TermMapColumn.builder()
      .column(LogicalColumn.builder(persontable).name( "person").build())
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public static TermMap templatesPerson = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person/{id}",persontable))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
 
  
  public static TermMap templatePersonsDog = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person/{id}/Dog/{dogid}",persontable))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();

  public static TermMap constantBlank = TermMapConstant.builder()
      .constantIRI(prefix + "Other/1")
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();

  public static TermMap columnBlank = TermMapColumn.builder()
      .column(LogicalColumn.builder(persontable).name("other").build())
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();
  
  public static TermMap templateBlank = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Other/{id}",persontable))
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();

  public static TermMap constantLiteral = TermMapConstant.builder()
      .constantIRI("foo")
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public static TermMap constantLiteralEn = TermMapConstant.builder()
      .constantIRI("foo")
      .lang("en")
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public static TermMap columnLiteral = TermMapColumn.builder()
      .column(LogicalColumn.builder(persontable).name("other").build())
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public static TermMap templateLiteral = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate("My id is: {id}",persontable))
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public static TermMap templatesPerson_no_separator = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person{id}",persontable))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public static TermMap templatePersonsDog_no_separator = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person{id}Dog{dogid}",persontable))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public static TermMap constantPerson_no_separator = TermMapConstant.builder()
      .constantIRI(prefix + "Person1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  public static TermMap constantPersonsDog_no_separator = TermMapConstant.builder()
      .constantIRI(prefix + "Person1Dog1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  

  public static Node var = NodeFactory.createVariable("x");
  public static Node personIri = NodeFactory.createURI(prefix + "Person/1");
  public static Node person2Iri = NodeFactory.createURI(prefix + "Person/2");

  public static Node literal = NodeFactory.createLiteral("foo");
  public static Node literalTyped = NodeFactory.createLiteral("foo", XSDDatatype.XSDstring);

  public static Node literalLangEn = NodeFactory.createLiteral("foo", "en");
  public static Node literalLangEnUs = NodeFactory.createLiteral("foo", "en-US");
  public static Node literalLangDe = NodeFactory.createLiteral("foo", "de");

  public static Node blank = NodeFactory.createBlankNode();

//@formatter:on

}
