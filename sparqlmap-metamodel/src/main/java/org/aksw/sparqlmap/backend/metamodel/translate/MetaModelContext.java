package org.aksw.sparqlmap.backend.metamodel.translate;

import org.aksw.sparqlmap.core.ContextConfiguration;
import org.apache.metamodel.DataContext;

/**
 * Contains classes needed for query translation which are tied to a SparqlMap instance with a MetaModel backend.
 * 
 * @author joerg
 *
 */

public class MetaModelContext {
  
  private DataContext dataContext;
  
  private ContextConfiguration conConf;
  
  
  private MetaModelSchemaHelper schemaHelper;

  public MetaModelContext(DataContext dataContext, ContextConfiguration conConf) {
    super();
    this.dataContext = dataContext;
    this.conConf = conConf;
    schemaHelper = new MetaModelSchemaHelper(dataContext);
  }

  public DataContext getDataContext() {
    return dataContext;
  }

  public ContextConfiguration getConConf() {
    return conConf;
  }


  public MetaModelSchemaHelper getSchemaHelper() {
    return schemaHelper;
  }
  
  
  
  
  
  

}
