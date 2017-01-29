package org.aksw.sparqlmap.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aksw.sparqlmap.core.mapper.finder.MappingBinding;
import org.aksw.sparqlmap.core.mapper.finder.QueryInformation;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;

import com.google.common.base.Stopwatch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * This class holds all information needed for a specific translation.
 * 
 * @author joerg
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationContext {
	
	private String queryString;
	
	private String queryName;
	
	private MappingBinding queryBinding;

	private Query query;
	
	private Op beautifiedQuery;
	
	private QueryInformation queryInformation;
	 /*
   * define if the result should be json/rdf, RDF/XML, turtle or else
   */
  private Object target = null;
  
  private Throwable problem;
  
  
	
	
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
  public int duplicatecounter = 0;

  
  public int getAndIncrementDuplicateCounter() {
    return this.duplicatecounter++;
  }
	
  /*
   * Simple profiler starts below
   * 
   * 
   */
  
  public Map<String, Long> phaseDurations = new LinkedHashMap<String, Long>();

	private Stopwatch sw;
	private String currentPhase;

  private int propertypathsuffix = 0;

  
  
	public void profileStartPhase(String phase) {
		if(sw == null){
			currentPhase = phase;
			sw = Stopwatch.createStarted();
			
		}else{
			sw.stop();
			phaseDurations.put(currentPhase, sw.elapsed(TimeUnit.MICROSECONDS));
			currentPhase = phase;
			sw.reset();
			sw.start();
		}
		
	}
	
	public void profileStop(){
//		if(sw==null){
//			throw new UnsupportedOperationException("Has first to be started");
//		}
//		sw.stop();
//		phaseDurations.put(currentPhase, sw.elapsed(TimeUnit.MICROSECONDS));

	}
	
	
	

 
  public String getPropertyPathPrefix() {
    return "sm_pp_" +  this.propertypathsuffix ++;
   }
	
	
	
	

}
