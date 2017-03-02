package org.aksw.sparqlmap.backend.metamodel.mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalRelation;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SchemaTranslator {
  
  /*
  private static Set<ColumnType> intTypes = Sets.newHashSet(ColumnType.BIGINT,ColumnType.INTEGER,ColumnType.TINYINT);
  private static Set<ColumnType> doubleTypes = Sets.newHashSet(ColumnType.DECIMAL,ColumnType.DOUBLE,ColumnType.FLOAT,ColumnType.NUMBER,ColumnType.NUMERIC,ColumnType.REAL);
  private static Set<ColumnType> boolTypes = Sets.newHashSet(ColumnType.BIT,ColumnType.BOOLEAN);
  private static Set<ColumnType> binaryTypes = Sets.newHashSet(ColumnType.BINARY, ColumnType.VARBINARY,ColumnType.LONGVARBINARY,ColumnType.BLOB);
  private static Set<ColumnType> dateTypes = Sets.newHashSet(ColumnType.DATE,ColumnType.TIME,ColumnType.TIMESTAMP);
  */
  
  
  
  public static LogicalSchema translate(Schema schema){
    
    LogicalSchema lschema = LogicalSchema.builder(schema.getName()).build();

    Map<String,LogicalTable> tables =   Arrays.stream(schema.getTables()).map(table -> {
      LogicalTable ltab = LogicalTable.builder(lschema).tablename(table.getName()).build();
      List<LogicalColumn> lcols = Arrays.stream(table.getColumns()).map(col -> {
        
        LogicalColumn lcol = LogicalColumn.builder(ltab).xsdDataType(getDataType(col.getType()))
          .name(col.getName())
          .build();

        return lcol;
      }).collect(Collectors.toList());
      ltab.setColumns(lcols);
      
      
      //translate primary keys

      ltab.setPrimaryKeys(
          Arrays.stream(
              table.getPrimaryKeys()).map(
                  primCol -> ltab.getCol(primCol.getName()).get()).collect(Collectors.toList()));
      
      
      return ltab;
    }).collect(Collectors.toMap(LogicalTable::getTablename, ltab->ltab));
    
    
    
    Collection<LogicalRelation> relations = Arrays.stream(schema.getRelationships()).map(relationship -> {
      assert(relationship.getPrimaryColumns().length == relationship.getForeignColumns().length);
      Collection<LogicalColumn[]> joinOnConditions = Lists.newArrayList();
      LogicalTable primary = tables.get(relationship.getPrimaryTable().getName());
      LogicalTable foreign = tables.get(relationship.getForeignTable().getName());
      
      for(int i = 0;i<relationship.getPrimaryColumns().length;i++){
        String priColName = relationship.getPrimaryColumns()[i].getName();
        String forColName = relationship.getForeignColumns()[i].getName();
        LogicalColumn[] cols = {primary.getCol(priColName).get(),foreign.getCol(forColName).get()};
        joinOnConditions.add(cols);
      }
      return LogicalRelation.builder().ons(joinOnConditions).primary(primary).foreign(foreign).build();
    }).collect(Collectors.toList());
    
    
    
    return LogicalSchema.builder(schema.getName()).relations(relations).tables(tables.values()).build();
    
    
    
  }
  
  
  public static Optional<XSDDatatype> getXSDDataType(ColumnType ct){
    XSDDatatype dt = null;
   if(ct==null) {
     // in cast ct is null
   }else if(ct.isBinary()){
     dt = XSDDatatype.XSDbase64Binary;
   }else if(ct.isBoolean()){
     dt = XSDDatatype.XSDboolean;
   }else if(ct.isNumber()){
     if(ct.getJavaEquivalentClass().equals(Double.class)){
       dt = XSDDatatype.XSDdouble;
     } if (ct.getJavaEquivalentClass().equals(UUID.class) ) {
       // do nothing
     }else{
       dt = XSDDatatype.XSDinteger;
     }
   } else if (ct.getName().equals("DATE")){
     dt = XSDDatatype.XSDdate;
   }else if(ct.getName().equals("TIME")){
     dt = XSDDatatype.XSDtime;
   }else if(ct.getName().equals("TIMESTAMP")){
     dt = XSDDatatype.XSDdateTime;
   }
   return Optional.ofNullable(dt);
  }
  
  
  public static Optional<String> getDataType(ColumnType ct){
    return getXSDDataType(ct).map(dt -> dt.getURI());
  }
  

  

}
