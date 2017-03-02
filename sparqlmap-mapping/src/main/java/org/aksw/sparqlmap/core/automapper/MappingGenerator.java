package org.aksw.sparqlmap.core.automapper;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Lists;


/**
 * Creates a Direct Mapping for the given schema
 * 
 * @author joerg
 *
 */
public class MappingGenerator {
  
 
  
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
      String vocabularyPrefix, String primaryKeySeparator) {
    super();
    this.mappingPrefix = mappingPrefix;
    this.instancePrefix = instancePrefix;
    this.vocabularyPrefix = vocabularyPrefix;
    this.primaryKeySeparator = primaryKeySeparator;
  }
  public MappingGenerator(String baseprefix, String mappingPrefix, String instancePrefix,
      String vocabularyPrefix, String primaryKeySeparator) {
    super();
    if(baseprefix==null){
      baseprefix = "http://localhost/baseiri/";
    }
    this.mappingPrefix = mappingPrefix!=null?mappingPrefix:baseprefix + "mapping/";
    this.instancePrefix = instancePrefix!=null?mappingPrefix:baseprefix + "instance/";
    this.vocabularyPrefix = vocabularyPrefix!=null?mappingPrefix:baseprefix + "vocab/";
    this.primaryKeySeparator = primaryKeySeparator!=null?mappingPrefix:"+";
  }
  
  public MappingGenerator(String prefix){
    super();
    this.mappingPrefix = prefix + "mapping/";
    this.instancePrefix = prefix + "instance/";
    this.vocabularyPrefix = prefix + "vocabulary/";
    this.primaryKeySeparator = "+";
    this.rowidtemplate = null;
  }
  

  
  
  
  public Model generateMapping(LogicalSchema schema){
    Model r2r = initMappingModel();

    for(LogicalTable table: schema.getTables()){
      Resource triplesMap = r2r.createResource(mappingPrefix + "mapping/" + ues(table.getTablename()));

      //add the subject map
      String subjectTemplate = generateSubjectTemplate(table);
      Resource subjectMap = r2r.createResource();
      r2r.add(triplesMap, R2RML.HASSUBJECTMAP, subjectMap);
      r2r.add(subjectMap,R2RML.HASTEMPLATE,subjectTemplate);

      //if no primary present, generate a blank node
      if(table.getPrimaryKeys().isEmpty()){
        subjectMap.addProperty(R2RML.TERMTYPE,R2RML.BLANKNODE);

      }

      //use the rowid template or not
      if(table.getPrimaryKeys().isEmpty()&&rowidtemplate!=null){
        //add the subquery statement
        Resource rrSqlQuery = r2r.createResource();
        r2r.add(triplesMap,R2RML.HASLOGICALTABLE,rrSqlQuery);
        r2r.add(triplesMap,RDFS.comment,"Added subquery for having acccess to rowid functionality.");
        r2r.add(rrSqlQuery,R2RML.HASSQLQUERY,String.format(rowidtemplate, table.getTablename()));


      }else{
        //add the logical Table statment
        Resource rrTableName = r2r.createResource();
        r2r.add(triplesMap,R2RML.HASLOGICALTABLE,rrTableName);
        r2r.add(rrTableName,R2RML.HASTABLENAME, escapeName(table.getTablename()));
      }

      // and the class statement
      r2r.add(subjectMap,R2RML.HASCLASS,r2r.createResource(vocabularyPrefix + ues(table.getTablename())));

      // map all relations 
      schema.getRelations().stream().filter(rel -> rel.getForeign().equals(table)).forEach(relationship -> {

        Resource pomap  = r2r.createResource();
        triplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
        
        // generate the property
        String refMapPropertySuffix =  relationship.getOns().stream().map(on -> on[1].getName()).map(colname -> ues(colname)).collect(Collectors.joining(primaryKeySeparator));


        pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(relationship.getForeign().getTablename()) +"#ref-"+ refMapPropertySuffix));

        // generate the object triple map condition
        Resource objectMap = r2r.createResource();
        pomap.addProperty(R2RML.HASOBJECTMAP, objectMap);

        objectMap.addProperty(R2RML.HASPARENTTRIPLESMAP, 
            r2r.createResource(mappingPrefix + "mapping/" + ues(relationship.getPrimary().getTablename())));

        relationship.getOns().forEach(on->{
          Resource joinCondition = r2r.createResource();
          objectMap.addProperty(R2RML.HASJOINCONDITION,joinCondition);
          joinCondition.addLiteral(R2RML.HASPARENT, this.escapeName(on[0].getName()));
          joinCondition.addLiteral(R2RML.HASCHILD, this.escapeName(on[1].getName()));

        });


      });

      // map all data columns 
      for(LogicalColumn column : table.getColumns()){
        Resource pomap  = r2r.createResource();
        triplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
        pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(column.getTable().getTablename()) +"#"+ ues(column.getName())));       
        Resource objectMap = r2r.createResource();
        pomap.addProperty(R2RML.HASOBJECTMAP, objectMap);
        objectMap.addProperty(R2RML.HASCOLUMN, escapeName(column.getName()));
        column.getXsdDataType().ifPresent(dtString -> 
        objectMap.addProperty(R2RML.HASDATATYPE, 
            r2r.createResource(dtString)));

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

 
  
  private String generateSubjectTemplate(LogicalTable table){
    List<LogicalColumn> colsOfSubject = null;
    

    if(!table.getPrimaryKeys().isEmpty()){
      //join the primary keys to generate the subject pattern
      colsOfSubject = table.getPrimaryKeys();
    }else{
      //use all cols to generate subject
        colsOfSubject= Lists.newArrayList(( table.getColumns()));
    }
    

    
    String templateSuffix = colsOfSubject.stream().map(LogicalColumn::getName).map(name -> 
       String.format("%s=%s",ues(name), escapeAsTemplate(name))
     ).collect(Collectors.joining(primaryKeySeparator));
        
    
    String template = this.instancePrefix + ues(table.getTablename()) +"/" + templateSuffix;
    
    return template;
  }
  
  
  public static String ues(String segment) {
    
    return IRILib.encodeUriComponent(segment);
    
  }
  
  private String escapeAsTemplate(String str){
    return String.format("{\"%s\"}", str);

  }
  
  private String escapeName(String str){
    return String.format("\"%s\"", str);
  }
  
 

}
