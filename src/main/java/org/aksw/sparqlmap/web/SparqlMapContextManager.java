package org.aksw.sparqlmap.web;

import java.util.Map;

import org.aksw.sparqlmap.core.SparqlMap;

import com.google.common.collect.Maps;


public class SparqlMapContextManager {
	
	public static String ROOT = "ROOT";
	
	private Map<String, SparqlMap> resource2sparqlmap = Maps.newHashMap();
	
	public SparqlMap getSparqlMap(String resource){
		return resource2sparqlmap.get(resource);
	}
	
	public void putContext(String resource, SparqlMap context){
	  resource2sparqlmap.put(resource, context);
	}
	
	

}
