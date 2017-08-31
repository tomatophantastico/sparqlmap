package org.aksw.sparqlmap.backend.metamodel;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelContext;
import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelQueryDump;
import org.aksw.sparqlmap.backend.metamodel.translate.MetaModelQueryExecution;
import org.aksw.sparqlmap.core.Dumper;
import org.aksw.sparqlmap.core.QueryMetadata;
import org.aksw.sparqlmap.core.SparqlMapBackend;
import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.UpdateRequestBinding;
import org.aksw.sparqlmap.core.automapper.MappingGenerator;
import org.aksw.sparqlmap.core.automapper.MappingPrefixes;
import org.aksw.sparqlmap.core.errors.SparqlMapException;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RMLMapping;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.parser.QueryParser;
import org.apache.metamodel.schema.Table;
import org.jooq.lambda.Unchecked;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetaModelBackend implements SparqlMapBackend {
	
	final DataContext dataContext;
	
	List<Closeable> closeOnEnds = Lists.newArrayList();
	
	

	@Override
	public String getName() {
		return "METAMODEL";
	}

	@Override
	public boolean queryEnabled() {
		return true;
	}

	@Override
	public QueryExecution executeQuery(TranslationContext context, QueryMetadata metadata) {
	  TranslationContextMetaModel mmContext = new TranslationContextMetaModel(context);
		return new MetaModelQueryExecution(mmContext, dataContext, true);
	}

	@Override
	public boolean updateEnabled() {
		return dataContext instanceof UpdateableDataContext;
	}

	@Override
	public UpdateProcessor executeUpdate(UpdateRequestBinding updateRequestBinding, QueryMetadata metadata) {
	  assert(dataContext instanceof UpdateableDataContext);
		return new MetamodelUpdateProcessor((UpdateableDataContext) dataContext, updateRequestBinding, metadata);
		
		
	}

	@Override
	public boolean generateSchemaEnabled() {
		return true;
	}

	@Override
	public Model generateDirectMapping(MappingPrefixes prefixes) {
	  
	  LogicalSchema schema = SchemaTranslator.translate(dataContext.getDefaultSchema());
	  
	  MappingGenerator mapgen = new MappingGenerator(prefixes);
	  return mapgen.generateMapping(schema);
	  

	}

	@Override
	public boolean validateSchemaEnabled() {
		return true;
	}

	@Override
	public List<String> validateSchema(R2RMLMapping mapping) {

	    List<String> warnings = Lists.newArrayList();
	    if(mapping==null){
	      throw new SparqlMapException("Mapping not set");
	    }
	    if(dataContext==null){
	      throw new SparqlMapException("Data source not set");
	    }
	    
	    // now we check if every mapping col exists
	    
	    for(QuadMap qm: mapping.getQuadMaps()){
	      LogicalTable lt =  qm.getLogicalTable();
	      Set<Table> tables = Sets.newHashSet();
	      if(lt.getTablename()!=null ){
	        
	        Table ltTab = dataContext.getTableByQualifiedLabel(lt.getTablename());
	        if(ltTab != null){
	          tables.add(ltTab);
	        }else {
	          warnings.add(String.format("Cannot link tablename \"%s\" to an actual table in mapping <%s>", lt.getTablename(), qm.getTriplesMapUri())  );
	        }
	          
	      }else{
	        try{
	        Query query = new QueryParser(dataContext,lt.getQuery()).parse();
	        query.getFromClause().getItems().iterator().forEachRemaining(fi -> tables.add( fi.getTable()));
	        }catch (Exception e) {
	          warnings.add("Cannot parse view query of triplesmap: " + qm.getTriplesMapUri() );
	        }
	      }

	      for(QuadPosition pos: QuadPosition.values()){
	        TermMap tm =  qm.get(pos);
	        tm.getColumnNames().stream().filter( 
	            col-> tables.stream().noneMatch(
	                tab-> null != tab.getColumnByName(col)))
	        .forEach(col->
	          warnings.add(String.format("Cannot bind col \"%s\" in mapping %s",col, qm.getTriplesMapUri())));

	      }
	    }
	    return warnings;
		  
	}

	@Override
	public boolean dumpEnabled() {
		return true;
	}

	@Override
	public Dumper dump(Collection<QuadMap> quadMaps) {
		return new DumperMetaModel(new MetaModelContext(dataContext), quadMaps);
	}

	@Override
	public void close() {
	  closeOnEnds.forEach(Unchecked.consumer(Closeable::close));
	}
	
	public MetaModelBackend closeOnShutdown(Closeable closeable) {
	  this.closeOnEnds.add(closeable);
	  return this;
	}

  @Override
  public LogicalSchema getDefaultSchema() {
    return SchemaTranslator.translate(this.dataContext.getDefaultSchema());
  }

}
