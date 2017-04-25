package org.aksw.sparqlmap.backend.metamodel.translate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.aksw.sparqlmap.backend.metamodel.mapper.SchemaTranslator;
import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.aksw.sparqlmap.core.schema.LogicalTable;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.query.parser.QueryParser;
import org.apache.metamodel.query.parser.QueryParserException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.jooq.lambda.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aol.cyclops.data.async.Queue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * 
 * @author joerg
 *
 */
public class MetaModelSelectiveDump implements Runnable{
  
  private AtomicInteger threadCount;

  private boolean rowwiseBlanks;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaModelSelectiveDump.class);

  public MetaModelSelectiveDump(LogicalTable ltable, Collection<QuadMap> quadmaps, DataContext dcontext, Queue<Multimap<Node,Triple>> queue, AtomicInteger threadCount, boolean rowwiseBlanks) {
    super();
    this.ltable = ltable;
    this.quadmaps = quadmaps;
    this.dcontext = dcontext;
    this.queue = queue;
    this.threadCount = threadCount;
    this.rowwiseBlanks = rowwiseBlanks;
    createQuery();
  }


  private LogicalTable ltable;
  private Collection<QuadMap> quadmaps;
  private DataContext dcontext;
  private Queue<Multimap<Node,Triple>> queue;
  
  private Query query;
  
  
  private Map<String,SelectItem> _colnameSelectItem = Maps.newHashMap();
  
  private void putColNameSelectItem(String name, SelectItem si){
    _colnameSelectItem.put(name.toUpperCase(), si);
  }
  
  private SelectItem getColNameSelectItem(String name){
    return _colnameSelectItem.get(name.toUpperCase());
  }
  

  
  

  
  
  /**
   * creates the query to be executed
   */
  private void createQuery(){

    if(ltable.getTablename()==null){

      try {
        QueryParser qp = new QueryParser(dcontext, ltable.getQuery().replaceAll("\"", ""));
        this.query = qp.parse();
       
               
      } catch (QueryParserException e) {
        createQueryUsingUnparsableQuery(ltable.getQuery(), quadmaps);
      }

    }else{
      createQueryUsingTable(this.dcontext.getTableByQualifiedLabel(ltable.getName()), quadmaps);
    }
    query.getSelectClause().getItems().forEach(si -> putColNameSelectItem(si.getAlias()!=null?si.getAlias():si.getColumn().getName(), si));

  }
  
  
  
  private void createQueryUsingUnparsableQuery(String queryString, Collection<QuadMap> quadmaps){
    Query query = new Query();
    FromItem fi = new FromItem(String.format("(%s)", queryString));
    fi.setAlias("subquery");
    query.from(fi);
    
    quadmaps.stream().flatMap(qm -> qm.getCols().stream()).distinct().forEach(col -> {
      MutableColumn mcol = new  MutableColumn(col);
      mcol.setQuote("\"");
      SelectItem si = new SelectItem(mcol, fi);
      query.select(si);
      
    });
    this.query = query;
  }
  
  
  private void createQueryUsingTable(Table table, Collection<QuadMap> quadmaps){
    Query query = new Query();
    query.from(new FromItem(table));
    quadmaps.stream().flatMap(qm -> qm.getCols().stream()).forEach(col->{
      Column column =  table.getColumnByName(col);
      
      //check if the columname is actually a number
      if(column == null && col.startsWith("#")){
        int colNo = Integer.valueOf(col.substring(1));
        column = dcontext.getTableByQualifiedLabel(ltable.getTablename()).getColumn(colNo);
      }
      query.select(column);
    });
    this.query = query;
  }
  

  
  
  public void dump(){
    int count = 0;
    try(DataSet ds =  dcontext.executeQuery(query)){
      while(ds.next()){
        Row row = ds.getRow();
        count++;
        Multimap<Node,Triple> rowTriples = HashMultimap.create();
        Map<TermMap,Node> subjectsofRow = quadmaps.stream()
            .map(qm -> qm.getSubject()).distinct()
            .map(trms -> Tuple.tuple(trms, (rowwiseBlanks && trms.isBlank() )? ResourceFactory.createResource().asNode() : materialize(row,trms)))
            .filter(t -> t.v2 != null)
            .collect(Collectors.toMap(t -> t.v1, t -> t.v2));
        
        
        for(QuadMap qm: quadmaps){
          Node graph = materialize(row,qm.getGraph());
          Node subject = subjectsofRow.get(qm.getSubject());
          Node predicate = materialize(row, qm.getPredicate());
          Node object = materialize(row, qm.getObject());
          if(graph!=null&& subject!=null&&predicate!=null &&object!=null){
            rowTriples.put(graph, new Triple(subject, predicate, object));      
          }
        }
        queue.offer(rowTriples);
      }
    }catch(Exception e){
      LOGGER.error("Error executing: "+ query.toSql() ,e);
    }finally{
      synchronized (threadCount) {
        if(threadCount.get()>1){
          threadCount.decrementAndGet();
        }else{
          queue.close();

        }
      }
      LOGGER.debug("Query {} executed \n with {} results", query.toSql(),count);
    }

   
    
  }
  
  private Node materialize(Row row, TermMap tm){
    Node result = null;
    BaseDatatype dt = null;
    if(tm.getDatatypIRI().isPresent()){
      dt = new BaseDatatype(tm.getDatatypIRI().get());
    }else{
      if(tm instanceof TermMapColumn){
        dt = getNaturalDatatype(row,tm);
      }
    }
    
    
    String cfString = assembleString(tm, row, dt);
    
    
    if(cfString!=null){
      if(tm.getTermTypeIRI().equals(R2RML.IRI_STRING)){
        result = NodeFactory.createURI(cfString);
      }else if(tm.getTermTypeIRI().equals(R2RML.BLANKNODE_STRING)){
        result = NodeFactory.createBlankNode(cfString);
      }else{
        if(tm.getLang().isPresent()){
          result = NodeFactory.createLiteral(cfString, tm.getLang().get());
        }else if(dt!=null){
          result = NodeFactory.createLiteral(cfString, dt );
        }else{
          result = NodeFactory.createLiteral(cfString);
        }
      }
    }
    return result;
  }
  
  /**
   * this methodt implement R2RML spec (https://www.w3.org/TR/r2rml/) section 10.2 "Natural Mapping of SQL Values"
   * @param row
   * @param tm
   * @return
   */
  private XSDDatatype getNaturalDatatype(Row row, TermMap tm) {
    XSDDatatype dt = null;
    if(tm instanceof TermMapColumn){
      
      
      ColumnType ct = getColNameSelectItem(((TermMapColumn) tm).getColumn()).getColumn().getType();
      dt = SchemaTranslator.getXSDDataType(ct).orElse(null);
    }
      
    return dt;
  }


  /**
   * constructs the canonical representation of the mapped valies
   * @param tm
   * @param row
   * @param dt
   * @return the canonical representation or null if any of the referenced columns is null.
   */
  private String assembleString(TermMap tm, Row row, BaseDatatype dt){
    Optional<Pattern> requiredPattern = tm.getConditionPattern();
    Optional<String> transformPattern = Optional.ofNullable(tm.getTransform());
    
    
    String result = null;
    if(tm instanceof TermMapConstant){
      result = ((TermMapConstant) tm).getConstant();
    }else if(tm instanceof TermMapTemplate){
      StringBuffer sb = new StringBuffer();
      for(TermMapTemplateTuple tmtt : ((TermMapTemplate) tm).getTemplate()){
        if(tmtt.getPrefix()!= null){
          sb.append(tmtt.getPrefix());
        }
        if(tmtt.getColumn()!=null){
          Object val = row.getValue(getColNameSelectItem(tmtt.getColumn().toUpperCase()));
          if(val!=null){
            if(tm.isIRI()){
              sb.append(IRILib.encodeUriComponent(val.toString()));
            }else{
              sb.append(val.toString());
            }
          }else{
            sb = new StringBuffer();
            break;
          }
        }
      }
      String sbString = sb.toString();
      if(!sbString.isEmpty()){
        if(requiredPattern.isPresent()){
          Matcher matcher = requiredPattern.get().matcher(sbString);
          if(matcher.matches()){
            if(transformPattern.isPresent()){
              //transformation required
              result = matcher.replaceAll(transformPattern.get());
            }else{
              //no transformation
              result = sbString;
            }
          }
        }else{
          // no pattern required
          result = sbString;
        }
      }
    } else if(tm instanceof TermMapColumn){
      String colname = ((TermMapColumn) tm).getColumn();
      Object val = row.getValue(getColNameSelectItem(colname));
      String colRes = null;

      if( val!=null){
        if(dt==null){
          // no datatype, just convert to string
          colRes = val.toString();
        }else if(dt.getURI().equals(XSDDatatype.XSDdateTime.getURI())){
          if(val instanceof Timestamp){
            SimpleDateFormat xsdfmttr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            
            colRes = xsdfmttr.format(val);
          }else {
            colRes = val.toString().replaceAll("\\s", "T");
            
          }
        } else if(dt.getURI().equals(XSDDatatype.XSDboolean.getURI())){
          colRes = val.toString().toLowerCase();
        }else if(dt.getURI().equals(XSDDatatype.XSDhexBinary.getURI())){
          
          if(val instanceof byte[]){
            colRes = DatatypeConverter.printHexBinary((byte[]) val);
          }else{
            colRes = DatatypeConverter.printHexBinary(val.toString().getBytes());
          }      
        }else{
          // fallback
          colRes = val.toString();
        }
        //do the conditional check  and transformation
        
        if(requiredPattern.isPresent()){
          Matcher matcher = requiredPattern.get().matcher(colRes);
          if( matcher.matches()){
            if(transformPattern.isPresent()){
              result = matcher.replaceAll(transformPattern.get());
            }else{
              result = colRes;
            }
          }
        }else{
          result = colRes;
        }
      }
    }
    return result;
  }
  
  




  @Override
  public void run() {
    dump();
  }
  
  

}
