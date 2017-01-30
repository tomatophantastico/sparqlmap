import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;


public class Playground {

  
  
  public static void main(String[] args) {
    
    
    Query query = QueryFactory.create("select distinct ?class  {?s a ?class}");
    
    Model model  = ModelFactory.createDefaultModel();
    
    model.add(ResourceFactory.createResource("test:a"), ResourceFactory.createProperty("test:p"), "a-p-val");
    model.add(ResourceFactory.createResource("test:a"), RDF.type, OWL.Thing);

    QueryExecution quexec =  QueryExecutionFactory.create(query, model);
    
    System.out.println(quexec.toString());
    
    System.out.println("----------------");

    
    System.out.println(quexec.getContext());
    
    
    quexec.execSelect();

    
    
    
  }

}