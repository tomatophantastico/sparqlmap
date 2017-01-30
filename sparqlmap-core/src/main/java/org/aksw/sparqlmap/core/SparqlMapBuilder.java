package org.aksw.sparqlmap.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.aksw.sparqlmap.core.automapper.MappingGenerator;
import org.aksw.sparqlmap.core.errors.SystemInitializationError;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.R2RMLModelLoader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.metamodel.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Create SparqlMap contexts with this Factory!
 * 
 * 
 * @author joerg
 *
 */
public class SparqlMapBuilder {
  
  private String baseIri;
  
  private SparqlMap sparqlMap;
  
  
  private static final Logger log = LoggerFactory.getLogger(SparqlMapBuilder.class);

  
  
  public static SparqlMapBuilder newSparqlMap(String baseIRI){
    if(baseIRI==null){
      try {
        baseIRI = "http://" + InetAddress.getLocalHost().getHostName() + "/baseiri/";
      } catch (UnknownHostException e) {
        baseIRI = "http://localhost/baseiri/";
      }
    }
    
    return new SparqlMapBuilder(baseIRI);

  }
  
  
  public SparqlMapBuilder(String baseIri) {
    sparqlMap = new SparqlMap();
    this.baseIri = baseIri;
  }
  

  public SparqlMapMappingBuilder connectTo(DataContext dcon){
    sparqlMap.setDataContext(dcon);
    
    return new SparqlMapMappingBuilder();
    
  }
  
  
 
  
  

  
  public class SparqlMapMappingBuilder{
    

    
    public SparqlMapMappingBuilder mappedByDefaultMapping(){
      
      return mappedByDefaultMapping(baseIri);
      
    }
   
    
    public SparqlMapMappingBuilder mappedByDefaultMapping(String prefix){
      MappingGenerator gen = new MappingGenerator(prefix);
      Model mapping = gen.generateMapping(sparqlMap.getDataContext().getDefaultSchema());
      sparqlMap.setMapping(loadMapping(mapping));

      return this;
    }
    
    public SparqlMapMappingBuilder mappedByDefaultMapping(String prefix, String mappingPrefix,
      String instancePrefix, String vocabularyPrefix, String primaryKeySeparator){
    
      MappingGenerator gen = new MappingGenerator(prefix,mappingPrefix,instancePrefix,vocabularyPrefix,primaryKeySeparator,null);
      Model mapping = gen.generateMapping(sparqlMap.getDataContext().getDefaultSchema());

      sparqlMap.setMapping(loadMapping(mapping));
      return this;
    }
    
    
    
    
    
    
    public SparqlMapMappingBuilder mappedBy(String location){
      Model r2rmlModel = RDFDataMgr.loadModel(location);
          
      sparqlMap.setMapping(loadMapping(r2rmlModel));
      
      return this;
    }
    
    public SparqlMapMappingBuilder mappedBy(Model model){
      
      sparqlMap.setMapping(loadMapping(model));

      return this;
    }
    
    public SparqlMap create(){
      
      //validate, that both the mapping is set and a db connection is present.
      
      
      if(sparqlMap.getMapping()==null){
        throw new SystemInitializationError("No mapping loaded");
      }
      
    
      if(sparqlMap.getContextConf()==null){
        sparqlMap.setContextConf(new ContextConfiguration());
      }
      
      
      List<String> warnings = sparqlMap.validateMapping();
      
      if(!warnings.isEmpty()){
       log.warn((Joiner.on(System.lineSeparator()).join(warnings)));
      }
      
      return sparqlMap;
    }
    
    
    
    
    
    private R2RMLMapping loadMapping(Model model){
      Model r2rmlspec = ModelFactory.createDefaultModel();
      
      FileManager.get().readModel(r2rmlspec, "vocabularies/r2rml.ttl");
      
      R2RMLMapping r2rmlMappig =  R2RMLModelLoader.loadModel(model, r2rmlspec,baseIri);
      
      return r2rmlMappig;
      
      
    }
   
    
  }
  
  
  

}
