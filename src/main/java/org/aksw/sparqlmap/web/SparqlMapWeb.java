package org.aksw.sparqlmap.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.sparqlmap.core.SparqlMap;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;

@Controller
@RequestMapping("/")
public class SparqlMapWeb {
	Logger log = LoggerFactory.getLogger(SparqlMapWeb.class);
	
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
			log.warn("sm.maxdumpage, using value " + maxdumpage);
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
		smManager.getSparqlMap(context).dump(resp.getOutputStream(),outFormat);
	}
	
	
	
	@RequestMapping("/{context}/sparql-jena")
	public void executeOverDump(@RequestParam(value="query") String queryString, @RequestParam(required=false) String defaultgraph, @RequestParam(required=false) String format, WebRequest req, HttpServletResponse resp, @PathVariable String context){
		try {
			if (dump == null
					|| System.currentTimeMillis() > (dumpage + maxdumpage)) {

				try {
					dump = smManager.getSparqlMap(context).dump();
					dumpage = System.currentTimeMillis();
					
				} catch (SQLException e) {
					log.error("Error:", e);

					resp.sendError(503, e.getMessage());

				}
			}

			Query query = QueryFactory.create(queryString);

			String outputformat = req.getParameter("output");

			Dataset ds = DatasetImpl.wrap(dump);

			QueryExecution exec = QueryExecutionFactory.create(query, ds);
			
			
		
			if (query.isAskType()) {
				boolean ask = exec.execAsk();
				if (outputformat != null && outputformat.contains("json")) {
					resp.setContentType("application/sparql-results+json");
					ResultSetFormatter
							.outputAsJSON(resp.getOutputStream(), ask);
				} else {
					resp.setContentType("application/sparql-results+xml");
					ResultSetFormatter.outputAsXML(resp.getOutputStream(), ask);
				}
			} else if (query.isConstructType()) {
				Model qresult = exec.execConstruct();

				if (outputformat != null && outputformat.contains("json")) {
					resp.setContentType("application/sparql-results+json");
					
				} else {
					resp.setContentType("application/sparql-results+xml");
					qresult.write(resp.getOutputStream());
				}
			} else if (query.isDescribeType()) {
				Model qresult = exec.execDescribe();

				if (outputformat != null && outputformat.contains("json")) {
					resp.setContentType("application/sparql-results+json");
					RDFDataMgr.write(resp.getOutputStream(), qresult, Lang.RDFJSON);
				} else {
					resp.setContentType("application/sparql-results+xml");
					qresult.write(resp.getOutputStream());
				}
			} else if (query.isSelectType()) {
				ResultSet rs = exec.execSelect();
				if (outputformat != null && outputformat.contains("json")) {
					resp.setContentType("application/sparql-results+json");
					ResultSetFormatter.outputAsJSON(resp.getOutputStream(), rs);
				} else {
					resp.setContentType("application/sparql-results+xml");
					ResultSetFormatter.outputAsXML(resp.getOutputStream(), rs);
				}
			}

		} catch (IOException e1) {
			log.error("Error:", e1);
		}
		
	}
	
	
	private String getContentType(WebRequest req){
		return null;
	}
	
	
	public static void stream(ResultSet rs, HttpServletResponse resp, String returntype){
		
	}
	

}
