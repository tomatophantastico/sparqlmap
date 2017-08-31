package org.aksw.sparqlmap.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import org.aksw.sparqlmap.backend.metamodel.MetaModelBackend;
import org.aksw.sparqlmap.core.automapper.MappingPrefixes;
import org.aksw.sparqlmap.core.errors.ImplementationException;
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
import com.google.common.collect.Lists;

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
  

  public SparqlMapMappingBuilder connectTo(List<SparqlMapBackend> backends){
    sparqlMap.setBackends(backends);
    
    return new SparqlMapMappingBuilder();
    
  }
  public SparqlMapMappingBuilder connectTo(SparqlMapBackend backend){
    sparqlMap.setBackends(Lists.newArrayList(backend));
    
    return new SparqlMapMappingBuilder();
    
  }
  
  public SparqlMapMappingBuilder connectTo(DataContext dcon) {
    sparqlMap.setBackends(Lists.newArrayList(new MetaModelBackend(dcon)));
    return new SparqlMapMappingBuilder();

  }

 

  
  public class SparqlMapMappingBuilder{

    public SparqlMapMappingBuilder mappedByDefaultMapping(){
      
      return mappedByDefaultMapping(new MappingPrefixes(baseIri));
      
    }
   
    
    public SparqlMapMappingBuilder mappedByDefaultMapping(MappingPrefixes prefixes){

      Optional<SparqlMapBackend> mappingBackend =  sparqlMap.getBackends().stream().filter(backend -> backend.generateSchemaEnabled()).findFirst();
      if(mappingBackend.isPresent()){
        Model mapping = mappingBackend.get().generateDirectMapping(prefixes);
        sparqlMap.setMapping(loadMapping(mapping));
      }else{
        throw new ImplementationException("No mapping generating backend present");
      }
      


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
      

      
      List<String> warnings = sparqlMap.validateMapping();
      
      if(!warnings.isEmpty()){
       log.warn((Joiner.on(System.lineSeparator()).join(warnings)));
      }
      
      return sparqlMap;
    }

    
    private R2RMLMapping loadMapping(Model model){
      Model r2rmlspec = ModelFactory.createDefaultModel();
      Model smapspec = ModelFactory.createDefaultModel();

      FileManager.get().readModel(r2rmlspec, "vocabularies/r2rml.ttl");
      FileManager.get().readModel(r2rmlspec, "vocabularies/smap.ttl");
      
      R2RMLMapping r2rmlMappig =  R2RMLModelLoader.loadModel(model, r2rmlspec, smapspec, baseIri);
      
      return r2rmlMappig;
    }
  }

}
