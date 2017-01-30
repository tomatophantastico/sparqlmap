package org.aksw.sparqlmap.backend.metamodel.translate;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlmap.core.r2rml.QuadMap;
import org.aksw.sparqlmap.core.r2rml.TermMap;
import org.aksw.sparqlmap.core.r2rml.TermMapConstant;
import org.aksw.sparqlmap.core.r2rml.TermMapReferencing;
import org.aksw.sparqlmap.core.r2rml.TermMapReferencing.JoinOn;
import org.aksw.sparqlmap.core.util.JenaHelper;
import org.aksw.sparqlmap.core.util.QuadPosition;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.JoinType;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaModelQueryWrapper {
  
  MetaModelContext mcontext;
  
  
  
  
  
  public MetaModelQueryWrapper(MetaModelContext mcontext) {
    super();
    this.mcontext = mcontext;
  }




  // keeps track of the cols in this query.
  private ListMultimap<String,SelectItem> alias2selectItem = ArrayListMultimap.create();
  
  
  //tracks all quads encountered, which is required to create a model later on.
  private Map<Quad,QuadMap> quads2quadMaps = Maps.newHashMap();
  
  
  //this map is used to identify neccessary self joins
  private Map<TermMap, String> termMap2variable = Maps.newHashMap();
  
  


  private List<FilterItem> filters  = Lists.newArrayList();
  // separate List, just in order to not bloat the filters array and make debuggin more simple
  private List<FilterItem> notNulls = Lists.newArrayList();
  
  
  // this map tracks which From Items are already joined and which are not.
  private Map<FromItem, FromItem> fromItemGroups = Maps.newHashMap();
   
  
  //incremented for self-join aliases
  private int selfJoinCount = 0;
  //incremented for join variable
  private int joinCount = 0;
  
  
  
  public void addQuad(Quad quad, QuadMap qm, boolean isOptional){
    
    quads2quadMaps.put(quad, qm);
    
    String selfJoinAlias = null;
        
    // check, if this quad has to be self joined
    // this is the case, any of assigned triples maps is used with an other variable.
    
    for(QuadPosition pos: QuadPosition.values()){
      Node node = JenaHelper.getField(quad, pos);
      TermMap termMap  = qm.get(pos);
      
      if(node.isVariable()){
        if(termMap2variable.containsKey(termMap) && !termMap2variable.get(termMap).equals(node.getName())&& ! (termMap instanceof TermMapConstant)){
          selfJoinAlias = "_" + this.selfJoinCount++;
        }else{
          termMap2variable.put(termMap, node.getName());
        }
      }
    }
    
    
    //process the table here.
  
    
    
   
    for(QuadPosition pos : QuadPosition.values()){
      Node node = JenaHelper.getField(quad, pos);
      TermMap termMap = qm.get(pos);
      FromItem fi = mcontext.getSchemaHelper().getFromItem(qm.getLogicalTable(), selfJoinAlias);
      
      if(!fromItemGroups.containsKey(fi)){
        fromItemGroups.put(fi,fi);

      }

      
      
      if(node.isVariable()){
        
        //preprocess referencing term maps
        
       if(termMap instanceof TermMapReferencing){
         TermMapReferencing tmf = (TermMapReferencing) termMap;
     
         FromItem newFi = mcontext.getSchemaHelper().getFromItem(tmf.getParent().getLogicalTable(),selfJoinAlias);
         
         
         // get the old join (which might just be a single item)
         FromItem oldJoin = fromItemGroups.get(fi);
         
         //build a join
         List<SelectItem> oldSis = Lists.newArrayList();
         List<SelectItem> newSis = Lists.newArrayList();
         for(JoinOn jo : tmf.getConditions()){
           oldSis.add(mcontext.getSchemaHelper().getSelectItem(fi, jo.getChildColumn()));
           newSis.add(mcontext.getSchemaHelper().getSelectItem(newFi, jo.getParentColumn()));
         }
         
         join(oldJoin, newFi, oldSis, newSis, isOptional);
         
         
         fi = newFi;
         termMap = tmf.getParent().getSubject();
       }
       
              
       
        
        
        addTermMap(node.getName(), termMap,fi, isOptional,selfJoinAlias);
      } 
    }    
  }
  
  
 
  
  
  
  
  public void addFilter(FilterItem fi){
    filters.add(fi);
  }
  
  /**
   * the control flow for inserting a term map
   * 
   * @param alias
   * @param termMap
   * @param isOptional
   */
  private void addTermMap(String alias, TermMap termMap, FromItem fi, boolean isOptional, String selfJoinAlias) {


    // first construct the select item

    if (alias2selectItem.containsKey(alias)) {
      // the alias is already in use, therefore constructing a join
      
      
      FromItem fiOld = alias2selectItem.get(alias).get(0).getFromItem();
      List<SelectItem> siOld = alias2selectItem.get(alias);
      List<SelectItem> siNew = mcontext.getSchemaHelper().getSelectItem(alias + "_" + joinCount++ , termMap,fi);
      
      join(fiOld,fi, siOld ,siNew, isOptional);
    } else {
      // not yet in use, simply add the termMap
      insertTermMap(alias, termMap, fi,isOptional);
    }

  }
 
  
  
  /**
   * delegete by term map type
   * 
   */
   private void insertTermMap(String alias, TermMap termMap,FromItem fi, boolean optional){

     List<SelectItem> selectItems = mcontext.getSchemaHelper().getSelectItem(alias, termMap, fi);
     alias2selectItem.putAll(alias, selectItems); 
     
     if(!optional){
       for(SelectItem selectItem: selectItems){
         notNulls.add(new FilterItem(selectItem, OperatorType.DIFFERENT_FROM, null));

       }
     }

     
    
  }
   
   






  private void join(FromItem fi1, FromItem fi2, List<SelectItem> si1s ,List<SelectItem> si2s, boolean optional){
    
    JoinType type = optional?JoinType.LEFT:JoinType.INNER;
    
    FromItem newjoin = new FromItem(type,fi1,fi2,si1s.toArray(new SelectItem[0]),si2s.toArray(new SelectItem[0]));
    
    
    // add the join for all FromItems it contains 
    List<FromItem> refOldJoinFis = Lists.newArrayList();
    for(FromItem  refOldJoinFiCand: fromItemGroups.keySet()){
      if(fromItemGroups.get(refOldJoinFiCand).equals(fi1)){
        refOldJoinFis.add(refOldJoinFiCand);
      }
    }
    // update with the new Join
    
    for(FromItem refoldJoin:  refOldJoinFis){
      fromItemGroups.put(refoldJoin, newjoin);
    }
     
    
  }



  




  public List<SelectItem> getSelectItems(String name) {
   
    return this.alias2selectItem.get(name);
  }




  public Query getQuery() {
    
    Query query = new Query();
    query.from(fromItemGroups.values().toArray(new FromItem[0]));
    query.select(alias2selectItem.values().toArray(new SelectItem[0]));
    List<FilterItem> filters = Lists.newArrayList();
    filters.addAll(filters);
    filters.addAll(notNulls);
    query.where(filters);
    
    return query;
  }




  public Map<Quad,QuadMap> getQuads2quadMaps() {
    return quads2quadMaps;
  }
  
  
  
  private class Join {
    private SelectItem s1;
    private SelectItem s2;
    private boolean optional;
    
    
    
    
    public Join(SelectItem s1, SelectItem s2, boolean optional) {
      super();
      this.s1 = s1;
      this.s2 = s2;
      this.optional = optional;
    }
    
    
    public SelectItem getS1() {
      return s1;
    }
    public void setS1(SelectItem s1) {
      this.s1 = s1;
    }
    public SelectItem getS2() {
      return s2;
    }
    public void setS2(SelectItem s2) {
      this.s2 = s2;
    }
    public boolean isOptional() {
      return optional;
    }
    public void setOptional(boolean optional) {
      this.optional = optional;
    }
    
   
    
    
    
    
    
    
  }
    
    
  

}
