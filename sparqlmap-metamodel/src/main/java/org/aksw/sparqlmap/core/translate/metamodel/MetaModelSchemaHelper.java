package org.aksw.sparqlmap.core.translate.metamodel;

import java.util.List;
import java.util.ListIterator;

import org.aksw.sparqlmap.core.r2rml.QuadMap.LogicalTable;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapColumn;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;

import com.google.common.collect.Lists;


/**
 * This class binds 
 * @author joerg
 *
 */
public class MetaModelSchemaHelper {

  private DataContext datacontext;
  
  
  
  
  public MetaModelSchemaHelper(DataContext datacontext) {
    super();
    this.datacontext = datacontext;
  }



  
  
  public SelectItem getSelectItem(String alias, String tablename, String colname){
    
    
    
    SelectItem si = new SelectItem(getColumn(tablename, colname));
    si.setAlias(alias);
    
    return si;
  }
  
  
  public FromItem getTableFrom(String alias, String tablename){
    FromItem fi =  new FromItem(datacontext.getDefaultSchema().getTableByName(tablename));
    fi.setAlias(alias);
    return fi;
  }
  
  
  public List<SelectItem> getSelectItem(String alias, TermMap termMap, FromItem fi){
    List<SelectItem> result = Lists.newArrayList();
    
    if (termMap instanceof TermMapColumn) {
      TermMapColumn tmc = (TermMapColumn) termMap;
      
      result.add(getSelectItem(alias, fi, tmc.getColumn()));
      
      
    }else if(termMap instanceof TermMapTemplate){
      TermMapTemplate tmt = (TermMapTemplate) termMap;
      ListIterator<TermMapTemplateTuple> tmtIter = tmt.getTemplate().listIterator();
      while(tmtIter.hasNext()){
        TermMapTemplateTuple tmtsc = tmtIter.next();
        result.add(getSelectItem(alias + "_" + tmtIter.previousIndex(), fi, tmtsc.getColumn()));
      }
      
    }
    
    
    return result;
  }
  
  
  public SelectItem getSelectItem(String alias,FromItem fi, String colname){
    

    SelectItem si = new SelectItem(fi.getTable().getColumnByName(colname));
    si.setAlias(alias);
    
    return si;
  }
  
  public SelectItem getSelectItem(FromItem fi, String childColumn) {
    
    return getSelectItem(null, fi, childColumn);
  }


  //consider backing this by a cache
  public Column getColumn(String tablename, String colname){
    Column col = datacontext.getDefaultSchema().getTableByName(tablename).getColumnByName(colname);
    return col;
  }
  
  
  public FromItem getFromItem(LogicalTable lTable, String aliasSuffix){
    
    
    
    FromItem fi  = null;
    
    if(lTable.getTablename()!=null){
      fi = new FromItem(datacontext.getDefaultSchema().getTableByName(lTable.getTablename()));
      fi.setAlias(lTable.getTablename());
    }else{
      fi = new FromItem(lTable.getQuery());
      fi.setAlias("query_" + lTable.getQuery().hashCode());
    }
    
    if(aliasSuffix!=null){
      fi.setAlias(fi.getAlias() + aliasSuffix );

    }
    return fi;
  }
  
  
}
