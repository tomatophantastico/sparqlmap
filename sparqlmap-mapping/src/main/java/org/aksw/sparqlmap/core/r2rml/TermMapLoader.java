package org.aksw.sparqlmap.core.r2rml;

import java.util.List;
import java.util.Optional;

import org.aksw.sparqlmap.core.r2rml.TermMapReferencing.JoinOn;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.XSD;

import com.google.common.collect.Lists;

public class TermMapLoader {
    


    public static TermMap load(Model r2rmlmodel, Resource termMap, String baseIri, LogicalTable ltab) {
      
      TermMap result = null;
      
  
      
      
      Resource termTypeRes = LoaderHelper.getSingleResourceObject(
          r2rmlmodel.listStatements(termMap, R2RML.TERMTYPE, (RDFNode) null));
      String termType = (termTypeRes == null?null:termTypeRes.getURI());
      
      

      Optional<String> column = Optional.ofNullable(termMap.getProperty(R2RML.HASCOLUMN)).map(Statement::getString);
      
      String template = LoaderHelper.getSingleLiteralObjectValue(
          r2rmlmodel.listStatements(termMap, R2RML.HASTEMPLATE, (RDFNode) null));
      
  
      
      RDFNode constant = LoaderHelper.getSingleRDFNode(
          r2rmlmodel.listStatements(termMap, R2RML.HASCONSTANT, (RDFNode) null));
      
      
      String language = Optional.ofNullable(termMap.getProperty(R2RML.HASLANGUAGE)).map(Statement::getString).orElse(null);
      
      String datatype = Optional.ofNullable(termMap.getProperty(R2RML.HASDATATYPE)).map(stmt -> stmt.getObject().asResource().getURI() ).orElse(null);
      
      String condition_pattern = Optional.ofNullable(termMap.getProperty(SMAP.REQUIRED_PATTERN)).map(Statement::getString).orElse(null);
      
      String transform_pattern = Optional.ofNullable(termMap.getProperty(SMAP.TRANSFORM_PATTERN)).map(Statement::getString).orElse(null);
      
      //check for given transform pattern,that a condition is present
      if(transform_pattern!=null && condition_pattern == null){
        throw new R2RMLValidationException("transform pattern \"" +transform_pattern+ "\" found, but no condition given.");
      }
      
      
      
      Resource parentMap = LoaderHelper.getSingleResourceObject(
          r2rmlmodel.listStatements(termMap, R2RML.HASPARENTTRIPLESMAP, (RDFNode) null));
      
      
      //if not explicitly declared, we infer from the location (g,s,p,o) the term type
      if(termType==null){
        //if used as object and has literal properties, it is a literal
        if(r2rmlmodel.contains(null, R2RML.HASOBJECTMAP, termMap)){
          if(column.isPresent() || language != null || datatype != null){
            termType = R2RML.LITERAL_STRING;
          }else{
            termType = R2RML.IRI_STRING;
          }
        }else{
          // used elsewhere, default to IRI
          termType = R2RML.IRI_STRING;

        }
      }
      
      //if the termmap produces an iri, we prefix it, if a prefix is not there.
      
      if(template != null && !template.contains(":") && R2RML.IRI_STRING.equals(termType)){
        template = baseIri + template;
      }

      
      
      if(column.isPresent() &&template==null&&constant==null&&parentMap==null){
        String colString = column.get();
        
        result = TermMapColumn.builder()
            .column(LogicalColumn.builder(ltab).name( R2RMLHelper.unescape(colString)).build())
            .termTypeIRI(termType)
            .datatypIRI(datatype)
            .lang(language)
            .condition(condition_pattern)
            .transform(transform_pattern)
            .build();
        
       
      }else if(!column.isPresent() && template!=null&&constant==null&&parentMap==null){
        List<TermMapTemplateTuple> templateTuples =  R2RMLHelper.splitTemplate(template,ltab);
        // expand the template with the base prefix
        
        result = TermMapTemplate.builder()
            .template(templateTuples)
            .termTypeIRI(termType)
            .datatypIRI(datatype)
            .lang(language)
            .condition(condition_pattern)
            .transform(transform_pattern)
            .build();
        
        
      }else if(!column.isPresent()&&template==null&&constant!=null&&parentMap==null){
        TermMapConstant tmConst = null;
        if(constant.isURIResource()){
          tmConst = termMapFromNodeUri(constant.asResource());
        }else if(constant.isLiteral()){
          
          //set empty 
          
          
          tmConst = termMapFromLiteralNode(constant);

        }else{
          throw new R2RMLValidationException("Blank node is not valid constant value for term map");
        }
        result = tmConst;
      }else if(!column.isPresent()&&template==null&&constant==null&&parentMap!=null){
         
          //only setting the join conditions here, parent map might not be loaded yet.
        
          //get all the join conditions
          List<TermMapReferencing.JoinOn> joinons = Lists.newArrayList();
          List<Statement> conditions = termMap.listProperties(R2RML.HASJOINCONDITION).toList();
          for(Statement condition:  conditions){
            if(condition.getObject().isResource()){
              Resource condResource = condition.getObject().asResource();
              
              String parentCol = R2RMLHelper.unescape( 
                  LoaderHelper.getSingleLiteralObjectValue( condResource.listProperties(R2RML.HASPARENT)));
              String childCol = R2RMLHelper.unescape( 
                  LoaderHelper.getSingleLiteralObjectValue( condResource.listProperties(R2RML.HASCHILD)));
              
              
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
        throw new R2RMLValidationException("Check termap definition for multiple or lacking definitons of rr:constant, rr:template or rr:column");
      }
      

      
      return result;
    }


    public static TermMapConstant termMapFromNodeUri(String constant) {
      return TermMapConstant.builder().termTypeIRI(R2RML.IRI_STRING).constantIRI(constant).build();
    }
    public static TermMapConstant termMapFromNodeUri(Resource constant) {
      return termMapFromNodeUri(constant.getURI());
    }


    public static TermMapConstant termMapFromLiteralNode(RDFNode constant) {

      String language = "".equals(constant.asLiteral().getLanguage())?null:constant.asLiteral().getLanguage();
      String datatype = constant.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())? null : constant.asLiteral().getDatatypeURI();
      
      return TermMapConstant.builder()
            .constantLiteral(constant.asLiteral().getLexicalForm())
            .datatypIRI(datatype)
            .lang(language)
            .termTypeIRI(R2RML.LITERAL_STRING)
            .build();
      
    }
    
    
    public static TermMapConstant termMapFromLiteralNode(Node_Literal constant) {

      String language = "".equals(constant.getLiteralLanguage())?null:constant.getLiteralLanguage();
      String datatype = constant.getLiteralDatatypeURI().equals(XSD.xstring.getURI())? null : constant.getLiteralDatatypeURI();
      
      return TermMapConstant.builder()
            .constantLiteral(constant.getLiteralLexicalForm())
            .datatypIRI(datatype)
            .lang(language)
            .termTypeIRI(R2RML.LITERAL_STRING)
            .build();
      
    }
    
    
    public static TermMap defaultGraphTermMap(){
      TermMapConstant dgTermMap = TermMapConstant.builder()
          .termTypeIRI(R2RML.IRI_STRING)
          .constantIRI(Quad.defaultGraphNodeGenerated.getURI()).build();
      return dgTermMap;
    }
}
