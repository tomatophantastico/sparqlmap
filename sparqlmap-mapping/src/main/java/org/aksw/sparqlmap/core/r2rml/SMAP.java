package org.aksw.sparqlmap.core.r2rml;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class SMAP {

  

    public static final  String SM_STRING = "http://aksw.org/Projects/SparqlMap/vocab#";

    public static final String NULLQUADMAPSTRING = SM_STRING + "NullQuadMap";

    public static final String NULLRESOURCE_STRING = SM_STRING + "Null";
    
    public static final String REQUIRED_PATTERN_STRING = SM_STRING + "requiredPattern";
    public static final Property REQUIRED_PATTERN = ResourceFactory.createProperty(REQUIRED_PATTERN_STRING);
    public static final String TRANSFORM_PATTERN_STRING = SM_STRING + "transformPattern";
    public static final Property TRANSFORM_PATTERN = ResourceFactory.createProperty(TRANSFORM_PATTERN_STRING);



}
