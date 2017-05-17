package org.aksw.sparqlmap.core.automapper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



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
    this.instancePrefix = instancePrefix!=null?instancePrefix:baseprefix + "instance/";
    this.vocabularyPrefix = vocabularyPrefix!=null?vocabularyPrefix:baseprefix + "vocab/";
    this.primaryKeySeparator = primaryKeySeparator!=null?primaryKeySeparator:";";
  }
  
  public MappingGenerator(String prefix){
    super();
    this.mappingPrefix = prefix + "mapping/";
    this.instancePrefix = prefix + "instance/";
    this.vocabularyPrefix = prefix + "vocabulary/";
    this.primaryKeySeparator = ";";
    this.rowidtemplate = null;
  }
  

  
  
  
  public Model generateMapping(LogicalSchema schema){
    
    Map<String,Resource> table2triplesMap = Maps.newHashMap();
    Model r2r = initMappingModel();

    for(LogicalTable table: schema.getTables()){
      Resource triplesMap = r2r.createResource(mappingPrefix + "mapping/" + ues(table.getTablename()));
      table2triplesMap.put(table.getName(), triplesMap);
      
      
      Set<LogicalColumn> idCols = getKeyCols(table, schema);
      Resource subjectMap = r2r.createResource();

      //if no primary present, generate a blank node
      if(table.getPrimaryKeys().isEmpty()){
        subjectMap.addProperty(R2RML.TERMTYPE,R2RML.BLANKNODE);
      }

      if(idCols.isEmpty()){
        idCols = table.getColumns().stream().sorted((c1,c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toCollection(LinkedHashSet::new));
      }
      String subjectTemplate = generateSubjectTemplate(idCols,table.getName());
      //add the subject map
      r2r.add(triplesMap, R2RML.HASSUBJECTMAP, subjectMap);
      r2r.add(subjectMap,R2RML.HASTEMPLATE,subjectTemplate);
      
      
      
      
     

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

     

      // map all data columns 
      for(LogicalColumn column : table.getColumns()){
        Resource pomap  = r2r.createResource();
        triplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
        pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(column.getTable().getTablename()) +"#"+ ues(column.getName())));       
        Resource objectMap = r2r.createResource();
        pomap.addProperty(R2RML.HASOBJECTMAP, objectMap);
        objectMap.addProperty(R2RML.HASCOLUMN, escapeName(column.getName()));
        Optional.ofNullable(column.getXsdDataType()).ifPresent(dtString -> 
        objectMap.addProperty(R2RML.HASDATATYPE, 
            r2r.createResource(dtString)));

      } 
    }
    
    addFKRelations(schema, r2r, table2triplesMap);
    

    
    
    return r2r;
  }
  
  
  
  
  
  private void addFKRelations(LogicalSchema schema, Model r2r,Map<String,Resource> table2triplesMap){
    
      
      
   
    
    // map all relations 
    schema.getRelations().forEach(relationship -> {
      
      
      Resource childTriplesMap = table2triplesMap.get(relationship.getForeign().getName());
      
      Resource parentTriplesMap =  table2triplesMap.get(relationship.getPrimary().getName());
      Resource parentSubjectMap = parentTriplesMap.getProperty(R2RML.HASSUBJECTMAP).getObject().asResource();
      Optional<Resource> blank = Optional.ofNullable(parentSubjectMap.asResource().getProperty(R2RML.TERMTYPE)).map(stmt -> stmt.getObject().asResource());
      String template = parentSubjectMap.getProperty(R2RML.HASTEMPLATE).getLiteral().getString();
      Resource pomap  = r2r.createResource();
      childTriplesMap.addProperty(R2RML.HASPREDICATEOBJECTMAP,pomap);
      
      // generate the property
      String refMapPropertySuffix =  relationship.getOns().stream().map(on -> on[1].getName()).map(colname -> ues(colname)).collect(Collectors.joining(primaryKeySeparator));


      pomap.addProperty(R2RML.HASPREDICATE, r2r.createResource(vocabularyPrefix +ues(relationship.getForeign().getTablename()) +"#ref-"+ refMapPropertySuffix));

      
      //generate a template for the object 
      String objectTemplateString = generateRefTemplate(template, relationship.getOns());
      Resource objectMap = ResourceFactory.createResource();
      r2r.add(pomap, R2RML.HASOBJECTMAP, objectMap);
      r2r.add(objectMap,R2RML.HASTEMPLATE, objectTemplateString);
      blank.ifPresent(b ->  r2r.add(objectMap,R2RML.TERMTYPE, R2RML.BLANKNODE));

    });
    
    }
  
  
  
  
  
  Model initMappingModel(){
    Model r2rmlMapping = ModelFactory.createDefaultModel();
    
    r2rmlMapping.setNsPrefix("rr", R2RML.R2RML_STRING);
    r2rmlMapping.setNsPrefix("vocab", vocabularyPrefix);
    r2rmlMapping.setNsPrefix("mapping", mappingPrefix);
    r2rmlMapping.setNsPrefix("inst", instancePrefix);
    return r2rmlMapping;
  }

 
  
  
  private Set<LogicalColumn> getKeyCols(LogicalTable table, LogicalSchema schema){
    Set<LogicalColumn> colsOfSubject = Sets.newLinkedHashSet( table.getPrimaryKeys());

    if(colsOfSubject.isEmpty() && !table.getFkRelations().isEmpty()){
      colsOfSubject = table.getFkRelations().stream().flatMap(rel -> rel.getOns().stream().map(on -> on[1])).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    if(colsOfSubject.isEmpty() && !table.getPRelations().isEmpty()){
      colsOfSubject = table.getPRelations().stream().flatMap(rel -> rel.getOns().stream().map(on -> on[0])).collect(Collectors.toCollection(LinkedHashSet::new));
    }
   
    

    return colsOfSubject;
  }
  
  private String generateSubjectTemplate(Collection<LogicalColumn> colsOfSubject, String tablename){
   
    
    String templateSuffix = colsOfSubject.stream().map(LogicalColumn::getName).map(name -> 
    String.format("%s=%s",ues(name), escapeAsTemplate(name))
        ).collect(Collectors.joining(primaryKeySeparator));


    String template = this.instancePrefix + ues(tablename) +"/" + templateSuffix;

    return template;
  }
  
  private String generateRefTemplate(String parentTemplate, Collection<LogicalColumn[]> ons){
    String result = parentTemplate;
    for(LogicalColumn[] on : ons) {
      result = result.replace(String.format("{\"%s\"}",on[0].getName()), String.format("{\"%s\"}",on[1].getName()));
    };
    

    return result;

    
    
    
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
