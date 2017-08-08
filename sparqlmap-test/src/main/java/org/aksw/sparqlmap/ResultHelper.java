package org.aksw.sparqlmap;

import com.google.common.base.Splitter;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/**
 * Created by joerg on 28.05.17.
 */
public class ResultHelper {

    private static Logger log = LoggerFactory.getLogger(ResultHelper.class);



    static public void assertModelAreEqual(Model result, Model expectedresultRaw) {

        Model cleanedExpected = ModelFactory.createDefaultModel();
        expectedresultRaw.listStatements().mapWith(stmt -> {
            Statement retStmt = stmt;
            String dtUri = stmt.getObject().isLiteral() && stmt.getObject().asLiteral().getDatatypeURI() != null ? stmt.getObject().asLiteral().getDatatypeURI() : null;
            if(XSD.xdouble.getURI().equals(dtUri)){

                retStmt = new StatementImpl(stmt.getSubject(), stmt.getPredicate(), ResourceFactory.createTypedLiteral(stmt.getObject().asLiteral().getDouble()));
            }

            return retStmt;
        }).forEachRemaining(stmnt -> cleanedExpected.add(stmnt));



        StringBuffer models =new StringBuffer();

        models.append("Actual result is :\n");
        models.append("=============================\n");
        ByteArrayOutputStream actualResBos  = new ByteArrayOutputStream();
        RDFDataMgr.write(actualResBos, result, Lang.TURTLE);
        models.append(actualResBos);

        models.append("=======================\nExpected was: \n");
        ByteArrayOutputStream expectedResBos  =new ByteArrayOutputStream();
        RDFDataMgr.write(expectedResBos, cleanedExpected,Lang.TURTLE);
        models.append(expectedResBos);
        models.append("=======================\nMissing in the sparqlmap result is: \n");

        Model missingInActual = cleanedExpected.difference(result);
        ByteArrayOutputStream missingInActualResBos  =new ByteArrayOutputStream();
        RDFDataMgr.write(missingInActualResBos, missingInActual,Lang.TURTLE);
        models.append(missingInActualResBos);

        models.append("=======================\nThese triples were unexpected: \n");
        Model missingInExpected = result.difference(cleanedExpected);
        ByteArrayOutputStream missingInExpectedResBos  =new ByteArrayOutputStream();
        RDFDataMgr.write(missingInExpectedResBos, missingInExpected,Lang.TURTLE);
        models.append(missingInExpectedResBos);
        models.append("=============================");


        //check if we have to deal with

        assertTrue(models.toString(), result.isIsomorphicWith(cleanedExpected));


    }

    public static void assertResultSetsAreEqual(ResultSet result,
                                                ResultSet expectedRS) {
        result = ResultSetFactory.makeRewindable(result);
        expectedRS = ResultSetFactory.makeRewindable(expectedRS);
        boolean isEqual  = ResultSetCompare.equalsByTerm(result, expectedRS);
        StringBuffer comparison = new StringBuffer();

        //we give it a second try, just because the result set comparison somtimes seems to fail
        if(!isEqual){

            Iterator<String> resStringIter = org.jooq.lambda.Seq.seq(Splitter.on(System.lineSeparator()).split(ResultSetFormatter.asText(result))).sorted().iterator();
            Iterator<String> exResStringIter = org.jooq.lambda.Seq.seq(Splitter.on(System.lineSeparator()).split(ResultSetFormatter.asText(expectedRS))).sorted().iterator();
            boolean stringsEqual = true;
            while(resStringIter.hasNext() && exResStringIter.hasNext()){
                if(!resStringIter.next().equals(exResStringIter.next())){
                    stringsEqual = false;
                    break;
                }
            }
            if(stringsEqual && !resStringIter.hasNext() && !exResStringIter.hasNext()){
                isEqual = true;
            }

        }



        comparison.append("Actual result is :\n");
        comparison.append("=============================");

        comparison.append(ResultSetFormatter.asText(result));

        comparison.append("\nCount: "+ result.getRowNumber()+ "=======================\nExpected was: ");

        comparison.append(ResultSetFormatter.asText(expectedRS));
        comparison.append("\nCount:"+expectedRS.getRowNumber()+"=============================");


        log.debug(comparison.toString());
        assertTrue(comparison.toString(),isEqual);

    }
}
