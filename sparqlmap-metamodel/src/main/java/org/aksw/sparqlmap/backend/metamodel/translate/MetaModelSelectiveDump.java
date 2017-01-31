package org.aksw.sparqlmap.backend.metamodel.translate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.QuadMap.LogicalTable;
import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.XSD;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.parser.QueryParser;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aol.cyclops.data.async.Queue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Maps;

/**
 * 
 * @author joerg
 *
 */
public class MetaModelSelectiveDump implements Runnable{
  
  private AtomicInteger threadCount;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaModelSelectiveDump.class);

  public MetaModelSelectiveDump(LogicalTable ltable, Collection<QuadMap> quadmaps, DataContext dcontext, Queue<Multimap<Node,Triple>> queue, AtomicInteger threadCount) {
    super();
    this.ltable = ltable;
    this.quadmaps = quadmaps;
    this.dcontext = dcontext;
    this.queue = queue;
    this.threadCount = threadCount;
    prepare();
  }


  private LogicalTable ltable;
  private Collection<QuadMap> quadmaps;
  private DataContext dcontext;
  private Queue<Multimap<Node,Triple>> queue;
  
  private Query query;
  
  
  private Map<String,Column> colnameSelectItem = Maps.newHashMap();
  

  
  

  
  
  
  private void prepare(){
     
    query = new Query();
    
    Table table = null;
    if(ltable.getTablename()==null){
      
      QueryParser qp = new QueryParser(dcontext, ltable.getQuery().replaceAll("\"", ""));
      Query subQuery = qp.parse();
      FromItem fi = new FromItem( subQuery);
      fi.setAlias("sq");  
      query.from(fi);
    }else{
      table = dcontext.getTableByQualifiedLabel(ltable.getTablename());
      query.from(table);
    }
    Set<String> cols = Sets.newHashSet();
    
    for(QuadMap qm: quadmaps){
      if(!qm.getLogicalTable().equals(ltable)){
        throw new IllegalArgumentException("QuadMap not of supplied logical table");
      }
      
      for(QuadPosition pos: QuadPosition.values()){
        TermMap tm = qm.get(pos);
        cols.addAll(TermMap.getCols(tm));
      }
    }
    
    if(cols.size()>0){
      for(String col:cols){
        Column column;
        if(ltable.getTablename()==null){
          
          MutableColumn mcolumn = new MutableColumn(col);
          
          // remove dependecy on metamodel-jdbc via reflection
          
          Lists.newArrayList(dcontext.getClass().getMethods()).stream()
          .filter(m -> m.getName().equals("getIdentifierQuoteString") && m.getParameterTypes().length ==0)
          .forEach(m -> {
            try {
                mcolumn.setQuote( (String) m.invoke(dcontext));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          });
          
        
          column = mcolumn;
        }else{
          column = dcontext.getTableByQualifiedLabel(ltable.getTablename()).getColumnByName(col);
        }
        
        
        
        colnameSelectItem.put(col, column);
        query.select(column);
      }
    }else{
      // in case cols is emtpy, for example if the quad maps have only constant term maps, we shorten the querying:
      
      query.selectAll();
      query.setMaxRows(1);
    }
    
    
    
  }
  
  
  
  public void dump(){
    int count = 0;
    try(DataSet ds =  dcontext.executeQuery(query)){
      while(ds.next()){
        Row row = ds.getRow();
        count++;
        Multimap<Node,Triple> rowTriples = HashMultimap.create();
        for(QuadMap qm: quadmaps){
          Node graph = materialize(row,qm.getGraph());
          Node subject = materialize(row, qm.getSubject());
          Node predicate = materialize(row, qm.getPredicate());
          Node object = materialize(row, qm.getObject());
          if(graph!=null&& subject!=null&&predicate!=null &&object!=null){
            rowTriples.put(graph, new Triple(subject, predicate, object));      
          }
        }
        queue.offer(rowTriples);
      }
    }catch(MetaModelException e){
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
    if(tm.getDatatypIRI()!=null){
      dt = new BaseDatatype(tm.getDatatypIRI());
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
        if(tm.getLang()!=null){
          result = NodeFactory.createLiteral(cfString, tm.getLang());
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
      ColumnType ct = colnameSelectItem.get(((TermMapColumn) tm).getColumn()).getType();
      if(ct ==null || ct.isLiteral()){
        // do nothing
      }else if(ct.isBinary()){
        dt = XSDDatatype.XSDbase64Binary;
      }else if(ct.isBoolean()){
        dt = XSDDatatype.XSDboolean;
      }else if(ct.isNumber()){
        if(ct.getJavaEquivalentClass().equals(Double.class)){
          dt = XSDDatatype.XSDdouble;
        } if (ct.getJavaEquivalentClass().equals(UUID.class) ) {
          // do nothing
        }else{
          dt = XSDDatatype.XSDinteger;
        }
      } else if (ct.getName().equals("DATE")){
        dt = XSDDatatype.XSDdate;
      }else if(ct.getName().equals("TIME")){
        dt = XSDDatatype.XSDtime;
      }else if(ct.getName().equals("TIMESTAMP")){
        dt = XSDDatatype.XSDdateTime;
      }
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
          Object val = row.getValue(colnameSelectItem.get(tmtt.getColumn()));
          if(val!=null){
            sb.append(IRILib.encodeUriComponent(val.toString()));
          }else{
            sb = new StringBuffer();
            break;
          }
        }
      }
      String sbString = sb.toString();
      if(!sbString.isEmpty()){
        result = sb.toString();
      }
    } else if(tm instanceof TermMapColumn){
      Object val = row.getValue(colnameSelectItem.get(((TermMapColumn) tm).getColumn()));

      if( val!=null){
        if(dt==null){
          
        }else if(dt.equals(XSDDatatype.XSDdateTime)){
          if(val instanceof Timestamp){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp) val).toInstant().toEpochMilli());
          }else {
            result = val.toString().replaceAll("\\s", "T");
            
          }
        } else if(dt.equals(XSDDatatype.XSDboolean)){
          result = val.toString().toLowerCase();
        }else if(dt.equals(XSDDatatype.XSDhexBinary)){
          result = Base64.getEncoder().encodeToString(val.toString().getBytes());  
          
        } 
        
        result = val.toString();
      }
    }
    return result;
  }
  
  




  @Override
  public void run() {
    dump();
  }
  
  

}