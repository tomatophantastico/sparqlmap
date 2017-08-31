package org.aksw.sparqlmap.core;


import java.util.Collection;
import java.util.List;

import org.aksw.sparqlmap.core.automapper.MappingPrefixes;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * A SparqlMap Backend, capable of execution queries (eventually) updates on mapping bindings
 */
public interface SparqlMapBackend {


    /**
     * A unique name, identifying this Backend
     * @return
     */
    public String getName();



    public boolean queryEnabled();

    public QueryExecution executeQuery(TranslationContext context, QueryMetadata metadata);


    public boolean updateEnabled();

    public UpdateProcessor executeUpdate(UpdateRequestBinding mapping, QueryMetadata metadata);


    public boolean generateSchemaEnabled();

    public Model generateDirectMapping(MappingPrefixes prefixes);
    
    
    public boolean validateSchemaEnabled();
    
	public List<String> validateSchema(R2RMLMapping mapping);


    /**
     * does this backend implement specific (fast) methods for creating a query dump?
     * @return if this is supporteds
     */
    public boolean dumpEnabled();

    Dumper dump(Collection<QuadMap> quadMaps);
    
    
    public LogicalSchema getDefaultSchema();
    
    public void close();







}
