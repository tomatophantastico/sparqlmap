package org.aksw.sparqlmap.core.mapper.compatibility;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aksw.sparqlmap.core.r2rml.TermMapTemplate;
import org.aksw.sparqlmap.core.r2rml.TermMapTemplateTuple;
import org.aksw.sparqlmap.core.schema.LogicalColumn;
import org.apache.jena.ext.com.google.common.collect.Lists;

import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

class TermMapTemplateWalker{
  
  TermMapTemplate tmt1;
  TermMapTemplate tmt2;
  
   
   
  public TermMapTemplateWalker(TermMapTemplate tmt1, TermMapTemplate tmt2) {
    this.tmt1 = tmt1;
    this.tmt2 = tmt2;

  }
   
   
   

  
  
  
  
  public Optional<List<CompatibilityRequires>> eval(){
    
    TermMapTemplateIterator iTmTemp1 =  new TermMapTemplateIterator(Iterators.peekingIterator(tmt1.getTemplate().iterator()));
    TermMapTemplateIterator iTmTemp2 = new TermMapTemplateIterator(Iterators.peekingIterator(tmt2.getTemplate().iterator()));
    
    Optional<List<CompatibilityRequires>> result = Optional.of(Lists.newArrayList());
 
    while(result.isPresent()&&!iTmTemp1.isConsumed()&&!iTmTemp2.isConsumed()){
      
      //compare the prefixes first
      if(iTmTemp1.prefix!=null&&iTmTemp2.prefix!=null){
        if(!(iTmTemp1.reducePrefix(iTmTemp2) 
              |  iTmTemp2.reducePrefix(iTmTemp1))){
          //if the prefixes do not match, emtpy result;
          result = Optional.empty();
        }
      //  
      }else if(iTmTemp1.prefix==null && iTmTemp2.prefix!=null){
        
        Optional<CompatibilityRequires> cres =  iTmTemp2.reducePrefixByColum(iTmTemp1);
        if(cres.isPresent()){
          result.get().add(cres.get());
        }else{
          result = Optional.empty();
        }
        
        
      }else if(iTmTemp1.prefix!=null && iTmTemp2.prefix==null){
        Optional<CompatibilityRequires> cres =  iTmTemp1.reducePrefixByColum(iTmTemp2);
        
        if(cres.isPresent()){
          result.get().add(cres.get());
        }else{
          result = Optional.empty();
        }

      }else if(iTmTemp1.column != null && iTmTemp2.column != null ){

        result.get().add(CompatibilityRequires.builder()
            .column(iTmTemp1.column)
            .valueColumn(iTmTemp2.column).build());
        iTmTemp1.nullColumn();
        iTmTemp2.nullColumn();
        
        
      }else {
        result = Optional.empty();
      }
       
      
     
    
    }
    
    return result;
  }
  
   
  
  private class TermMapTemplateIterator{
    
    private String prefix;
    private LogicalColumn column;
    private boolean encodeCol;
    private PeekingIterator<TermMapTemplateTuple> tmtmIter;

    
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
    
    public Optional<CompatibilityRequires> reducePrefixByColum(TermMapTemplateIterator otheriter){
      Optional<CompatibilityRequires> compatible = Optional.empty();
      if(prefix!=null && otheriter.column!=null){
        //check until where this column goes. peeking therefore behind the next column
        String peekPrefix = otheriter.peekAtPrefix();
        if(peekPrefix!=null){
          int indexPeekPrefix = prefix.indexOf(peekPrefix);
          if(indexPeekPrefix>=0){
            
            
            String colvalue = prefix.substring(0, indexPeekPrefix);
            
            
            prefix = prefix.substring(indexPeekPrefix);
           
            compatible = Optional.of(
                CompatibilityRequires.builder()
                  .column(otheriter.column)
                  .value(colvalue).build());
            
            
            otheriter.nullColumn();
            
          }
       
        }else{
          //if the column gets encoded, we can adanvce to the next reserved character
          if(otheriter.encodeCol){
            char[] prefixChars = prefix.toCharArray();
            
            for(int i = 0; i < prefixChars.length; i++){
              if(URIHelper.RESERVED.get(prefixChars[i])){
                String colvalue = prefix.substring(0, i);
                prefix = prefix.substring(i);
                compatible = Optional.of(CompatibilityRequires.builder().column(otheriter.column).value(colvalue).build());
                break;
              }
            }
          }else{
            //not encoded, no suffix, we just take the whole prefix
            compatible = Optional.of(CompatibilityRequires.builder().column(otheriter.column).value(prefix).build());
            prefix =null;
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