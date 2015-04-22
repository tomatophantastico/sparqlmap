package org.aksw.sparqlmap.web;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlmap.core.SparqlMap;
import org.springframework.context.ApplicationContext;


public class SparqlMapContextManager {
	
	public static String ROOT = "ROOT";
	
	private Map<String, ApplicationContext> resourceString2sparqlmapContext = new HashMap<String, ApplicationContext>();
	
	public SparqlMap getSparqlMap(String resource){
		return resourceString2sparqlmapContext.get(resource).getBean(SparqlMap.class);
	}
	
	public void putContext(String resource, ApplicationContext context){
		resourceString2sparqlmapContext.put(resource, context);
	}
	
	

}
