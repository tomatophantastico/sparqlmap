package org.aksw.sparqlmap.core.translate.metamodel;

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
  
  private MetaModelRowBinder rowBinder;
  
  private MetaModelSchemaHelper schemaHelper;

  public MetaModelContext(DataContext dataContext, ContextConfiguration conConf) {
    super();
    this.dataContext = dataContext;
    this.conConf = conConf;
    rowBinder = new MetaModelRowBinder(conConf.getBaseUri());
    schemaHelper = new MetaModelSchemaHelper(dataContext);
  }

  public DataContext getDataContext() {
    return dataContext;
  }

  public ContextConfiguration getConConf() {
    return conConf;
  }

  public MetaModelRowBinder getRowBinder() {
    return rowBinder;
  }

  public MetaModelSchemaHelper getSchemaHelper() {
    return schemaHelper;
  }
  
  
  
  
  
  

}
