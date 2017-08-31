package org.aksw.sparqlmap.backend.metamodel.translate;

import org.apache.metamodel.DataContext;

import lombok.Data;

/**
 * Contains classes needed for query translation which are tied to a SparqlMap instance with a MetaModel backend.
 * 
 * @author joerg
 *
 */
@Data
public class MetaModelContext {
  
  private DataContext dataContext;
  

  private boolean rowwiseBlanks = true;
  
  
  private MetaModelSchemaHelper schemaHelper;

  public MetaModelContext(DataContext dataContext) {
    super();
    this.dataContext = dataContext;
    schemaHelper = new MetaModelSchemaHelper(dataContext);
  }


}
