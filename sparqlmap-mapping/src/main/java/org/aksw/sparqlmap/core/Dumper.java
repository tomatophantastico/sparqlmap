package org.aksw.sparqlmap.core;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Multimap;

public interface Dumper {
  
  public DatasetGraph dumpDatasetGraph();
  public Iterator<Quad> streamDump();
  public void streamDump(OutputStream fos);
  public Stream<Multimap<Node, Triple>> dump(Collection<String> mappingfilters, boolean fast);
  public void dump(OutputStream out, Lang format);
}
