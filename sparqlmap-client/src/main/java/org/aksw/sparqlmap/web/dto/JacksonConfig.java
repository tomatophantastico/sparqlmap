package org.aksw.sparqlmap.web.dto;

import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.module.SimpleModule;
@Component
public class JacksonConfig extends SimpleModule {
  
  @Override
  public void setupModule(SetupContext context) {
   context.setMixInAnnotations(LogicalSchema.class, SchemaMixin.class);
   context.setMixInAnnotations(LogicalTable.class, TableMixin.class);
   context.setMixInAnnotations(LogicalColumn.class, ColumnMixin.class);
  }


}
