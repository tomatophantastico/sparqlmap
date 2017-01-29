package org.aksw.sparqlmap.core.mapper.compatibility;

import java.util.Map;

import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;

import com.google.common.collect.PeekingIterator;

import com.google.common.collect.Maps;

class TermMapTemplateWalker{
  
  
   TermMapTemplateIterator iTmTemp1;
   TermMapTemplateIterator iTmTemp2;
   
   
   public TermMapTemplateWalker(PeekingIterator<TermMapTemplateTuple> iTmTemp1, PeekingIterator<TermMapTemplateTuple> iTmTemp2) {
    this.iTmTemp1 = new TermMapTemplateIterator(iTmTemp1);
    this.iTmTemp2 = new TermMapTemplateIterator(iTmTemp2);
  }
   
   
   

  public boolean eval(){
    return eval(true);
  }
  
  
  public boolean eval(boolean generateFilters){
    
    boolean result = true;
 
    while(result&&!iTmTemp1.isConsumed()&&!iTmTemp2.isConsumed()){
      
      //compare the prefixes first
      if(iTmTemp1.prefix!=null&&iTmTemp2.prefix!=null){
        result = iTmTemp1.reducePrefix(iTmTemp2) 
              |  iTmTemp2.reducePrefix(iTmTemp1);
      //  
      }else if(iTmTemp1.prefix==null && iTmTemp2.prefix!=null){
        result = iTmTemp2.reducePrefixByColum(iTmTemp1, generateFilters);
      }else if(iTmTemp1.prefix!=null && iTmTemp2.prefix==null){
        result = iTmTemp1.reducePrefixByColum(iTmTemp2, generateFilters);
      }else if(iTmTemp1.column != null && iTmTemp2.column != null ){
        iTmTemp1.filterColEqCols.put(iTmTemp1.column, iTmTemp2.column);
        iTmTemp1.nullColumn();
        iTmTemp2.filterColEqCols.put(iTmTemp2.column, iTmTemp1.column);
        iTmTemp2.nullColumn();
        
      }else {
        result = false;
      }
       
      
     
    
    }
    
    return result;
  }
  
   
  
  private class TermMapTemplateIterator{
    
    private String prefix;
    private String column;
    private boolean encodeCol;
    private PeekingIterator<TermMapTemplateTuple> tmtmIter;

    Map<String,String> filterColEqCols = Maps.newHashMap();
    Map<String,String> filterColEqString = Maps.newHashMap();
    

    
    public TermMapTemplateIterator(PeekingIterator<TermMapTemplateTuple> tmtmIter) {
      super();
      this.tmtmIter = tmtmIter;
      nextTemplateTuple();
    }

    public void nextTemplateTuple(){
      if(prefix!=null && prefix.length()==0){
        prefix = null;
      }
      if(tmtmIter.hasNext() && prefix==null && column == null){
        TermMapTemplateTuple currentTuple = tmtmIter.next();
        prefix = currentTuple.getPrefix();
        column = currentTuple.getColumn();
        encodeCol = currentTuple.isColUrlEncoding();
      }
      
    }
    
    
    public String peekAtPrefix(){
      String result = null;
      
      if(tmtmIter.hasNext()){
        result = tmtmIter.peek().getPrefix();
      }
      
      return result;
      
    }
    
    public boolean isConsumed(){

      
      return (!tmtmIter.hasNext() && prefix==null && column == null);
    }
    
   
    
    public boolean reducePrefix(TermMapTemplateIterator otheriter){
      boolean compatible = false;
      if(prefix!=null && otheriter.prefix!=null){
        if(prefix.startsWith(otheriter.prefix)){
          prefix = prefix.substring(otheriter.prefix.length());
          otheriter.nullPrefix();
          compatible = true;
        }
      }
      nextTemplateTuple();
      return compatible;
    }
    
    public boolean reducePrefixByColum(TermMapTemplateIterator otheriter, boolean createFilter){
      boolean compatible = false;
      if(prefix!=null && otheriter.column!=null){
        //check until where this column goes. peeking therefore behind the next column
        String peekPrefix = otheriter.peekAtPrefix();
        if(peekPrefix!=null){
          int indexPeekPrefix = prefix.indexOf(peekPrefix);
          if(indexPeekPrefix>=0){
            
            if(createFilter){
              String colvalue = prefix.substring(0, indexPeekPrefix);
              otheriter.filterColEqString.put(otheriter.column, colvalue);
            }
            
            prefix = prefix.substring(indexPeekPrefix);
            otheriter.nullColumn();
            compatible = true;
          }
       
        }else{
          //if the column gets encoded, we can adanvce to the next reserved character
          if(otheriter.encodeCol){
            char[] prefixChars = prefix.toCharArray();
            
            for(int i = 0; i < prefixChars.length; i++){
              if(URIHelper.RESERVED.get(prefixChars[i])){
                prefix = prefix.substring(i);
                compatible = true;
                break;
              }
            }
          }else{
            //not encoded, no suffix, we just take the whole prefix
            prefix =null;
            compatible = true;
          }
        }
        
        
    
      }
      
      return compatible;
    }
    
    private void nullPrefix() {
      prefix=null;
      nextTemplateTuple();
      
    }
    private void nullColumn(){
      column = null;
      nextTemplateTuple();
    }
      

   
    
  }
  
  
    
}