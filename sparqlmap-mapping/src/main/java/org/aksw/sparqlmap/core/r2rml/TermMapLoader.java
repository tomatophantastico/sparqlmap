package org.aksw.sparqlmap.core.r2rml;

import java.util.List;

import org.aksw.sparqlmap.core.r2rml.TermMapReferencing.JoinOn;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Lists;

public class TermMapLoader {
    


    public static TermMap load(Model r2rmlmodel, Resource termMap, String baseIri) {
      
      TermMap result = null;
      
  
      
      
      Resource termTypeRes = LoaderHelper.getSingleResourceObject(
          r2rmlmodel.listStatements(termMap, R2RML.TERMTYPE, (RDFNode) null));
      String termType = (termTypeRes == null?null:termTypeRes.getURI());
      
      
      String column = LoaderHelper.getSingleLiteralObjectValue(
          r2rmlmodel.listStatements(termMap, R2RML.HASCOLUMN, (RDFNode) null));
      String template = LoaderHelper.getSingleLiteralObjectValue(
          r2rmlmodel.listStatements(termMap, R2RML.HASTEMPLATE, (RDFNode) null));
      
      if(template != null && !template.contains(":")){
        template = baseIri + template;
      }
      
      RDFNode constant = LoaderHelper.getSingleRDFNode(
          r2rmlmodel.listStatements(termMap, R2RML.HASCONSTANT, (RDFNode) null));
      
      String language = LoaderHelper.getSingleLiteralObjectValue(
          r2rmlmodel.listStatements(termMap, R2RML.HASLANGUAGE, (RDFNode) null));
      
      Resource datatype = LoaderHelper.getSingleResourceObject(
          r2rmlmodel.listStatements(termMap, R2RML.HASDATATYPE, (RDFNode) null));
      
      Resource parentMap = LoaderHelper.getSingleResourceObject(
          r2rmlmodel.listStatements(termMap, R2RML.HASPARENTTRIPLESMAP, (RDFNode) null));
      
      
      //if not explicitly declared, we infer from the location (g,s,p,o) the term type
      if(termType==null){
        if(r2rmlmodel.contains(null, R2RML.HASOBJECTMAP, termMap)){
          if(column!=null || language != null || datatype !=null){
            termType = R2RML.LITERAL_STRING;
          }else{
            
          }
        }
      }
      //defaults to iri
      if(termType == null){
        termType = R2RML.IRI_STRING;
      }
      
      
      if(column!=null&&template==null&&constant==null&&parentMap==null){
        result = TermMapColumn.builder()
            .column(R2RMLHelper.unescape(column))
            .datatypIRI(datatype!=null?datatype.getURI():null)
            .termTypeIRI(termType)
            .build();
        
       
      }else if(column==null&&template!=null&&constant==null&&parentMap==null){
        List<TermMapTemplateTuple> templateTuples =  R2RMLHelper.splitTemplate(template);
        // expand the template with the base prefix
        
        result = TermMapTemplate.builder()
            .template(templateTuples)
            .termTypeIRI(termType)
            .build();
        
        
      }else if(column==null&&template==null&&constant!=null&&parentMap==null){
        TermMapConstant tmConst = null;
        if(constant.isURIResource()){
          tmConst = TermMapConstant.builder().termTypeIRI(termType).constantIRI(constant.asResource().getURI()).build();
        }else if(constant.isLiteral()){
          tmConst = TermMapConstant.builder()
                .constantLiteral(constant.asLiteral().getLexicalForm())
                .lang(constant.asLiteral().getLanguage())
                .datatypIRI(constant.asLiteral().getDatatypeURI())
                .termTypeIRI(R2RML.LITERAL_STRING)
                .build();

        }else{
          throw new R2RMLValidationException("Blank node is not valid constant value for term map");
        }
        result = tmConst;
      }else if(column==null&&template==null&&constant==null&&parentMap!=null){
         
          //only setting the join conditions here, parent map might not be loaded yet.
        
          //get all the join conditions
          List<TermMapReferencing.JoinOn> joinons = Lists.newArrayList();
          List<Statement> conditions = parentMap.listProperties(R2RML.HASJOINCONDITION).toList();
          for(Statement condition:  conditions){
            if(condition.getObject().isResource()){
              Resource condResource = condition.getObject().asResource();
              
              String parentCol = LoaderHelper.getSingleLiteralObjectValue( condResource.listProperties(R2RML.HASPARENT));
              String childCol = LoaderHelper.getSingleLiteralObjectValue( condResource.listProperties(R2RML.HASCHILD));
              
              
              JoinOn joinon = new JoinOn();
              joinon.setChildColumn(childCol);
              joinon.setParentColumn(parentCol);
              joinons.add(joinon);
            }
          }
          
          result = TermMapReferencing.builder()
              .parentMapUri(parentMap.getURI())
              .conditions(joinons)
              .termTypeIRI(termType)
              .build();
              
          
      
        
      }else{  
        throw new R2RMLValidationException("Check termmap definition for multiple or lacking definitons of rr:constant, rr:template or rr:column");
      }
      
      
      
      return result;
    }
    
    
    public static TermMap defaultGraphTermMap(){
      TermMapConstant dgTermMap = TermMapConstant.builder()
          .termTypeIRI(R2RML.IRI_STRING)
          .constantIRI(Quad.defaultGraphNodeGenerated.getURI()).build();
      return dgTermMap;
    }
}
