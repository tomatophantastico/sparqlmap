package org.aksw.sparqlmap.querytests;

import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.SparqlMapBuilder;
import org.aksw.sparqlmap.core.automapper.MappingGenerator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.eobjects.metamodel.access.AccessDataContext;
import static org.junit.Assert.*;
import org.junit.Test;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.internal.react.stream.CloseableIterator;
import com.datastax.driver.core.Row;


public class AccessQueryTest {
  
  private static final String PFARRERBUCHLOCATION = "./src/test/resources/pfarrerbuch/KPS-BIO-A_Probe.accdb";
  
  @Test
  public void testPfarrerbuchSchemaGen(){
    Model defaultMapping = new MappingGenerator("http://example.com/pfarrerbuch/").generateMapping(SchemaTranslator.translate(new AccessDataContext(PFARRERBUCHLOCATION).getDefaultSchema()));
    
    defaultMapping.write(System.out,"TTL");
    
    assertTrue(defaultMapping.size()>1);
    
  }
  
  @Test
  public void printPfarrerbuch(){
    DataContext dc = new AccessDataContext(PFARRERBUCHLOCATION);
    
    for(Table tab: dc.getDefaultSchema().getTables()){
      System.out.println(tab.getName());
      DataSet ds = dc.query().from(tab).selectAll().execute();
      ds.forEach(row -> {
       
      for(Column col:tab.getColumns()){
        System.out.print( col.getName() + " :" + row.getValue(col)  + ", ");
      }
      System.out.println("--------------");
      });
      
    }
    
    
  }
  

  
  
  
  
  @Test
  public void testPfarrerbuch(){
    SparqlMap sm = SparqlMapBuilder.newSparqlMap(null).connectTo(new AccessDataContext(PFARRERBUCHLOCATION)).mappedByDefaultMapping().create();
    
    DatasetGraph dsg = sm.getDumpExecution().dumpDatasetGraph();
    
    assertTrue(dsg.getDefaultGraph().size()>1);
   
    
    
  }
  

}
