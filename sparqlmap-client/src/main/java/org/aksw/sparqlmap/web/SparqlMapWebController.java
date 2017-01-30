package org.aksw.sparqlmap.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.jooq.lambda.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Lists;

import lombok.Data;



@Controller
@RequestMapping("/")
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
	
	@RequestMapping("/sparql")
	public void executeSparqlQuery(@RequestParam(value="query") String queryString, @RequestParam(required=false) String defaultgraph, @RequestParam(required=false) String format, WebRequest req, HttpServletResponse resp){
		executeSparqlQuery(queryString, defaultgraph, format, req, resp, SparqlMapContextManager.ROOT);
	}
	
	@RequestMapping("/{context}/sparql")
	public void executeSparqlQuery(@RequestParam(value="query") String queryString, @RequestParam(required=false) String defaultgraph, @RequestParam(required=false) String format, WebRequest req, HttpServletResponse resp, @PathVariable String context){
	
		try {
			
			String query = req.getParameter("query");
			String outputformat = req.getParameter("output");
			List<String> acceptHeaders = Arrays.asList(req.getHeaderValues("accept"));
			
			
			//determine the return type of the application
			Object resultsFormat = null;
			if(!acceptHeaders.isEmpty()){
				for(String acceptHeader: acceptHeaders){
					Lang rdflang =  RDFLanguages.nameToLang(acceptHeader);
					if(rdflang!=null){
						resultsFormat = rdflang;
						break;
					}else if(acceptHeader.contains("+")){
						
						String lookupString = acceptHeader.substring(acceptHeader.indexOf("+")+1);
						if(lookupString.contains(",")){
							lookupString = lookupString.substring(0,lookupString.indexOf(","));
						}
						
						ResultsFormat rs =  ResultsFormat.lookup(lookupString);
						if(rs!=null&&!ResultsFormat.isRDFGraphSyntax(rs)){
							resultsFormat = rs;
							break;
						}
					}
					
				}
			} 
			// reutrn type might still be null, but is determined according to the query type later on.
			
			
			log.debug("Receveived query: " + query);
			try {
				
				
					resp.setContentType(WebContent.contentTypeRDFXML);
					smManager.getSparqlMap(context).executeSparql(query,resultsFormat, resp.getOutputStream());
					
			} catch (SQLException e) {
				
				resp.getOutputStream().write(e.getMessage().getBytes());
				log.error("Error for query \n" + query + "\n",e);
			}
		} catch (IOException e) {
			log.error("Error:",e);
		} catch (Throwable t){
			log.error("Throwable caught: ", t);
		
		}
		
		
		
	}
	
	
	
	@RequestMapping("/dump")
	public void dump(WebRequest req, HttpServletResponse resp) throws SQLException, IOException{
		dump(req, SparqlMapContextManager.ROOT, resp);
	}
	
	@RequestMapping("/{context}/dump")
	public void dump(WebRequest req, @PathVariable String context, HttpServletResponse resp) throws SQLException, IOException{
		String outFormat = getContentType(req);
		smManager.getSparqlMap(context).getDumpExecution().dump(resp.getOutputStream(), RDFLanguages.nameToLang(outFormat));
	}
	
	@RequestMapping("/context")
	public @ResponseBody List<Map<String,String>> listContexts(){
	  List<Map<String,String>> result = Seq.seq(smManager.getResource2sparqlmap()).map(e -> 
	  {
	    Map<String,String> entry = Maps.newHashMap();
      entry.put("endpoint","/" + e.v1);
      entry.put("title", e.v2.getMapping().getDescription());
      return entry;
	    
	  }).collect(Collectors.toList());
	  
	  return result;
	 
	}
	
	
	
	
	
	private String getContentType(WebRequest req){
		return "NTRIPLES";
	}
	
	
	public static void stream(ResultSet rs, HttpServletResponse resp, String returntype){
		
	}
	

}
