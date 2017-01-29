package org.aksw.sparqlmap.core.automapper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Creates a Direct Mapping for the given schema
 * 
 * @author joerg
 *
 */
public class MappingGenerator {
  
  private static Set<ColumnType> intTypes = Sets.newHashSet(ColumnType.BIGINT,ColumnType.INTEGER,ColumnType.TINYINT);
  private static Set<ColumnType> doubleTypes = Sets.newHashSet(ColumnType.DECIMAL,ColumnType.DOUBLE,ColumnType.FLOAT,ColumnType.NUMBER,ColumnType.NUMERIC,ColumnType.REAL);
  private static Set<ColumnType> boolTypes = Sets.newHashSet(ColumnType.BIT,ColumnType.BOOLEAN);
  private static Set<ColumnType> binaryTypes = Sets.newHashSet(ColumnType.BINARY, ColumnType.VARBINARY,ColumnType.LONGVARBINARY,ColumnType.BLOB);
  private static Set<ColumnType> dateTypes = Sets.newHashSet(ColumnType.DATE,ColumnType.TIME,ColumnType.TIMESTAMP);
  
  
  private String mappingPrefix;
  private String instancePrefix;
  private String vocabularyPrefix;
  private String primaryKeySeparator;
  private String rowidtemplate;
  
  /**
   * 
   * @param mappingPrefix
   * @param instancePrefix
   * @param vocabularyPrefix
   * @param primaryKeySeparator
   * @param rowidtemplate by convetion a query that yields the all the cols of a table plus a 
   *                column "sm_rowid" which contains the rowid.
   */
  public MappingGenerator(String mappingPrefix, String instancePrefix,
      String vocabularyPrefix, String primaryKeySeparator,String rowidtemplate) {
    super();
    this.mappingPrefix = mappingPrefix;
    this.instancePrefix = instancePrefix;
    this.vocabularyPrefix = vocabularyPrefix;
    this.primaryKeySeparator = primaryKeySeparator;
    this.rowidtemplate = rowidtemplate;
  }
  public MappingGenerator(String baseprefix, String mappingPrefix, String instancePrefix,
      String vocabularyPrefix, String primaryKeySeparator,String rowidtemplate) {
    super();
    if(baseprefix==null){
      baseprefix = "http://localhost/baseiri/";
    }
    this.mappingPrefix = mappingPrefix!=null?mappingPrefix:baseprefix + "mapping/";
    this.instancePrefix = instancePrefix!=null?mappingPrefix:baseprefix + "instance/";
    this.vocabularyPrefix = vocabularyPrefix!=null?mappingPrefix:baseprefix + "vocab/";
    this.primaryKeySeparator = primaryKeySeparator!=null?mappingPrefix:"+";
    this.rowidtemplate = rowidtemplate;
  }
  
  public MappingGenerator(String prefix){
    super();
    this.mappingPrefix = prefix + "mapping/";
    this.instancePrefix = prefix + "instance/";
    this.vocabularyPrefix = prefix + "vocabulary/";
    this.primaryKeySeparator = "+";
    this.rowidtemplate = null;

    
  }
  
  public Model generateMapping(DataContext context){
    
    return generateMapping(context.getDefaultSchema());
  }
  
  
  
  public Model generateMapping(Schema schema){
   Model r2r = initMappingModel();

   for(Table table: schema.getTables()){
     Resource triplesMap = r2r.createResource(mappingPrefix + "mapping/" + ues(table.getName()));
     

     
     //add the subject map
     
     String subjectTemplate = generateSubjectTemplate(table);
     Resource subjectMap = r2r.createResource();
     r2r.add(triplesMap, R2RML.HASSUBJECTMAP, subjectMap);
     r2r.add(subjectMap,R2RML.HASTEMPLATE,subjectTemplate);
     
     //if no primary present, generate a blank node
     if(table.getPrimaryKeys().length==0){
         subjectMap.addProperty(R2RML.TERMTYPE,R2RML.BLANKNODE);
      
     }
      
     //use the rowid template or not
     if(table.getPrimaryKeys().length==0&&rowidtemplate!=null){
       //add the subquery statement
       Resource rrSqlQuery = r2r.createResource();
       r2r.add(triplesMap,R2RML.HASLOGICALTABLE,rrSqlQuery);
       r2r.add(triplesMap,RDFS.comment,"Added subquery for having acccess to rowid functionality.");
       r2r.add(rrSqlQuery,R2RML.HASSQLQUERY,String.format(rowidtemplate, table.getName()));
       
       
     }else{
       //add the logical Table statment
       Resource rrTableName = r2r.createResource();
       r2r.add(triplesMap,R2RML.HASLOGICALTABLE,rrTableName);
       r2r.add(rrTableName,R2RML.HASTABLENAME, escapeName(table.getName()));
     }
    
     
    
     
     
     // and the class statement
     r2r.add(subjectMap,R2RML.HASCLASS,r2r.createResource(vocabularyPrefix + ues(table.getName())));
     
     
     // map all relations 
     for(Relationship relationship: table.getForeignKeyRelationships() ){

       Resource pomap  = r2r.createResource();
       triplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
       
       // generate the property
       List<String> cols = Lists.transform(Lists.newArrayList(relationship.getForeignColumns()), new Function<Column,String>(){
        @Override
        public String apply(Column input) {
          return ues(input.getName());
        }
       });
       String refMapPropertySuffix = Joiner.on(this.primaryKeySeparator).join(cols);
       pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(relationship.getForeignTable().getName()) +"#ref-"+ refMapPropertySuffix));
       
       // generate the object triple map condition
       Resource objectMap = r2r.createResource();
       pomap.addProperty(R2RML.HASOBJECTMAP, objectMap);
       
       objectMap.addProperty(R2RML.HASPARENTTRIPLESMAP, r2r.createResource(mappingPrefix + "mapping/" + ues(relationship.getPrimaryTable().getName())));
       
       for(int i = 0; i<relationship.getForeignColumns().length;i++  ){
         Resource joinCondition = r2r.createResource();
         objectMap.addProperty(R2RML.HASJOINCONDITION,joinCondition);
         joinCondition.addLiteral(R2RML.HASPARENT, this.escapeName(relationship.getPrimaryColumns()[i].getName()));
         joinCondition.addLiteral(R2RML.HASCHILD, this.escapeName(relationship.getForeignColumns()[i].getName()));
       }
     }
     
     // map all data columns 
     for(Column column : table.getColumns()){
       Resource pomap  = r2r.createResource();
       triplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
       pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(column.getTable().getName()) +"#"+ ues(column.getName())));       
       Resource objectMap = r2r.createResource();
       pomap.addProperty(R2RML.HASOBJECTMAP, objectMap);
       objectMap.addProperty(R2RML.HASCOLUMN, escapeName(column.getName()));
       if(intTypes.contains(column.getType())){ 
         objectMap.addProperty(R2RML.HASDATATYPE, 
             r2r.createResource(XSDDatatype.XSDinteger.getURI()));
       }else if(dateTypes.contains(column.getType())){
         objectMap.addProperty(R2RML.HASDATATYPE, 
             r2r.createResource(XSDDatatype.XSDdateTime.getURI()));
       }else if(doubleTypes.contains(column.getType())){
         objectMap.addProperty(R2RML.HASDATATYPE, 
             r2r.createResource(XSDDatatype.XSDdouble.getURI()));
       }else if(boolTypes.contains(column.getType())){
         objectMap.addProperty(R2RML.HASDATATYPE, 
             r2r.createResource(XSDDatatype.XSDboolean.getURI()));
       }else if(binaryTypes.contains(column.getType())){
         objectMap.addProperty(R2RML.HASDATATYPE, 
             r2r.createResource(XSDDatatype.XSDbase64Binary.getURI()));
       }
     } 
   }
    return r2r;
  }
  
  
  
  
  Model initMappingModel(){
    Model r2rmlMapping = ModelFactory.createDefaultModel();
    
    r2rmlMapping.setNsPrefix("rr", R2RML.R2RML_STRING);
    r2rmlMapping.setNsPrefix("vocab", vocabularyPrefix);
    r2rmlMapping.setNsPrefix("mapping", mappingPrefix);
    r2rmlMapping.setNsPrefix("inst", instancePrefix);
    return r2rmlMapping;
  }

 
  
  private String generateSubjectTemplate(Table table){
    List<Column> colsOfSubject = null;
    

    if(table.getPrimaryKeys().length>0){
      //join the primary keys to generate the subject pattern
      colsOfSubject= Lists.newArrayList(( table.getPrimaryKeys()));

    }else{
      
      if(rowidtemplate == null){
      //use all cols to generate subject
        colsOfSubject= Lists.newArrayList(( table.getColumns()));
      }else{
        // we use the rowid here
        colsOfSubject = Lists.newArrayList((Column)new MutableColumn("sm_rowid", table));
        
      }
    }
    
    Function<Column,String> funcCol2Colname = new Function<Column,String>() {
      @Override
      public String apply(Column input) {
        return String.format("%s=%s",ues(input.getName()), escapeAsTemplate(input.getName()));
      }
     };
    
    String templateSuffix =
        Joiner.on(primaryKeySeparator).join(
            Lists.transform(
                colsOfSubject, funcCol2Colname
                ) );
    
    String template = this.instancePrefix + ues(table.getName()) +"/" + templateSuffix;
    
    return template;
  }
  
  
  private String ues(String segment) {
    
    return IRILib.encodeUriComponent(segment);
    
  }
  
  private String escapeAsTemplate(String str){
    return String.format("{\"%s\"}", str);

  }
  
  private String escapeName(String str){
    return String.format("\"%s\"", str);
  }
  
 

}
