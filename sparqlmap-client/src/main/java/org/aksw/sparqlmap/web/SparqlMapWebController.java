package org.aksw.sparqlmap.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.core.TranslationContext;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.schema.LogicalSchema;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.jooq.lambda.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;



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
	      .map(mediaType -> mediaType.getType())
	      .map(WebContent::contentTypeCanonical)
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
		         
		         for(ContentType langCandidate: targetLangs){
		           Consumer<ResultSet> resultSetSender = null;
		           
		           if(RDFLanguages.TEXT.equals(langCandidate)){
		             resultSetSender = ((qres) -> ResultSetFormatter.asText(qres));		             
		           } else if(WebContent.contentTypeResultsXML.equals(langCandidate)){
		             
		           }
		           
		           if(resultSetSender!=null){
		             resultSetSender.accept(rs);
		             break;
		           }
		         
		         }
		         

			      
			    }

		} catch (Exception t){
			log.error("Throwable caught: ", t);
			throw t;
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
    
    return smManager.getSparqlMap(context).getMapping().getQuadMaps().values();
   
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
