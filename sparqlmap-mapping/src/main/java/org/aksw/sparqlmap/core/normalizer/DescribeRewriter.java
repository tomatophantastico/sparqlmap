package org.aksw.sparqlmap.core.normalizer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.core.TranslationContext;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.Template;

import com.google.common.collect.Lists;


public class DescribeRewriter {
  
  
  
  public static void rewriteDescribe(TranslationContext tc){
    
      if(tc.getQuery().isDescribeType()){
        Query describe = tc.getQuery();
        String suf = createVariablesSuffix(describe);
        Query result = new Query();
        result.setQueryConstructType();
        List<Node> describedResources;
        
        if(describe.getResultURIs().isEmpty()){
          describedResources =  describe.getProject().getVars().stream().map(var -> (Node) var).collect(Collectors.toList());
        }else{
          describedResources = describe.getResultURIs();

        }
        
        
       
        

        for(Node todesc: describedResources){
          List<List<Triple>> allNested = createList(todesc, suf);
          
          List<Triple> all = allNested.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
          
          result.setConstructTemplate(new Template( BasicPattern.wrap(all)));
          
          ElementUnion union = new ElementUnion();

          allNested.forEach(list -> union.addElement(new ElementTriplesBlock(BasicPattern.wrap(list))));
          
          
          ElementGroup eg = new ElementGroup();
          
          eg.addElement(union);
          result.setQueryPattern(eg);
          
          
          
          if(describe.getQueryPattern()!=null){
            ElementGroup group = new ElementGroup();
            group.addElement(describe.getQueryPattern());
            group.addElement(result.getQueryPattern());
            result.setQueryPattern(group);
          }
        }
        
        
 
            
      
        
       
        
        describe.getGraphURIs().forEach(uri -> result.addGraphURI(uri));
        describe.getNamedGraphURIs().forEach(uri-> result.addNamedGraphURI(uri));
        tc.setQuery(result);

      }
      
      

  }
  
  
  private static List<List<Triple>> createList(Node describedResource, String suf){
    //create the triples used both in the template and the where condition.
    return Lists.newArrayList( 
      Lists.newArrayList(
          new Triple(Var.alloc("si0_" + suf),Var.alloc("pi0_"+ suf),describedResource)),
     
//      Lists.newArrayList(
//          new Triple(Var.alloc("si12_"+ suf),Var.alloc("pi12_"+ suf),Var.alloc("si10_"+ suf)),
//          new Triple(Var.alloc("si10_"+ suf),Var.alloc("pi10_"+ suf),describedResource)),
//
      Lists.newArrayList(
          new Triple(describedResource, Var.alloc("po0_"+ suf),Var.alloc("oo0_"+ suf)))
      
//      Lists.newArrayList(
//          new Triple(describedResource, Var.alloc("po10_"+ suf),Var.alloc("oo10_"+ suf)),
//          new Triple(Var.alloc("oo10_"+ suf), Var.alloc("po11_"+ suf),Var.alloc("oo11_"+ suf)))
    );
    
  }
  
  private static String createVariablesSuffix(Query query){
    
    List<String> vars = OpVars.mentionedVars(new AlgebraGenerator().compile(query)).stream().map(var -> var.getName()).collect(Collectors.toList());
    
    String prefix = null;
    
    Random random = new Random();
    
    while(prefix == null){
      String prefix_cand = "sm_" + random.nextInt(10000);
      if(!vars.stream().anyMatch(v -> v.startsWith(prefix_cand))){
        prefix = prefix_cand;
        break;
      }
    }
    return prefix;
    
  }

}
