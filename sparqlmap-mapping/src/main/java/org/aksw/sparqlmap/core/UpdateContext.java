package org.aksw.sparqlmap.core;

import java.util.Map;

import org.aksw.sparqlmap.core.mapper.finder.QueryBinding;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class  UpdateContext{

  String originalQuery;
  UpdateRequest compiled;
  
  Map<Update,QueryBinding> updateBinding = Maps.newHashMap();
  
}
