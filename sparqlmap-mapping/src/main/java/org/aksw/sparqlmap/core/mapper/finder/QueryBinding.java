package org.aksw.sparqlmap.core.mapper.finder;

import java.util.List;

import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.apache.jena.sparql.core.Quad;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryBinding {
  List<Quad> head;
  List<List<QuadMap>> rows;

}
