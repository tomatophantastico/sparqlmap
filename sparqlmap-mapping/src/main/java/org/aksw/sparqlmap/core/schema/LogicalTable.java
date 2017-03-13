package org.aksw.sparqlmap.core.schema;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ext.com.google.common.collect.Lists;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

@Data
@EqualsAndHashCode(exclude={"columns","primaryKeys","fkRelations","pRelations"})
@Builder(builderMethodName = "ltabbuilder")
public class LogicalTable {
  
    private LogicalSchema schema;
    
    @Singular
    private List<LogicalColumn> columns;
    
  
    private List<LogicalColumn> primaryKeys;
    
    /*
     * the relations, in which this table is the foreign table 
     */
    private List<LogicalRelation> fkRelations = Lists.newArrayList();
    
    /*
     * the relations, in which this table is the primary table
     */
    private List<LogicalRelation> pRelations = Lists.newArrayList();
    
    private String tablename;
        
    private String version;
    
    private String query;
    
    
    
    
    
    
    
    public static LogicalTableBuilder builder(LogicalSchema lschema){
      return ltabbuilder().schema(lschema);
    }
    public static LogicalTableBuilder builder(){
      return ltabbuilder().schema(LogicalSchema.DEFAULTSCHEMA);
    }
    
    
    public final static LogicalTable NULLTABLE =  builder(LogicalSchema.NULLSCHEMA).build();
    
    public Optional<LogicalColumn> getCol(String name){
      return columns.stream().filter(col->name.equals(col.getName())).findFirst();
    }
    
    
    public String getName(){
      String name = tablename;
      if(query!=null){
        name = "query-" +Hashing.farmHashFingerprint64().hashString(query, Charsets.UTF_8) ;
      }
      
      return name;
      
    }
    
    public String getPath(){
      return String.format("%s/%s", schema.getPath(),getName() );
    }
    
    
    
    public static class LogicalTableBuilder{
      private List<LogicalRelation> fkRelations = Lists.newArrayList();
      private List<LogicalRelation> pRelations = Lists.newArrayList();
      
    }
    
    
    


  
}
