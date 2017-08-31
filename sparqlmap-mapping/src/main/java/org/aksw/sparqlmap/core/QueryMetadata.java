package org.aksw.sparqlmap.core;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

import lombok.Data;


@Data
public class QueryMetadata {
  private Map<SparqlMapPhase, Stopwatch> phaseDurations = Maps.newHashMap();
  private String name;
  private String queryString;
  
  
  public void setName(String name) {
    if(name.length()>500) {
      this.name = name.substring(0, 499);
    }else {
      this.name = name;
    }
  }


  public void start(SparqlMapPhase phase) {
    assert (phaseDurations.get(phase) == null);
    phaseDurations.put(phase, Stopwatch.createStarted());
  }

  public void stop(SparqlMapPhase phase) {
    phaseDurations.get(phase).stop();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (name != null) {
      sb.append("name: ");
      sb.append(name);
      sb.append(System.lineSeparator());
    }
    if (queryString != null) {
      sb.append("query: ");
      sb.append(queryString);
      sb.append(System.lineSeparator());
    }
    phaseDurations.forEach((phase, duration) -> {
      sb.append(phase);
      sb.append(": ");
      sb.append(duration.elapsed(TimeUnit.MICROSECONDS));
      sb.append(System.lineSeparator());
    });

    return sb.toString();
  }
}
