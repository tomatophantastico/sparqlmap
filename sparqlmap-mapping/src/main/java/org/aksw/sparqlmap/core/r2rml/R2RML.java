package org.aksw.sparqlmap.core.r2rml;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
/**
 * This class encapsulates the R2RML vocabulary.
 * @author joerg
 *
 */
public abstract class R2RML {
  
 

  public static final  String R2RML_STRING = "http://www.w3.org/ns/r2rml#";

  public static final String BASETABLEORVIEW_STRING = R2RML_STRING + "BaseTableOrView";

  public static final String BLANKNODE_STRING = R2RML_STRING + "BlankNode";

  public static final String GRAPHMAP_STRING = R2RML_STRING + "GraphMap";

  public static final String IRI_STRING = R2RML_STRING + "IRI";

  public static final String JOIN_STRING = R2RML_STRING + "Join";

  public static final String LITERAL_STRING = R2RML_STRING + "Literal";

  public static final String LOGICALTABLE_STRING = R2RML_STRING + "LogicalTable";

  public static final String OBJECTMAP_STRING = R2RML_STRING + "ObjectMap";

  public static final String PREDICATEMAP_STRING = R2RML_STRING + "PredicateMap";

  public static final String PREDICATEOBJECTMAP_STRING = R2RML_STRING + "PredicateObjectMap";

  public static final String REFOBJECTMAP_STRING = R2RML_STRING + "RefObjectMap";

  public static final String R2RMLVIEW_STRING = R2RML_STRING + "R2RMLView";

  public static final String SUBJECTMAP_STRING = R2RML_STRING + "SubjectMap";

  public static final String TRIPLESMAP_STRING = R2RML_STRING + "TriplesMap";

  public static final String HASCHILD_STRING = R2RML_STRING + "child";

  public static final String HASCLASS_STRING = R2RML_STRING + "class";

  public static final String HASCOLUMN_STRING = R2RML_STRING + "column";

  public static final String HASCONSTANT_STRING = R2RML_STRING + "constant";

  public static final String HASDATATYPE_STRING = R2RML_STRING + "datatype";

  public static final String HASGRAPH_STRING = R2RML_STRING + "graph";

  public static final String HASGRAPHMAP_STRING = R2RML_STRING + "graphMap";

  public static final String HASINVERSEEXPRESSION_STRING = R2RML_STRING + "inverseExpression";

  public static final String HASJOINCONDITION_STRING = R2RML_STRING + "joinCondition";

  public static final String HASLANGUAGE_STRING = R2RML_STRING + "language";

  public static final String HASLOGICALTABLE_STRING = R2RML_STRING + "logicalTable";

  public static final String HASOBJECT_STRING = R2RML_STRING + "object";

  public static final String HASOBJECTMAP_STRING = R2RML_STRING + "objectMap";

  public static final String HASPARENT_STRING = R2RML_STRING + "parent";

  public static final String HASPARENTTRIPLESMAP_STRING = R2RML_STRING + "parentTriplesMap";

  public static final String HASPREDICATE_STRING = R2RML_STRING + "predicate";

  public static final String HASPREDICATEMAP_STRING = R2RML_STRING + "predicateMap";

  public static final String HASPREDICATEOBJECTMAP_STRING = R2RML_STRING + "predicateObjectMap";

  public static final String HASREFOBJECTMAP_STRING = R2RML_STRING + "refObjectMap";

  public static final String HASSQLQUERY_STRING = R2RML_STRING + "sqlQuery";

  public static final String HASSQLVERSION_STRING = R2RML_STRING + "sqlVersion";

  public static final String HASSUBJECT_STRING = R2RML_STRING + "subject";

  public static final String HASSUBJECTMAP_STRING = R2RML_STRING + "subjectMap";

  public static final String HASTABLENAME_STRING = R2RML_STRING + "tableName";

  public static final String HASTEMPLATE_STRING = R2RML_STRING + "template";

  public static final String HASTERMTYPE_STRING = R2RML_STRING + "termType";

  public static final String SQL2008_STRING = R2RML_STRING + "SQL2008";

  public static final String DEFAULTGRAPH_STRING = R2RML_STRING + "defaultGraph";

  public static final Resource BASETABLEORVIEW = createResource(BASETABLEORVIEW_STRING);

  public static final Resource BLANKNODE = createResource(BLANKNODE_STRING);

  public static final Resource GRAPHMAP = createResource(GRAPHMAP_STRING);

  public static final Resource IRI = createResource(IRI_STRING);

  public static final Resource JOIN = createResource(JOIN_STRING);

  public static final Resource LITERAL = createResource(LITERAL_STRING);

  public static final Resource LOGICALTABLE = createResource(LOGICALTABLE_STRING);

  public static final Resource OBJECTMAP = createResource(OBJECTMAP_STRING);

  public static final Resource PREDICATEMAP = createResource(PREDICATEMAP_STRING);

  public static final Resource PREDICATEOBJECTMAP = createResource(PREDICATEOBJECTMAP_STRING);

  public static final Resource REFOBJECTMAP = createResource(REFOBJECTMAP_STRING);

  public static final Resource R2RMLVIEW = createResource(R2RMLVIEW_STRING);

  public static final Resource SUBJECTMAP = createResource(SUBJECTMAP_STRING);

  public static final Resource TRIPLESMAP = createResource(TRIPLESMAP_STRING);

  public static final Property HASCHILD = createProprty(HASCHILD_STRING);

  public static final Property HASCLASS = createProprty(HASCLASS_STRING);

  public static final Property HASCOLUMN = createProprty(HASCOLUMN_STRING);

  public static final Property HASCONSTANT = createProprty(HASCONSTANT_STRING);

  public static final Property HASDATATYPE = createProprty(HASDATATYPE_STRING);

  public static final Property HASGRAPH = createProprty(HASGRAPH_STRING);

  public static final Property HASGRAPHMAP = createProprty(HASGRAPHMAP_STRING);

  public static final Property HASINVERSEEXPRESSION = createProprty(HASINVERSEEXPRESSION_STRING);

  public static final Property HASJOINCONDITION = createProprty(HASJOINCONDITION_STRING);

  public static final Property HASLANGUAGE = createProprty(HASLANGUAGE_STRING);

  public static final Property HASLOGICALTABLE = createProprty(HASLOGICALTABLE_STRING);

  public static final Property HASOBJECT = createProprty(HASOBJECT_STRING);

  public static final Property HASOBJECTMAP = createProprty(HASOBJECTMAP_STRING);

  public static final Property HASPARENT = createProprty(HASPARENT_STRING);

  public static final Property HASPARENTTRIPLESMAP = createProprty(HASPARENTTRIPLESMAP_STRING);

  public static final Property HASPREDICATE = createProprty(HASPREDICATE_STRING);

  public static final Property HASPREDICATEMAP = createProprty(HASPREDICATEMAP_STRING);

  public static final Property HASPREDICATEOBJECTMAP = createProprty(HASPREDICATEOBJECTMAP_STRING);

  public static final Property HASREFOBJECTMAP = createProprty(HASREFOBJECTMAP_STRING);

  public static final Property HASSQLQUERY = createProprty(HASSQLQUERY_STRING);

  public static final Property SQLVERSION = createProprty(HASSQLVERSION_STRING);

  public static final Property HASSUBJECT = createProprty(HASSUBJECT_STRING);

  public static final Property HASSUBJECTMAP = createProprty(HASSUBJECTMAP_STRING);

  public static final Property HASTABLENAME = createProprty(HASTABLENAME_STRING);

  public static final Property HASTEMPLATE = createProprty(HASTEMPLATE_STRING);

  public static final Property TERMTYPE = createProprty(HASTERMTYPE_STRING);

  public static final Resource SQL2008 = createResource(SQL2008_STRING);

  public static final Resource DEFAULTGRAPH = createResource(DEFAULTGRAPH_STRING);

  private static Resource createResource(String uri) {
    return ResourceFactory.createResource(uri);
  }

  private static Property createProprty(String uri) {
    return ResourceFactory.createProperty(uri);
  }
  

}
