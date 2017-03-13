package org.aksw.sparqlmap.core.mapper.compatibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.R2RMLHelper;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class CompatibilityCheckerTest {
//@formatter:off
  String prefix = "http://example.org/";

  public TermMap constantPerson = TermMapConstant.builder()
      .constantIRI(prefix + "Person/1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public TermMap columnPerson = TermMapColumn.builder()
      .column("person")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public TermMap templatesPerson = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person/{id}"))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
 
  
  public TermMap templatePersonsDog = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person/{id}/Dog/{dogid}"))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();

  public TermMap constantBlank = TermMapConstant.builder()
      .constantIRI(prefix + "Other/1")
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();

  public TermMap columnBlank = TermMapColumn.builder()
      .column("other")
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();
  
  public TermMap templateBlank = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Other/{id}"))
      .termTypeIRI(R2RML.BLANKNODE_STRING)
      .build();

  public TermMap constantLiteral = TermMapConstant.builder()
      .constantIRI("foo")
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public TermMap constantLiteralEn = TermMapConstant.builder()
      .constantIRI("foo")
      .lang(Optional.of("en"))
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public TermMap columnLiteral = TermMapColumn.builder()
      .column("other")
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public TermMap templateLiteral = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate("My id is: {id}"))
      .termTypeIRI(R2RML.LITERAL_STRING)
      .build();
  
  public TermMap templatesPerson_no_separator = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person{id}"))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public TermMap templatePersonsDog_no_separator = TermMapTemplate.builder()
      .template(R2RMLHelper.splitTemplate(prefix + "Person{id}Dog{dogid}"))
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  
  public TermMap constantPerson_no_separator = TermMapConstant.builder()
      .constantIRI(prefix + "Person1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  public TermMap constantPersonsDog_no_separator = TermMapConstant.builder()
      .constantIRI(prefix + "Person1Dog1")
      .termTypeIRI(R2RML.IRI_STRING)
      .build();
  

  Node var = NodeFactory.createVariable("x");
  Node personIri = NodeFactory.createURI(prefix + "Person/1");
  Node person2Iri = NodeFactory.createURI(prefix + "Person/2");

  Node literal = NodeFactory.createLiteral("foo");
  Node literalTyped = NodeFactory.createLiteral("foo", XSDDatatype.XSDstring);

  Node literalLangEn = NodeFactory.createLiteral("foo", "en");
  Node literalLangEnUs = NodeFactory.createLiteral("foo", "en-US");
  Node literalLangDe = NodeFactory.createLiteral("foo", "de");

  Node blank = NodeFactory.createBlankNode();

  CompatibilityChecker cc = new CompatibilityChecker();
//@formatter:on
  @Test
  public void testConstantVsDynmicIRIs() {

    // check vs constant templates
    assertTrue("constant/iri-column", cc.isCompatible(constantPerson, columnPerson));
    assertTrue("constant/iri-template", cc.isCompatible(constantPerson, templatesPerson));
    assertTrue("contstant/iri-non-matching template", !cc.isCompatible(constantPerson, templatePersonsDog));

  }

  @Test
  public void testTermMapsVsVariables() {

    // check variables

    assertTrue("var/iri-column", cc.isCompatible(columnPerson, var));
    assertTrue("var/iri-template", cc.isCompatible(templatesPerson, var));
    assertTrue("var/iri-non-matching template", cc.isCompatible(templatePersonsDog, var));

  }

  @Test
  public void testTermMapsVsIRINodes() {
    // check against resource nodes
    assertTrue("person1/iri-constant", cc.isCompatible(constantPerson, personIri));
    assertTrue("person1/iri-column", cc.isCompatible(columnPerson, personIri));
    assertTrue("person1/iri-template", cc.isCompatible(templatesPerson, personIri));
    assertTrue("person1/iri-non-matching template", !cc.isCompatible(templatePersonsDog, personIri));

    assertFalse("person2/iri-constant", cc.isCompatible(constantPerson, person2Iri));
    assertTrue("person2/iri-column", cc.isCompatible(columnPerson, person2Iri));
    assertTrue("person2/iri-template", cc.isCompatible(templatesPerson, person2Iri));
    assertFalse("person2/iri-non-matching template", cc.isCompatible(templatePersonsDog, person2Iri));

  }

  @Test
  public void testIriTermMapVsLiteralNodes() {
    assertFalse(cc.isCompatible(columnPerson, literalLangDe));
    assertFalse(cc.isCompatible(columnPerson, literalLangEn));
    assertFalse(cc.isCompatible(columnPerson, literal));

    assertFalse(cc.isCompatible(templatesPerson, literal));
    assertFalse(cc.isCompatible(columnPerson, literal));
    assertFalse(cc.isCompatible(constantPerson, literal));

  }

  @Test
  public void testTermMapVsBlankNode() {
    assertFalse(cc.isCompatible(templatesPerson, blank));
    assertFalse(cc.isCompatible(columnPerson, blank));
    assertFalse(cc.isCompatible(constantPerson, blank));
  }

  @Test
  public void testTermMapIRIVsTermMapLiteral() {
    assertFalse(cc.isCompatible(templatesPerson, constantLiteral));
    assertFalse(cc.isCompatible(templatesPerson, constantLiteralEn));
    assertFalse(cc.isCompatible(constantPerson, constantLiteralEn));
    assertFalse(cc.isCompatible(columnPerson, constantLiteral));

  }

  @Test
  public void testTermMapIRIVsTermMapBlank() {
    // check against other person2
    assertFalse(cc.isCompatible(templatesPerson, templateBlank));
    assertFalse(cc.isCompatible(columnPerson, columnBlank));
    assertFalse(cc.isCompatible(constantPerson, constantBlank));
  }
  
  
  @Test
  public void testTemplateVsTemplate(){
    
    assertTrue(cc.isCompatible(templatesPerson, templatesPerson));
  }
  

  @Test
  public void testTermMapTemplateCompatbility(){
    
    assertTrue(cc.isCompatible(templatePersonsDog_no_separator, constantPersonsDog_no_separator));
    
    
    
  }
  
  
}
