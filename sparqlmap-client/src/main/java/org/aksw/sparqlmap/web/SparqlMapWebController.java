package org.aksw.sparqlmap.web;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.aksw.sparqlmap.core.util.JenaHelper;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.jooq.lambda.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.google.common.base.Predicates;



@Controller
@RequestMapping(method= {RequestMethod.GET,RequestMethod.POST}, value = "/api")
public class SparqlMapWebController {
	Logger log = LoggerFactory.getLogger(SparqlMapWebController.class);
	
	@Autowired
	SparqlMapContextManager smManager;
	
	@Autowired
	Environment env;
	
	long maxdumpage = 60000;
	long dumpage = 0;
	DatasetGraph dump = null;
	
	@PostConstruct
	public void setUp(){
		if(env.getProperty("sm.maxdumpage")!=null){
			maxdumpage = Long.parseLong(env.getProperty("sm.maxdumpage"));
		}else{
			log.info("sm.maxdumpage, using value " + maxdumpage);
		}
		
	}
	
	@RequestMapping(method= {RequestMethod.GET,RequestMethod.POST}, value = "/sparql")
	public void executeSparqlQuery(@RequestParam(value="query") String queryString, @RequestParam Optional<String> defaultgraph, @RequestParam Optional<String> format, @RequestHeader HttpHeaders headers, HttpServletResponse resp){
		executeSparqlQuery(queryString, defaultgraph, format, headers, resp, SparqlMapContextManager.ROOT);
	}
	
	@RequestMapping(method= {RequestMethod.GET,RequestMethod.POST}, value = "/{context}/sparql")
	public void executeSparqlQuery(@RequestParam(value="query") String queryString, @RequestParam Optional<String> defaultgraph, @RequestParam Optional<String> format, @RequestHeader HttpHeaders headers, HttpServletResponse resp, @PathVariable String context){
	
	  
	  List<ContentType> targetLangs = headers.getAccept().stream()
	      .map(mediaType -> mediaType.getType() + '/' + mediaType.getSubtype())
	      .map(WebContent::contentTypeCanonical)
	      .filter(Predicates.notNull())
	      .map(ContentType::create)
	      .collect(Collectors.toCollection(()-> new java.util.ArrayList<ContentType>()));
	  
	  format.map(formatString -> RDFLanguages.nameToLang(formatString))
	    .ifPresent(lang -> targetLangs.add(lang.getContentType()));
	  
		try {
			
			    TranslationContext tc = new TranslationContext();
			    tc.setQueryString(queryString);
			    Query query = QueryFactory.create(queryString);
			    tc.setQuery(query);
          QueryExecution qexec = smManager.getSparqlMap(context).execute(tc);

			    if(query.isSelectType()){
		         ResultSet rs = qexec.execSelect();

	           ResultsFormat resFormat = targetLangs.stream()
	               .map(JenaHelper::contentTypeToSelectResultFormats)
	               .filter(rf -> rf != null)
	               .findFirst()
	               .orElse(ResultsFormat.FMT_RS_JSON);
	           
	           resp.setContentType(resFormat.getSymbol());
	           ResultSetFormatter.output(resp.getOutputStream(), rs, resFormat);

			    }else if(query.isConstructType() || query.isDescribeType()){
			      Lang tragetLang = targetLangs.stream().map(ct -> RDFLanguages.contentTypeToLang(ct)).filter(lang -> lang != null).findFirst().orElse(RDFLanguages.TURTLE);
			      Model result = query.isDescribeType() ? qexec.execDescribe():qexec.execConstruct();			      
			      resp.setContentType(tragetLang.getHeaderString());
			      RDFDataMgr.write(resp.getOutputStream(),result, tragetLang);
			    }else if(query.isAskType()){
			      resp.setContentType(ResultsFormat.FMT_RS_XML.getSymbol());
			     boolean answer = qexec.execAsk();
			     boolean sent = false;
			     OutputStream out =  resp.getOutputStream();
			     for(ContentType tartgetCt: targetLangs){
			       String ctString = tartgetCt.getContentType();
             //Jenas ResultSetFormatter lacks a parameterizable output function, so here we go:
             if(ResultsFormat.FMT_RS_XML.getSymbol().equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.outputAsXML(out, answer);
               sent = true;
             }else if(ResultsFormat.FMT_RS_JSON.equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.outputAsJSON(out, answer);
               sent = true;
             }else if(ResultsFormat.FMT_TEXT.equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.out(out, answer);
               sent = true;
             } else if(ResultsFormat.FMT_RS_CSV.equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.outputAsCSV(out, answer);
               sent = true;
             }else if(ResultsFormat.FMT_RS_TSV.equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.outputAsTSV(out, answer);
               sent = true;
             }else if(ResultsFormat.FMT_RS_SSE.equals(ctString)){
               resp.setContentType(ctString);
               ResultSetFormatter.outputAsSSE(out,answer);
               sent = true;
             }
             
         
             if(sent){
               break;
             }
			     }
			     
			     
			     if(!sent){
			       throw new HttpMediaTypeNotSupportedException("Unable to answer SPARQL ask in with: " + targetLangs.stream().map(ContentType::getContentType).collect(Collectors.joining(", ")));
			     }
			      
			   
			      
			      
			    }

		} catch (Exception t){
			log.error("Throwable caught: ", t);
			throw new RuntimeException(t);
		}
	}
	
	
	
	@GetMapping("/dump")
	public void dump(WebRequest req, HttpServletResponse resp) throws SQLException, IOException{
		dump(req, SparqlMapContextManager.ROOT, resp);
	}
	
	@GetMapping("/{context}/dump")
	public void dump(WebRequest req, @PathVariable String context, HttpServletResponse resp) throws SQLException, IOException{
		String outFormat = getContentType(req);
		smManager.getSparqlMap(context).getDumpExecution().dump(resp.getOutputStream(), RDFLanguages.nameToLang(outFormat));
	}
	

  @GetMapping("/context")
  public @ResponseBody Collection<Map<String,String>> listContexts(){
    List<Map<String,String>> result = Seq.seq(smManager.getResource2sparqlmap()).map(e -> 
    {
      Map<String,String> entry = Maps.newHashMap();
      entry.put("endpoint",e.v1);
      entry.put("title", e.v2.getMapping().getDescription());
      return entry;
      
    }).collect(Collectors.toList());
    
    return result;
   
  }
  
  @GetMapping("/context/{context}/mapping")
  public @ResponseBody Collection<QuadMap> getContextMapping(@PathVariable String context){
    
    return smManager.getSparqlMap(context).getMapping().getQuadMaps();
   
  }
  
  @GetMapping("/context/{context}/relation")
  public @ResponseBody LogicalSchema getContextLogicalTables(@PathVariable String context){
    
    LogicalSchema schema  = SchemaTranslator.translate( smManager.getSparqlMap(context).getDataContext().getDefaultSchema());
    return schema;
  }
	
	
	
	
	private String getContentType(WebRequest req){
		return "NTRIPLES";
	}
	
	


	 /* 
  @RequestMapping("/error")
  public  Map<String, Object> error(HttpServletRequest req) {
    Map<String, Object> body = getErrorAttributes(req,getTraceParameter(req));
    String trace = (String) body.get("trace");
    if(trace != null){
      String[] lines = trace.split("\n\t");
      body.put("trace", lines);
    }
    return body;
  }
  
  
  private boolean getTraceParameter(HttpServletRequest request) {
    String parameter = request.getParameter("trace");
    if (parameter == null) {
        return false;
    }
    return !"false".equals(parameter.toLowerCase());
  }
  
  @Autowired
  private ErrorAttributes errorAttributes;

  private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest, boolean includeStackTrace) {
    RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
    return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
  }
  @Override
  public String getErrorPath() {
    return "/api/error";
  }
	
*/
}
