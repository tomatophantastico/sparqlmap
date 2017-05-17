package org.aksw.sparqlmap.core.mapper.compatibility;

import static org.aksw.sparqlmap.core.PersonDogMapping.blank;
import static org.aksw.sparqlmap.core.PersonDogMapping.columnBlank;
import static org.aksw.sparqlmap.core.PersonDogMapping.columnPerson;
import static org.aksw.sparqlmap.core.PersonDogMapping.constantBlank;
import static org.aksw.sparqlmap.core.PersonDogMapping.constantLiteral;
import static org.aksw.sparqlmap.core.PersonDogMapping.constantLiteralEn;
import static org.aksw.sparqlmap.core.PersonDogMapping.constantPerson;
import static org.aksw.sparqlmap.core.PersonDogMapping.constantPersonsDog_no_separator;
import static org.aksw.sparqlmap.core.PersonDogMapping.literal;
import static org.aksw.sparqlmap.core.PersonDogMapping.literalLangDe;
import static org.aksw.sparqlmap.core.PersonDogMapping.literalLangEn;
import static org.aksw.sparqlmap.core.PersonDogMapping.person2Iri;
import static org.aksw.sparqlmap.core.PersonDogMapping.personIri;
import static org.aksw.sparqlmap.core.PersonDogMapping.templateBlank;
import static org.aksw.sparqlmap.core.PersonDogMapping.templatePersonsDog;
import static org.aksw.sparqlmap.core.PersonDogMapping.templatePersonsDog_no_separator;
import static org.aksw.sparqlmap.core.PersonDogMapping.templatesPerson;
import static org.aksw.sparqlmap.core.PersonDogMapping.var;
import static org.aksw.sparqlmap.core.mapper.compatibility.CompatibilityChecker.isCompatible;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompatibilityCheckerTest {
//@formatter:off
 
  @Test
  public void testConstantVsDynmicIRIs() {

    // check vs constant templates
    assertTrue("constant/iri-column", isCompatible(constantPerson, columnPerson));
    assertTrue("constant/iri-template", isCompatible(constantPerson, templatesPerson));
    assertTrue("contstant/iri-non-matching template", !isCompatible(constantPerson, templatePersonsDog));

  }

  @Test
  public void testTermMapsVsVariables() {

    // check variables

    assertTrue("var/iri-column", isCompatible(columnPerson, var));
    assertTrue("var/iri-template", isCompatible(templatesPerson, var));
    assertTrue("var/iri-non-matching template", isCompatible(templatePersonsDog, var));

  }

  @Test
  public void testTermMapsVsIRINodes() {
    // check against resource nodes
    assertTrue("person1/iri-constant", isCompatible(constantPerson, personIri));
    assertTrue("person1/iri-column", isCompatible(columnPerson, personIri));
    assertTrue("person1/iri-template", isCompatible(templatesPerson, personIri));
    assertTrue("person1/iri-non-matching template", !isCompatible(templatePersonsDog, personIri));

    assertFalse("person2/iri-constant", isCompatible(constantPerson, person2Iri));
    assertTrue("person2/iri-column", isCompatible(columnPerson, person2Iri));
    assertTrue("person2/iri-template", isCompatible(templatesPerson, person2Iri));
    assertFalse("person2/iri-non-matching template", isCompatible(templatePersonsDog, person2Iri));

  }

  @Test
  public void testIriTermMapVsLiteralNodes() {
    assertFalse(isCompatible(columnPerson, literalLangDe));
    assertFalse(isCompatible(columnPerson, literalLangEn));
    assertFalse(isCompatible(columnPerson, literal));

    assertFalse(isCompatible(templatesPerson, literal));
    assertFalse(isCompatible(columnPerson, literal));
    assertFalse(isCompatible(constantPerson, literal));

  }

  @Test
  public void testTermMapVsBlankNode() {
    assertFalse(isCompatible(templatesPerson, blank));
    assertFalse(isCompatible(columnPerson, blank));
    assertFalse(isCompatible(constantPerson, blank));
  }

  @Test
  public void testTermMapIRIVsTermMapLiteral() {
    assertFalse(isCompatible(templatesPerson, constantLiteral));
    assertFalse(isCompatible(templatesPerson, constantLiteralEn));
    assertFalse(isCompatible(constantPerson, constantLiteralEn));
    assertFalse(isCompatible(columnPerson, constantLiteral));

  }

  @Test
  public void testTermMapIRIVsTermMapBlank() {
    // check against other person2
    assertFalse(isCompatible(templatesPerson, templateBlank));
    assertFalse(isCompatible(columnPerson, columnBlank));
    assertFalse(isCompatible(constantPerson, constantBlank));
  }
  
  
  @Test
  public void testTemplateVsTemplate(){
    
    assertTrue(isCompatible(templatesPerson, templatesPerson));
  }
  

  @Test
  public void testTermMapTemplateCompatbility(){
    
    assertTrue(isCompatible(templatePersonsDog_no_separator, constantPersonsDog_no_separator));
    
    
    
  }
  
  
}
